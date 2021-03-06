// automatically generated by the FlatBuffers compiler, do not modify

package amidst.remote;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class Request extends Table {
  public static Request getRootAsRequest(ByteBuffer _bb) { return getRootAsRequest(_bb, new Request()); }
  public static Request getRootAsRequest(ByteBuffer _bb, Request obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public Request __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte dataType() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table data(Table obj) { int o = __offset(6); return o != 0 ? __union(obj, o) : null; }

  public static int createRequest(FlatBufferBuilder builder,
      byte data_type,
      int dataOffset) {
    builder.startObject(2);
    Request.addData(builder, dataOffset);
    Request.addDataType(builder, data_type);
    return Request.endRequest(builder);
  }

  public static void startRequest(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addDataType(FlatBufferBuilder builder, byte dataType) { builder.addByte(0, dataType, 0); }
  public static void addData(FlatBufferBuilder builder, int dataOffset) { builder.addOffset(1, dataOffset, 0); }
  public static int endRequest(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

