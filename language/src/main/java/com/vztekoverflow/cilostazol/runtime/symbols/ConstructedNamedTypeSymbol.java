package com.vztekoverflow.cilostazol.runtime.symbols;

import com.oracle.truffle.api.CompilerDirectives;
import java.util.Arrays;

public final class ConstructedNamedTypeSymbol extends NamedTypeSymbol {
  private final NamedTypeSymbol constructedFrom;
  private final NamedTypeSymbol originalDefinition;
  private final TypeSymbol[] typeArguments;

  private ConstructedNamedTypeSymbol(
      NamedTypeSymbol constructedFrom,
      NamedTypeSymbol originalDefinition,
      TypeSymbol[] typeArguments) {
    super(
        constructedFrom.definingModule,
        constructedFrom.flags,
        constructedFrom.name,
        constructedFrom.namespace,
        constructedFrom.typeParameters,
        constructedFrom.definingRow,
        new TypeMap(constructedFrom.typeParameters, typeArguments));
    this.constructedFrom = constructedFrom;
    this.originalDefinition = originalDefinition;
    this.typeArguments = typeArguments;
  }

  @Override
  public ConstructedNamedTypeSymbol construct(TypeSymbol[] typeArguments) {
    return new ConstructedNamedTypeSymbol(originalDefinition, this, typeArguments);
  }

  @Override
  public TypeSymbol[] getTypeArguments() {
    return typeArguments;
  }

  // region Getters
  @Override
  public NamedTypeSymbol getDirectBaseClass() {
    if (lazyDirectBaseClass == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyDirectBaseClass = map.substitute(constructedFrom.getDirectBaseClass());
    }

    return lazyDirectBaseClass;
  }

  @Override
  public NamedTypeSymbol[] getInterfaces() {
    if (lazyInterfaces == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyInterfaces =
          Arrays.stream(constructedFrom.getInterfaces())
              .map(map::substitute)
              .toArray(NamedTypeSymbol[]::new);
    }

    return lazyInterfaces;
  }

  @Override
  public MethodSymbol[] getMethods() {
    if (lazyMethods == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyMethods =
          Arrays.stream(constructedFrom.getMethods())
              .map(
                  x ->
                      SubstitutedMethodSymbol.SubstitutedMethodSymbolFactory.create(
                          x.getDefinition(), x, this))
              .toArray(MethodSymbol[]::new);
    }

    return lazyMethods;
  }

  @Override
  public MethodSymbol[] getVMT() {
    if (lazyVMethodTable == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyVMethodTable =
          Arrays.stream(constructedFrom.getMethods())
              .map(
                  x ->
                      SubstitutedMethodSymbol.SubstitutedMethodSymbolFactory.create(
                          x.getDefinition(), x, this))
              .toArray(MethodSymbol[]::new);
    }

    return lazyVMethodTable;
  }

  @Override
  public FieldSymbol[] getFields() {
    if (lazyFields == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyFields =
          Arrays.stream(constructedFrom.getFields())
              .map(x -> FieldSymbol.FieldSymbolFactory.createWith(x, map.substitute(x.getType())))
              .toArray(FieldSymbol[]::new);
    }

    return lazyFields;
  }
  // endregion

  public static final class ConstructedNamedTypeSymbolFactory {
    public static ConstructedNamedTypeSymbol create(
        NamedTypeSymbol constructedFrom,
        NamedTypeSymbol originalDefinition,
        TypeSymbol[] typeArguments) {
      return new ConstructedNamedTypeSymbol(constructedFrom, originalDefinition, typeArguments);
    }
  }
}
