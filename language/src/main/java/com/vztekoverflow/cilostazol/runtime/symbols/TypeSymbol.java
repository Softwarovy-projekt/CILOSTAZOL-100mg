package com.vztekoverflow.cilostazol.runtime.symbols;

import static com.vztekoverflow.cilostazol.runtime.symbols.ArrayTypeSymbol.*;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;
import com.vztekoverflow.cilostazol.runtime.objectmodel.SystemType;
import java.util.Arrays;

public abstract class TypeSymbol extends Symbol {
  protected final ModuleSymbol definingModule;
  private final CILOSTAZOLFrame.StackType stackTypeKind;
  private final SystemType systemType;

  public TypeSymbol(
      ModuleSymbol definingModule,
      CILOSTAZOLFrame.StackType stackTypeKind,
      SystemType staticObjType) {
    super(ContextProviderImpl.getInstance());
    this.definingModule = definingModule;
    this.stackTypeKind = stackTypeKind;
    this.systemType = staticObjType;
  }

  public AssemblyIdentity getAssemblyIdentity() {
    return definingModule.getDefiningFile().getAssemblyIdentity();
  }

  // region SOM
  protected static int fastLookup(TypeSymbol target, TypeSymbol[] types) {
    if (!CompilerDirectives.isPartialEvaluationConstant(types)) {
      return fastLookupBoundary(target, types);
    }
    // PE-friendly.
    CompilerAsserts.partialEvaluationConstant(types);
    return fastLookupImpl(target, types);
  }

  @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_EXPLODE_UNTIL_RETURN)
  protected static int fastLookupImpl(TypeSymbol target, TypeSymbol[] types) {
    for (int i = 0; i < types.length; i++) {
      if (types[i] == target) {
        return i;
      }
    }

    return -1;
  }

  @CompilerDirectives.TruffleBoundary(allowInlining = true)
  protected static int fastLookupBoundary(TypeSymbol target, TypeSymbol[] types) {
    return fastLookupImpl(target, types);
  }

  public boolean isAssignableFrom(TypeSymbol other) {
    if (this == other) return true;

    // Partition I: 8.7.1 Assignment compatibility for signature types
    if (this.isArray() && other.isArray()) {
      ArrayTypeSymbol thisArray = (ArrayTypeSymbol) this;
      ArrayTypeSymbol otherArray = (ArrayTypeSymbol) other;
      if (thisArray.getRank() == otherArray.getRank()) {
        return thisArray.getElementType().isAssignableFrom(otherArray.getElementType());
      }
      return false;
    }
    // Both array case handled above
    if (this.isArray() || other.isArray()) {
      return false;
    }

    if (this.isInterface()) {
      return checkInterfaceSubclassing(other);
    }
    return checkOrdinaryClassSubclassing(other);
  }

  /**
   * Performs type checking for non-interface, non-array classes.
   *
   * @param other the class whose type is to be checked against {@code this}
   * @return true if {@code other} is a subclass of {@code this}
   */
  public boolean checkOrdinaryClassSubclassing(TypeSymbol other) {
    int depth = getHierarchyDepth();
    return other.getHierarchyDepth() >= depth && other.getSuperClasses()[depth] == this;
  }

  /**
   * Performs type checking for interface classes.
   *
   * @param other the class whose type is to be checked against {@code this}
   * @return true if {@code this} is a super interface of {@code other}
   */
  public boolean checkInterfaceSubclassing(TypeSymbol other) {
    NamedTypeSymbol[] interfaces = other.getInterfaces();
    return fastLookup(this, interfaces) >= 0;
  }

  protected abstract int getHierarchyDepth();
  // endregion

  public ModuleSymbol getDefiningModule() {
    return definingModule;
  }

  public abstract boolean isInterface();

  public abstract boolean isArray();

  public boolean isCovariantTo(TypeSymbol other) {
    return this.equals(other) || Arrays.asList(getSuperClasses()).contains(other);
  }

  public abstract NamedTypeSymbol[] getInterfaces();

  public abstract NamedTypeSymbol[] getSuperClasses();

  public SystemType getSystemType() {
    return systemType;
  }

  public CILOSTAZOLFrame.StackType getStackTypeKind() {
    return stackTypeKind;
  }
}
