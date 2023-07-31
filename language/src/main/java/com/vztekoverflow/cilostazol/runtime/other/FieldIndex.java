package com.vztekoverflow.cilostazol.runtime.other;

import com.vztekoverflow.cilostazol.runtime.symbols.FieldSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;

public class FieldIndex extends ClassIndex {
  public FieldSymbol getItem() {
    return getSymbol().getFields()[getIndex()];
  }

  public FieldIndex(NamedTypeSymbol symbol, int index) {
    super(symbol, index);
  }
}
