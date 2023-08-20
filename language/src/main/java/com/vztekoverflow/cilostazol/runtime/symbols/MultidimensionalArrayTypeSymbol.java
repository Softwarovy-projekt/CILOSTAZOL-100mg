package com.vztekoverflow.cilostazol.runtime.symbols;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;

public final class MultidimensionalArrayTypeSymbol extends ArrayTypeSymbol {

  private final NamedTypeSymbol arrayImplementation;

  private MultidimensionalArrayTypeSymbol(
      TypeSymbol elementType,
      int rank,
      int[] lengths,
      int[] lowerBounds,
      ModuleSymbol definingModule) {
    super(elementType, rank, lengths, lowerBounds, definingModule);

    this.arrayImplementation =
        (NamedTypeSymbol)
            SymbolResolver.resolveType(
                ((NamedTypeSymbol)
                    SymbolResolver.resolveType(
                        "MultidimensionalArray`1",
                        "CILOSTAZOLInternalImpl",
                        AssemblyIdentity.CILOSTAZOLInternalImpl(),
                        definingModule.getContext())),
                new TypeSymbol[] {elementType},
                definingModule.getContext());
  }

  @Override
  public MethodSymbol[] getMethods() {
    return arrayImplementation.getMethods();
  }

  @Override
  public FieldSymbol[] getFields() {
    return arrayImplementation.getFields();
  }

  public static class MultidimensionalArrayTypeSymbolFactory {
    public static MultidimensionalArrayTypeSymbol create(
        TypeSymbol elementType,
        int rank,
        int[] lengths,
        int[] lowerBounds,
        ModuleSymbol definingModule) {
      return new MultidimensionalArrayTypeSymbol(
          elementType, rank, lengths, lowerBounds, definingModule);
    }
  }
}
