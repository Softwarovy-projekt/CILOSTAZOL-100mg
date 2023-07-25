package com.vztekoverflow.cilostazol.runtime.other;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import java.util.Objects;

public record TypeSymbolCacheKey(String name, String namespace, AssemblyIdentity assemblyIdentity) {

  @Override
  public String toString() {
    return namespace + "." + name + " in " + assemblyIdentity;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name.hashCode(), namespace.hashCode(), assemblyIdentity.hashCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TypeSymbolCacheKey that)) return false;
    return Objects.equals(name, that.name)
        && Objects.equals(namespace, that.namespace)
        && Objects.equals(assemblyIdentity, that.assemblyIdentity);
  }
}
