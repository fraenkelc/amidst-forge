// automatically generated by the FlatBuffers compiler, do not modify

package amidst.remote;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class CreateWorldRequest extends Table {
  public static CreateWorldRequest getRootAsCreateWorldRequest(ByteBuffer _bb) { return getRootAsCreateWorldRequest(_bb, new CreateWorldRequest()); }
  public static CreateWorldRequest getRootAsCreateWorldRequest(ByteBuffer _bb, CreateWorldRequest obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public CreateWorldRequest __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long seed() { int o = __offset(4); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
  public String worldType() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer worldTypeAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer worldTypeInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }
  public String generatorOptions() { int o = __offset(8); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer generatorOptionsAsByteBuffer() { return __vector_as_bytebuffer(8, 1); }
  public ByteBuffer generatorOptionsInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 8, 1); }

  public static int createCreateWorldRequest(FlatBufferBuilder builder,
      long seed,
      int worldTypeOffset,
      int generatorOptionsOffset) {
    builder.startObject(3);
    CreateWorldRequest.addSeed(builder, seed);
    CreateWorldRequest.addGeneratorOptions(builder, generatorOptionsOffset);
    CreateWorldRequest.addWorldType(builder, worldTypeOffset);
    return CreateWorldRequest.endCreateWorldRequest(builder);
  }

  public static void startCreateWorldRequest(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addSeed(FlatBufferBuilder builder, long seed) { builder.addLong(0, seed, 0L); }
  public static void addWorldType(FlatBufferBuilder builder, int worldTypeOffset) { builder.addOffset(1, worldTypeOffset, 0); }
  public static void addGeneratorOptions(FlatBufferBuilder builder, int generatorOptionsOffset) { builder.addOffset(2, generatorOptionsOffset, 0); }
  public static int endCreateWorldRequest(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

