package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

public abstract class INITOBJNode extends NodeizedNodeBase {

  protected final NamedTypeSymbol type;

  private final int top;

  public INITOBJNode(NamedTypeSymbol type, int top) {
    this.type = type;
    this.top = top;
  }

  @Specialization(guards = "isValueType()")
  protected int executeValueType(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var dest = CILOSTAZOLFrame.popInt(frame, top - 1);
    var value = type.getContext().getAllocator().createNew(type);
    CILOSTAZOLFrame.setLocalObject(frame, dest, value);
    return top - 1;
  }

  @Fallback
  protected int executeReferenceType(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var dest = CILOSTAZOLFrame.popInt(frame, top - 1);
    CILOSTAZOLFrame.setLocalObject(frame, dest, StaticObject.NULL);
    return top - 1;
  }

  protected boolean isValueType() {
    var baseClass = type.getDirectBaseClass();
    assert baseClass != null;
    return baseClass.getNamespace().equals("System") && baseClass.getName().equals("ValueType");
  }
}
