package com.vztekoverflow.cilostazol.runtime.context;

import com.vztekoverflow.cilostazol.runtime.symbols.ReferenceSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import java.util.Objects;

public record ReferenceCacheKey(ReferenceSymbol.ReferenceType referenceType, TypeSymbol underlyingType) {
  @Override
  public String toString() {
    return  "ref " + underlyingType.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(referenceType, underlyingType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ReferenceCacheKey that)) return false;
    return Objects.equals(referenceType, that.referenceType) && Objects.equals(underlyingType, that.underlyingType);
  }
}
