package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.exceptions.RuntimeCILException;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticField;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;

public final class StringMethodImplementations {
  public static Object stringCreateFromChar(VirtualFrame frame) {
    char value = (char) (int) frame.getArguments()[0];
    return CILOSTAZOLContext.get(null)
        .getAllocator()
        .createString(Character.toString(value), frame, 0);
  }

  public static Object stringFastAllocateString(VirtualFrame frame) {
    int length = (int) frame.getArguments()[0];
    return CILOSTAZOLContext.get(null).getAllocator().createStringWithoutContent(frame, 0, length);
  }

  public static Object stringFillStringChecked(VirtualFrame frame) {
    StaticObject dest = (StaticObject) frame.getArguments()[0];
    int destPos = (int) frame.getArguments()[1];
    StaticObject src = (StaticObject) frame.getArguments()[2];
    NamedTypeSymbol stringSymbol = (NamedTypeSymbol) dest.getTypeSymbol();
    StaticField arrayField = stringSymbol.getInstanceFields(frame, 0)[1];
    char[] destBuffer =
        (char[])
            (CILOSTAZOLContext.get(null).getArrayProperty().getObject(arrayField.getObject(dest)));
    char[] srcBuffer =
        (char[])
            (CILOSTAZOLContext.get(null).getArrayProperty().getObject(arrayField.getObject(src)));

    if (srcBuffer.length > destBuffer.length - destPos)
      throw RuntimeCILException.RuntimeCILExceptionFactory.create(
          RuntimeCILException.Exception.IndexOutOfRange, CILOSTAZOLContext.get(null), frame, 0);

    System.arraycopy(srcBuffer, 0, destBuffer, destPos, srcBuffer.length);
    return null;
  }

  public static Object stringGetLength(VirtualFrame frame) {
    StaticObject str = (StaticObject) frame.getArguments()[0];
    NamedTypeSymbol stringSymbol = (NamedTypeSymbol) str.getTypeSymbol();
    return stringSymbol.getInstanceFields(frame, 0)[0].getInt(str);
  }
}
