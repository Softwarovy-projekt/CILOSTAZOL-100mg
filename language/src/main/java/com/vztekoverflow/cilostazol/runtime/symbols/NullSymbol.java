package com.vztekoverflow.cilostazol.runtime.symbols;

import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;

public final class NullSymbol extends TypeSymbol {

  public NullSymbol() {
    super(
        null,
        CILOSTAZOLFrame.StackType.Object,
        com.vztekoverflow.cilostazol.runtime.objectmodel.SystemType.Object);
  }

  @Override
  protected int getHierarchyDepth() {
    return 0;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public NamedTypeSymbol[] getInterfaces() {
    return new NamedTypeSymbol[0];
  }

  @Override
  public NamedTypeSymbol[] getSuperClasses() {
    return new NamedTypeSymbol[0];
  }
}
