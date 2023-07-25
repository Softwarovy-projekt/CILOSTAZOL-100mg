package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

public abstract class LDFLDNode extends FieldManipulationBaseNode {
  private final CLITablePtr fieldPtr;
  private final int top;

  public LDFLDNode(MethodSymbol method, NamedTypeSymbol type, CLITablePtr fieldPtr, int top) {
    super(method, type.getAssignableField(fieldPtr));
    this.fieldPtr = fieldPtr;
    this.top = top;
  }

  @Specialization(guards = "isInt()")
  public int executeInt(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 1);
    int value = object.getTypeSymbol().getAssignableField(this.fieldPtr).getInt(object);

    CILOSTAZOLFrame.putInt(frame, top - 1, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top - 1, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Int32));
    return top;
  }

  @Specialization(guards = "isLong()")
  public int executeLong(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 1);
    long value = object.getTypeSymbol().getAssignableField(this.fieldPtr).getLong(object);

    CILOSTAZOLFrame.putLong(frame, top - 1, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top - 1, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Int64));
    return top;
  }

  @Specialization(guards = "isFloat()")
  public int executeFloat(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 1);
    float value = object.getTypeSymbol().getAssignableField(this.fieldPtr).getFloat(object);

    CILOSTAZOLFrame.putDouble(frame, top - 1, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top - 1, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Single));
    return top;
  }

  @Specialization(guards = "isDouble()")
  public int executeDouble(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 1);
    double value = object.getTypeSymbol().getAssignableField(this.fieldPtr).getDouble(object);

    CILOSTAZOLFrame.putDouble(frame, top - 1, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top - 1, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Double));
    return top;
  }

  @Specialization(guards = "isBool()")
  public int executeBool(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 1);
    boolean value = object.getTypeSymbol().getAssignableField(this.fieldPtr).getBoolean(object);

    CILOSTAZOLFrame.putInt(frame, top - 1, value ? 1 : 0);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame,
        top - 1,
        method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Boolean));
    return top;
  }

  @Specialization(guards = "isChar()")
  public int executeChar(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 1);
    char value = object.getTypeSymbol().getAssignableField(this.fieldPtr).getChar(object);

    CILOSTAZOLFrame.putInt(frame, top - 1, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top - 1, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Char));
    return top;
  }

  @Specialization(
      replaces = {
        "executeInt",
        "executeLong",
        "executeFloat",
        "executeDouble",
        "executeBool",
        "executeChar"
      })
  public int executeReference(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    StaticObject object = getObjectFromFrame(frame, taggedFrame, top - 1);
    StaticObject value =
        (StaticObject) object.getTypeSymbol().getAssignableField(this.fieldPtr).getObject(object);

    CILOSTAZOLFrame.putObject(frame, top - 1, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top - 1, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Object));
    return top;
  }
}
