package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;

public final class ObjectMethodImplementations {
  public static Object ObjectToString(VirtualFrame frame) {
    StaticObject object = (StaticObject) frame.getArguments()[0];
    return CILOSTAZOLContext.get(null)
        .getAllocator()
        .createString(object.getTypeSymbol().toString(), frame, 0);
  }
}
