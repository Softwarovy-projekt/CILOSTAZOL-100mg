package com.vztekoverflow.cilostazol.runtime.other;

import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;

import java.util.Arrays;
import java.util.Objects;

public record MethodInstantiationCacheKey(MethodSymbol genMethod, TypeSymbol[] typeArgs) {
  @Override
  public String toString() {
    String result = "";

    result += genMethod.toString() + "<";
    result += String.join(",", Arrays.stream(typeArgs).map(x -> x.toString()).toArray(String[]::new));
    result += ">";

    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(genMethod, Arrays.hashCode(typeArgs));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MethodInstantiationCacheKey that)) return false;
    return Objects.equals(genMethod, that.genMethod)
            && Arrays.equals(typeArgs, that.typeArgs);
  }
}
