package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

public abstract class STFLDNode extends FieldManipulationBaseNode {
  private final CLITablePtr fieldPtr;
  private final int top;

  public STFLDNode(MethodSymbol method, NamedTypeSymbol type, CLITablePtr fieldPtr, int top) {
    super(method, type.getAssignableField(fieldPtr));
    this.fieldPtr = fieldPtr;
    this.top = top;
  }

  @Specialization(guards = "isInt()")
  public int executeInt(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 2);
    int value = CILOSTAZOLFrame.popInt(frame, top - 1);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);

    object.getTypeSymbol().getAssignableField(this.fieldPtr).setInt(object, value);
    return top - 2;
  }

  @Specialization(guards = "isLong()")
  public int executeLong(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 2);
    long value = CILOSTAZOLFrame.popLong(frame, top - 1);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);

    object.getTypeSymbol().getAssignableField(this.fieldPtr).setLong(object, value);
    return top - 2;
  }

  @Specialization(guards = "isFloat()")
  public int executeFloat(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 2);
    double value = CILOSTAZOLFrame.popDouble(frame, top - 1);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);

    object.getTypeSymbol().getAssignableField(this.fieldPtr).setFloat(object, (float) value);
    return top - 2;
  }

  @Specialization(guards = "isDouble()")
  public int executeDouble(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 2);
    double value = CILOSTAZOLFrame.popDouble(frame, top - 1);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);

    object.getTypeSymbol().getAssignableField(this.fieldPtr).setDouble(object, value);
    return top - 2;
  }

  @Specialization(guards = "isBool()")
  public int executeBool(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 2);
    int value = CILOSTAZOLFrame.popInt(frame, top - 1);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);

    object.getTypeSymbol().getAssignableField(this.fieldPtr).setBoolean(object, value != 0);
    return top - 2;
  }

  @Specialization(guards = "isChar()")
  public int executeChar(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 2);
    int value = CILOSTAZOLFrame.popInt(frame, top - 1);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);

    object.getTypeSymbol().getAssignableField(this.fieldPtr).setChar(object, (char) value);
    return top - 2;
  }

  @Fallback
  public int executeReference(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 2);
    StaticObject value = CILOSTAZOLFrame.popObject(frame, top - 1);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);

    object.getTypeSymbol().getAssignableField(this.fieldPtr).setObject(object, value);
    return top - 2;
  }
}
