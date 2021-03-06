// automatically generated by the FlatBuffers compiler, do not modify

package amidst.remote;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class BiomeListReply extends Table {
  public static BiomeListReply getRootAsBiomeListReply(ByteBuffer _bb) { return getRootAsBiomeListReply(_bb, new BiomeListReply()); }
  public static BiomeListReply getRootAsBiomeListReply(ByteBuffer _bb, BiomeListReply obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public BiomeListReply __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public BiomeEntry biomes(int j) { return biomes(new BiomeEntry(), j); }
  public BiomeEntry biomes(BiomeEntry obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int biomesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }

  public static int createBiomeListReply(FlatBufferBuilder builder,
      int biomesOffset) {
    builder.startObject(1);
    BiomeListReply.addBiomes(builder, biomesOffset);
    return BiomeListReply.endBiomeListReply(builder);
  }

  public static void startBiomeListReply(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addBiomes(FlatBufferBuilder builder, int biomesOffset) { builder.addOffset(0, biomesOffset, 0); }
  public static int createBiomesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startBiomesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endBiomeListReply(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

