package com.vztekoverflow.cilostazol.runtime.other;

import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;

public record TypeSymbolPtrCacheKey(CLITablePtr ptr) {

  @Override
  public String toString() {
    return ptr.toString();
  }

  @Override
  public int hashCode() {
    return ptr.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TypeSymbolPtrCacheKey that)) return false;
    return ptr.equals(that.ptr);
  }
}
