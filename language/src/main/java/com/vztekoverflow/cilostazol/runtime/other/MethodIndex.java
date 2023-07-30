package com.vztekoverflow.cilostazol.runtime.other;

import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;

public class MethodIndex extends ClassIndex {
  public MethodSymbol getItem() {
    return getSymbol().getMethods()[getIndex()];
  }

  public MethodIndex(NamedTypeSymbol symbol, int index) {
    super(symbol, index);
  }
}
