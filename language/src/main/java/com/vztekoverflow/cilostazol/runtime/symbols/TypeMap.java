package com.vztekoverflow.cilostazol.runtime.symbols;

import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import java.util.Arrays;
import java.util.HashMap;

public class TypeMap {
  private final HashMap<TypeParameterSymbol, TypeSymbol> map;

  public TypeMap(TypeParameterSymbol[] from, TypeSymbol[] to) {
    this.map = new HashMap<>();
    for (int i = 0; i < from.length; i++) {
      map.put(from[i], to[i]);
    }
  }

  public TypeMap(TypeMap map, TypeParameterSymbol[] from, TypeSymbol[] to) {
    this.map = new HashMap<>();
    this.map.putAll(map.map);
    for (int i = 0; i < from.length; i++) {
      this.map.put(from[i], to[i]);
    }
  }

  private TypeSymbol substituteTypeParameter(TypeParameterSymbol symbol) {
    return map.getOrDefault(symbol, symbol);
  }

  private NamedTypeSymbol substituteNamedTypeSymbol(NamedTypeSymbol symbol) {
    if (symbol.isClosed()) return symbol;

    var typeArgs = symbol.getTypeArguments();
    var replacedTypeArgs = Arrays.stream(typeArgs).map(this::substitute).toArray(TypeSymbol[]::new);
    return (NamedTypeSymbol)
        SymbolResolver.resolveType(symbol, replacedTypeArgs, symbol.getContext());
  }

  /**
   * Substitute all type parameters in the given symbol. Use specific versions when possible: {@link
   * #substitute(NamedTypeSymbol)} or {@link #substitute(TypeParameterSymbol)}.
   *
   * @param symbol where to substitute
   * @return substituted symbol
   */
  public TypeSymbol substitute(TypeSymbol symbol) {
    if (symbol == null) return null;

    if (symbol instanceof TypeParameterSymbol typeParameterSymbol) {
      return substituteTypeParameter(typeParameterSymbol);
    } else if (symbol instanceof ArrayTypeSymbol arrayTypeSymbol) {
      return SymbolResolver.resolveArray(
          substitute(arrayTypeSymbol.getElementType()),
          arrayTypeSymbol.getRank(),
          arrayTypeSymbol.getLengths(),
          arrayTypeSymbol.getLowerBounds(),
          arrayTypeSymbol.getContext());
    } else {
      return substituteNamedTypeSymbol((NamedTypeSymbol) symbol);
    }
  }

  public NamedTypeSymbol substitute(NamedTypeSymbol symbol) {
    return substituteNamedTypeSymbol(symbol);
  }

  public TypeSymbol substitute(TypeParameterSymbol symbol) {
    return substituteTypeParameter(symbol);
  }
}
