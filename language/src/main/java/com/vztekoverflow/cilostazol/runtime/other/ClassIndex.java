package com.vztekoverflow.cilostazol.runtime.other;

import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;

public abstract class ClassIndex {
  NamedTypeSymbol symbol;
  int index;

  public ClassIndex(NamedTypeSymbol symbol, int index) {
    this.symbol = symbol;
    this.index = index;
  }

  public NamedTypeSymbol getSymbol() {
    return symbol;
  }

  public int getIndex() {
    return index;
  }
}
