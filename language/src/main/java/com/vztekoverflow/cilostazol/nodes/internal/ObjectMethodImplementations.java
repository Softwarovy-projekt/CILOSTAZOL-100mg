package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;

public final class ObjectMethodImplementations {
  public static Object objectToString(VirtualFrame frame) {
    StaticObject object = (StaticObject) frame.getArguments()[0];
    return CILOSTAZOLContext.get(null)
        .getAllocator()
        .createString(object.getTypeSymbol().toString(), frame, 0);
  }

  public static Object byteToString(VirtualFrame frame) {
    byte value =
        (byte)
            IndirectLoader.loadByte(
                (StaticObject) frame.getArguments()[0], CILOSTAZOLContext.get(null));
    return CILOSTAZOLContext.get(null).getAllocator().createString(Byte.toString(value), frame, 0);
  }

  public static Object shortToString(VirtualFrame frame) {
    short value =
        (short)
            IndirectLoader.loadShort(
                (StaticObject) frame.getArguments()[0], CILOSTAZOLContext.get(null));
    return CILOSTAZOLContext.get(null).getAllocator().createString(Short.toString(value), frame, 0);
  }

  public static Object int32ToString(VirtualFrame frame) {
    int value =
        IndirectLoader.loadInt32(
            (StaticObject) frame.getArguments()[0], CILOSTAZOLContext.get(null));
    return CILOSTAZOLContext.get(null)
        .getAllocator()
        .createString(Integer.toString(value), frame, 0);
  }

  public static Object int64ToString(VirtualFrame frame) {
    long value =
        IndirectLoader.loadInt64(
            (StaticObject) frame.getArguments()[0], CILOSTAZOLContext.get(null));
    return CILOSTAZOLContext.get(null).getAllocator().createString(Long.toString(value), frame, 0);
  }

  public static Object floatToString(VirtualFrame frame) {
    float value =
        (float)
            IndirectLoader.loadFloat(
                (StaticObject) frame.getArguments()[0], CILOSTAZOLContext.get(null));
    return CILOSTAZOLContext.get(null).getAllocator().createString(Float.toString(value), frame, 0);
  }

  public static Object doubleToString(VirtualFrame frame) {
    double value =
        IndirectLoader.loadDouble(
            (StaticObject) frame.getArguments()[0], CILOSTAZOLContext.get(null));
    return CILOSTAZOLContext.get(null)
        .getAllocator()
        .createString(Double.toString(value), frame, 0);
  }
}
