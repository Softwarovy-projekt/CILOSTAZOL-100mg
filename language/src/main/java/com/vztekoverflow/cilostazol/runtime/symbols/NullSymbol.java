package com.vztekoverflow.cilostazol.runtime.symbols;

import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;

public final class NullSymbol extends TypeSymbol {

  public NullSymbol() {
    super(
        null,
        CILOSTAZOLFrame.StackType.Object,
        com.vztekoverflow.cilostazol.runtime.objectmodel.SystemType.Object);
  }
}
