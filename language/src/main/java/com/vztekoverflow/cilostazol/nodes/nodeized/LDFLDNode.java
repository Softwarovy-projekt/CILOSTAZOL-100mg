package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

public abstract class LDFLDNode extends FieldManipulationBaseNode {
  private final CLITablePtr fieldPtr;
  private final int top;

  // TODO: Check ModuleSymbol::getLocalMethod for a way of getting the type for a field
  public LDFLDNode(NamedTypeSymbol type, CLITablePtr fieldPtr, int top) {
    super(type.getAssignableField(fieldPtr));
    this.fieldPtr = fieldPtr;
    this.top = top;
  }

  @Specialization(guards = "isInt()")
  public int executeInt(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    var object = getObjectFromFrame(frame, taggedFrame, top - 1);
    var value = object.getTypeSymbol().getAssignableField(this.fieldPtr).getInt(object);
    CILOSTAZOLFrame.putInt(frame, top - 1, value);
    return top;
  }
}
