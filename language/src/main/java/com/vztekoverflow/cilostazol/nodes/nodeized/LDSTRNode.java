package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

public class LDSTRNode extends NodeizedNodeBase {
  protected final TypeSymbol stringTypeSymbol;
  protected final StaticObject value;
  private final int top;

  public LDSTRNode(String value, int top, NamedTypeSymbol stringType) {
    this.value = stringType.getContext().getAllocator().createNew(stringType);
    stringType.getInstanceFields()[0].setInt(this.value, value.length());
    stringType.getInstanceFields()[1].setObject(this.value, value.toCharArray());
    this.top = top;
    stringTypeSymbol = stringType;
  }

  @Override
  public int execute(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    CILOSTAZOLFrame.putObject(frame, top, value);
    taggedFrame[top] = stringTypeSymbol;
    return top + 1;
  }
}