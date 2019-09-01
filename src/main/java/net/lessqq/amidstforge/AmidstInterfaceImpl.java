package net.lessqq.amidstforge;

import java.util.ArrayList;
import java.util.List;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Iterators;

import amidst.remote.sbe.BiomeDataBlockEncoder;
import amidst.remote.sbe.BiomeDataRequestDecoder;
import amidst.remote.sbe.BiomeDataResponseEncoder;
import amidst.remote.sbe.BiomeDataResponseEncoder.BiomeDataEncoder;
import amidst.remote.sbe.BiomeListRequestDecoder;
import amidst.remote.sbe.BiomeListResponseEncoder;
import amidst.remote.sbe.BiomeListResponseEncoder.BiomeEntryEncoder;
import amidst.remote.sbe.BooleanType;
import amidst.remote.sbe.CreateWorldRequestDecoder;
import amidst.remote.sbe.MessageHeaderDecoder;
import amidst.remote.sbe.MessageHeaderEncoder;
import io.aeron.Image;
import io.aeron.ImageFragmentAssembler;
import io.aeron.Publication;
import io.aeron.logbuffer.BufferClaim;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class AmidstInterfaceImpl {
    private static final Logger logger = LogManager.getLogger(AmidstForgeMod.MOD_ID);

    private final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private final BiomeDataRequestDecoder biomeDataRequestDecoder = new BiomeDataRequestDecoder();
    private final BiomeListRequestDecoder biomeListRequestDecoder = new BiomeListRequestDecoder();
    private final CreateWorldRequestDecoder createWorldRequestDecoder = new CreateWorldRequestDecoder();

    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final BiomeDataResponseEncoder biomeDataResponseEncoder = new BiomeDataResponseEncoder();
    private final BiomeListResponseEncoder biomeListResponseEncoder = new BiomeListResponseEncoder();

    private final BufferClaim bufferClaim = new BufferClaim();
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(1024);

    private BiomeDataAccessor biomeDataAccessor = new BiomeDataAccessor();
    private BiomeProviderAccess biomeProviderAccess = new IntegratedBiomeProviderAccess();

    private Publication publication;

    public void onAvailableImage(Image image) {
        FragmentHandler reassemblingFragmentHandler = new ImageFragmentAssembler(this::onFragment);
        image.poll(reassemblingFragmentHandler, 1);
    }

    private void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
        messageHeaderDecoder.wrap(buffer, offset);
        final int schemaId = messageHeaderDecoder.schemaId();
        if (schemaId != MessageHeaderDecoder.SCHEMA_ID) {
            throw new RemoteCommunicationException(
                    "expected schemaId=" + MessageHeaderDecoder.SCHEMA_ID + ", actual=" + schemaId);
        }
        final int templateId = messageHeaderDecoder.templateId();

        switch (templateId) {
        case BiomeDataRequestDecoder.TEMPLATE_ID:
            biomeDataRequestDecoder.wrap(buffer, offset + MessageHeaderDecoder.ENCODED_LENGTH,
                    messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());
            getBiomeData(biomeDataRequestDecoder);
            break;
        case BiomeListRequestDecoder.TEMPLATE_ID:
            biomeListRequestDecoder.wrap(buffer, offset + MessageHeaderDecoder.ENCODED_LENGTH,
                    messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());
            getBiomeList(biomeListRequestDecoder);
            break;
        case CreateWorldRequestDecoder.TEMPLATE_ID:
            createWorldRequestDecoder.wrap(buffer, offset + MessageHeaderDecoder.ENCODED_LENGTH,
                    messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());
            createNewWorld(createWorldRequestDecoder);
            break;
        default:
            throw new RemoteCommunicationException("Unhandled Template id " + templateId);
        }
    }

    private void getBiomeData(BiomeDataRequestDecoder biomeDataRequestDecoder) {
        int x = biomeDataRequestDecoder.x();
        int y = biomeDataRequestDecoder.y();
        int width = biomeDataRequestDecoder.width();
        int height = biomeDataRequestDecoder.height();
        boolean useQuarterResolution = BooleanType.T == biomeDataRequestDecoder.useQuarterResolution();
        int[] biomeData = biomeDataAccessor.getBiomeData(x, y, width, height, useQuarterResolution);

        int dataLength = width * height;
        int buckets = (int) Math.ceil(dataLength / (double) BiomeDataBlockEncoder.dataLength());
        int bufferLength = MessageHeaderEncoder.ENCODED_LENGTH + BiomeDataResponseEncoder.BLOCK_LENGTH
                + (buckets * BiomeDataResponseEncoder.BiomeDataEncoder.dataEncodingLength());

        while (true) {
            final long result = publication.tryClaim(bufferLength, bufferClaim);
            if (result > 0) {
                BiomeDataEncoder biomeDataEncoder = biomeDataResponseEncoder
                        .wrapAndApplyHeader(bufferClaim.buffer(), bufferClaim.offset(), messageHeaderEncoder)
                        .length(width * height).biomeDataCount(buckets);
                BiomeDataBlockEncoder data = null;
                for (int i = 0; i < width * height; i++) {
                    if (i % BiomeDataBlockEncoder.dataLength() == 0) {
                        data = biomeDataEncoder.next().data();
                        data.length((dataLength - i) % BiomeDataBlockEncoder.dataLength());
                    }
                    data.data(i / BiomeDataBlockEncoder.dataLength(), biomeData[i]);
                }
                bufferClaim.commit();
                break;
            }
        }
    }

    private void getBiomeList(BiomeListRequestDecoder biomeListRequestDecoder) {
        List<Biome> biomes = new ArrayList<>();
        Iterators.addAll(biomes, ForgeRegistries.BIOMES.iterator());

        biomeListResponseEncoder.wrapAndApplyHeader(buffer, 0, messageHeaderEncoder);
        BiomeEntryEncoder entryEncoder = biomeListResponseEncoder.biomeEntryCount(biomes.size());
        for (Biome b : biomes) {
            entryEncoder.next().biomeId(Biome.REGISTRY.getIDForObject(b)).biomeName(b.getBiomeName());
        }
        publication.offer(buffer);
    }

    private void createNewWorld(CreateWorldRequestDecoder createWorldRequestDecoder) {
        BiomeProvider biomeProvider = biomeProviderAccess.getBiomeProvider(createWorldRequestDecoder.seed(),
                mapWorldType(createWorldRequestDecoder.worldType()), createWorldRequestDecoder.generatorOptions());
        biomeDataAccessor.setBiomeProvider(biomeProvider);
    }

    private WorldType mapWorldType(String worldType) {
        switch (worldType) {
        case "DEFAULT":
            return WorldType.DEFAULT;
        case "FLAT":
            return WorldType.FLAT;
        case "LARGE_BIOMES":
            return WorldType.LARGE_BIOMES;
        case "AMPLIFIED":
            return WorldType.AMPLIFIED;
        case "CUSTOMIZED":
            return WorldType.CUSTOMIZED;
        default:
            throw new RuntimeException("Unknown WorldType " + worldType);
        }
    }

    public void setBiomeProviderAccess(BiomeProviderAccess biomeProviderAccess) {
        this.biomeProviderAccess = biomeProviderAccess;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

}
