package com.vztekoverflow.cilostazol.runtime.context;

import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import java.util.Objects;

public record ArrayCacheKey(TypeSymbol elemType, int rank) {
  @Override
  public String toString() {
    return elemType.toString() + "[" + rank + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(elemType, rank);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArrayCacheKey that)) return false;
    return Objects.equals(elemType, that.elemType) && rank == that.rank;
  }
}

