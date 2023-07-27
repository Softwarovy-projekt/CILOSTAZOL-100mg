package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.ArrayTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

public final class LDSTRNode extends NodeizedNodeBase {
  private final TypeSymbol stringTypeSymbol;
  private final StaticObject value;
  private final int top;

  public LDSTRNode(String value, int top, NamedTypeSymbol stringType) {
    var stringChar = value.toCharArray();

    final var charType = stringType.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Char);
    final var charArrayType =
        ArrayTypeSymbol.ArrayTypeSymbolFactory.create(charType, charType.getDefiningModule());
    final var charArray =
        stringType
            .getContext()
            .getAllocator()
            .createNewPrimitiveArray(charArrayType, stringChar.length);
    CILOSTAZOLContext.get(null).getArrayProperty().setObject(charArray, stringChar);

    this.value = stringType.getContext().getAllocator().createNew(stringType);
    stringType.getInstanceFields()[0].setInt(this.value, value.length());
    stringType.getInstanceFields()[1].setObject(this.value, charArray);
    this.top = top;
    stringTypeSymbol = stringType;
  }

  @Override
  public int execute(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    CILOSTAZOLFrame.putObject(frame, top, value);
    CILOSTAZOLFrame.putTaggedStack(taggedFrame, top, stringTypeSymbol);
    return top + 1;
  }
}
