package com.vztekoverflow.cilostazol.runtime.symbols;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cil.parser.cli.signature.ArrayShapeSig;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;

public final class ArrayTypeSymbol extends NamedTypeSymbol {
  private final TypeSymbol elementType;
  private final NamedTypeSymbol arrayType;
  private final int rank;
  private final int[] lengths;
  private final int[] lowerBounds;

  private ArrayTypeSymbol(
      TypeSymbol elementType,
      int rank,
      int[] lengths,
      int[] lowerBounds,
      ModuleSymbol definingModule) {
    this(
        elementType,
        rank,
        lengths,
        lowerBounds,
        definingModule,
        (NamedTypeSymbol)
            SymbolResolver.resolveType(
                "Array",
                "System",
                AssemblyIdentity.SystemPrivateCoreLib700(),
                definingModule.getContext()));
  }

  private ArrayTypeSymbol(
      TypeSymbol elementType,
      int rank,
      int[] lengths,
      int[] lowerBounds,
      ModuleSymbol definingModule,
      NamedTypeSymbol arrayType) {
    super(
        definingModule,
        1056897,
        "Array",
        "System",
        arrayType.getTypeParameters(),
        arrayType.definingRow,
        arrayType.map);
    this.elementType = elementType;
    this.rank = rank;
    this.lengths = lengths;
    this.lowerBounds = lowerBounds;
    this.arrayType = arrayType;
  }

  public TypeSymbol getElementType() {
    return elementType;
  }

  public int getRank() {
    return rank;
  }

  public int[] getLengths() {
    return lengths;
  }

  public int[] getLowerBounds() {
    return lowerBounds;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  protected int getHierarchyDepth() {
    return arrayType.getHierarchyDepth();
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public NamedTypeSymbol[] getInterfaces() {
    return arrayType.getInterfaces();
  }

  @Override
  public NamedTypeSymbol[] getSuperClasses() {
    return arrayType.getSuperClasses();
  }

  public static class ArrayTypeSymbolFactory {
    public static ArrayTypeSymbol create(
        TypeSymbol elementType, ArrayShapeSig arrayShapeSig, ModuleSymbol definingModule) {
      return new ArrayTypeSymbol(
          elementType,
          arrayShapeSig.rank(),
          arrayShapeSig.lengths(),
          arrayShapeSig.lowerBounds(),
          definingModule);
    }

    public static ArrayTypeSymbol create(
        TypeSymbol elementType, int rank, ModuleSymbol definingModule) {
      return new ArrayTypeSymbol(elementType, rank, new int[0], new int[0], definingModule);
    }

    public static ArrayTypeSymbol create(TypeSymbol elementType, ModuleSymbol definingModule) {
      return new ArrayTypeSymbol(elementType, 1, new int[0], new int[0], definingModule);
    }
  }
}
