// automatically generated by the FlatBuffers compiler, do not modify

package amidst.remote;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class BiomeEntry extends Table {
  public static BiomeEntry getRootAsBiomeEntry(ByteBuffer _bb) { return getRootAsBiomeEntry(_bb, new BiomeEntry()); }
  public static BiomeEntry getRootAsBiomeEntry(ByteBuffer _bb, BiomeEntry obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public BiomeEntry __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int biomeId() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public String biomeName() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer biomeNameAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer biomeNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }

  public static int createBiomeEntry(FlatBufferBuilder builder,
      int biomeId,
      int biomeNameOffset) {
    builder.startObject(2);
    BiomeEntry.addBiomeName(builder, biomeNameOffset);
    BiomeEntry.addBiomeId(builder, biomeId);
    return BiomeEntry.endBiomeEntry(builder);
  }

  public static void startBiomeEntry(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addBiomeId(FlatBufferBuilder builder, int biomeId) { builder.addInt(0, biomeId, 0); }
  public static void addBiomeName(FlatBufferBuilder builder, int biomeNameOffset) { builder.addOffset(1, biomeNameOffset, 0); }
  public static int endBiomeEntry(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

