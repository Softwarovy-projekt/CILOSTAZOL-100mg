package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticField;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

public abstract class STFLDNode extends NodeizedNodeBase {

  protected final StaticField field;
  private final CLITablePtr fieldPtr;
  private final int top;

  // TODO: Check ModuleSymbol::getLocalMethod for a way of getting the type for a field
  public STFLDNode(NamedTypeSymbol type, CLITablePtr fieldPtr, int top) {
    this.field = type.getAssignableField(fieldPtr);
    this.fieldPtr = fieldPtr;
    this.top = top;
  }

  @Specialization(guards = "isInt()")
  public int executeInt(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var objectReference = CILOSTAZOLFrame.popInt(frame, top - 2);
    var object = CILOSTAZOLFrame.getLocalObject(frame, objectReference);
    var value = CILOSTAZOLFrame.popInt(frame, top - 1);
    object.getTypeSymbol().getAssignableField(this.fieldPtr).setInt(object, value);
    return top - 2;
  }

  @Specialization(guards = "isLong()")
  public int executeLong(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var staticObject = CILOSTAZOLFrame.popObject(frame, top - 2);
    field.setLong(staticObject, CILOSTAZOLFrame.popLong(frame, top - 1));
    return top - 2;
  }

  @Specialization(guards = "isFloat()")
  public int executeFloat(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var staticObject = CILOSTAZOLFrame.popObject(frame, top - 2);
    field.setFloat(staticObject, (float) CILOSTAZOLFrame.popDouble(frame, top - 1));
    return top - 2;
  }

  @Specialization(guards = "isDouble()")
  public int executeDouble(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var staticObject = CILOSTAZOLFrame.popObject(frame, top - 2);
    field.setDouble(staticObject, CILOSTAZOLFrame.popDouble(frame, top - 1));
    return top - 2;
  }

  @Specialization(guards = "isBool()")
  public int executeBool(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var staticObject = CILOSTAZOLFrame.popObject(frame, top - 2);
    field.setBoolean(staticObject, CILOSTAZOLFrame.popInt(frame, top - 1) != 0);
    return top - 2;
  }

  @Specialization(guards = "isChar()")
  public int executeChar(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var staticObject = CILOSTAZOLFrame.popObject(frame, top - 2);
    field.setChar(staticObject, (char) CILOSTAZOLFrame.popInt(frame, top - 1));
    return top - 2;
  }

  @Fallback
  public int executeReference(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var staticObject = CILOSTAZOLFrame.popObject(frame, top - 2);
    field.setObject(staticObject, CILOSTAZOLFrame.popObject(frame, top - 1));
    return top - 2;
  }

  protected boolean isBool() {
    return field.getPropertyType() == boolean.class;
  }

  protected boolean isChar() {
    return field.getPropertyType() == char.class;
  }

  protected boolean isFloat() {
    return field.getPropertyType() == float.class;
  }

  protected boolean isDouble() {
    return field.getPropertyType() == double.class;
  }

  protected boolean isInt() {
    return field.getPropertyType() == int.class;
  }

  protected boolean isLong() {
    return field.getPropertyType() == long.class;
  }
}
