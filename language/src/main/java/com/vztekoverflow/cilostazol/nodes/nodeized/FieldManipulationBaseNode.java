package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticField;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.ReferenceSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

public abstract class FieldManipulationBaseNode extends NodeizedNodeBase {
  protected final StaticField field;

  protected FieldManipulationBaseNode(StaticField field) {
    this.field = field;
  }

  protected StaticObject getObjectFromFrame(
      VirtualFrame frame, TypeSymbol[] taggedFrame, int slot) {
    if (taggedFrame[slot] instanceof ReferenceSymbol) {
      slot = CILOSTAZOLFrame.popInt(frame, slot);
      // TODO: We should do slot + localsOffset -> merge the indirect load PR
      return CILOSTAZOLFrame.getLocalObject(frame, slot);
    }

    return CILOSTAZOLFrame.popObject(frame, slot);
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
