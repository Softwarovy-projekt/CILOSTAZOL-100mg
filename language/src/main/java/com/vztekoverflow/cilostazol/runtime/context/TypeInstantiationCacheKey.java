package com.vztekoverflow.cilostazol.runtime.context;

import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import java.util.Arrays;
import java.util.Objects;

public record TypeInstantiationCacheKey(NamedTypeSymbol genType, TypeSymbol[] typeArgs) {
  @Override
  public String toString() {
    String result = "";

    result += genType.toString() + "<";
    result +=
        String.join(",", Arrays.stream(typeArgs).map(x -> x.toString()).toArray(String[]::new));
    result += ">";

    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(genType, Arrays.hashCode(typeArgs));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TypeInstantiationCacheKey that)) return false;
    return Objects.equals(genType, that.genType) && Arrays.equals(typeArgs, that.typeArgs);
  }
}
