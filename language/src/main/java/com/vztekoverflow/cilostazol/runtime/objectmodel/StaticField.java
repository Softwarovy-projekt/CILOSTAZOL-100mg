package com.vztekoverflow.cilostazol.runtime.objectmodel;

import com.oracle.truffle.api.staticobject.StaticProperty;
import com.vztekoverflow.cilostazol.runtime.symbols.FieldSymbol;

public class StaticField extends StaticProperty {
  private final FieldSymbol symbol;

  public StaticField(FieldSymbol symbol) {
    this.symbol = symbol;
  }

  public boolean isStatic() {
    return symbol.isStatic();
  }

  public SystemTypes getKind() {
    return symbol.getKind();
  }

  public Class<?> getPropertyType() {
    return switch (symbol.getKind()) {
      case Boolean -> boolean.class;
      case Char -> char.class;
      case Float -> float.class;
      case Double -> double.class;
      case Int -> int.class;
      case Long -> long.class;
      case Array -> Object.class; // TODO: Klepitko -> arrrays will be objects
      default -> StaticObject.class;
    };
  }

  @Override
  protected String getId() {
    return symbol.getName();
  }
}
