package com.vztekoverflow.cilostazol.runtime.symbols;

import com.oracle.truffle.api.CompilerDirectives;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
  public boolean isClosed() {
    return Arrays.stream(typeArguments).allMatch(TypeSymbol::isClosed);
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
      var baseClass = constructedFrom.getDirectBaseClass();
      if (baseClass == null) {
        lazyDirectBaseClass = null;
      } else {
        lazyDirectBaseClass = map.substitute(constructedFrom.getDirectBaseClass());
      }
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
  public Map<MethodSymbol, MethodSymbol> getMethodsImpl() {
    if (lazyMethodImpl == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyMethodImpl =
          constructedFrom.getMethodsImpl().entrySet().stream()
              .collect(
                  Collectors.toMap(
                      x ->
                          SubstitutedMethodSymbol.SubstitutedMethodSymbolFactory.create(
                              x.getKey().getDefinition(), x.getKey(), this),
                      x ->
                          SubstitutedMethodSymbol.SubstitutedMethodSymbolFactory.create(
                              x.getValue().getDefinition(), x.getValue(), this)));
    }

    return lazyMethodImpl;
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

  @Override
  public String toString() {
    return super.toString() + "<" + Arrays.stream(typeArguments)
            .map(TypeSymbol::toString)
            .collect(Collectors.joining(", ")) + ">";
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
