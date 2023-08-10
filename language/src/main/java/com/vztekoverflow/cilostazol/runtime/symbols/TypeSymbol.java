package com.vztekoverflow.cilostazol.runtime.symbols;

import static com.vztekoverflow.cilostazol.runtime.symbols.ArrayTypeSymbol.*;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cil.parser.cli.signature.SignatureReader;
import com.vztekoverflow.cil.parser.cli.signature.TypeSig;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITypeSpecTableRow;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.exceptions.TypeSystemException;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;
import com.vztekoverflow.cilostazol.runtime.objectmodel.SystemType;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
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
  //  {
  //    return false;
  //  }

  public abstract boolean isArray();
  //  {
  //    return false;
  //  }

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

  public static final class TypeSymbolFactory {
    public static TypeSymbol create(
        TypeSig typeSig, TypeSymbol[] mvars, TypeSymbol[] vars, ModuleSymbol module) {
      return switch (typeSig.getElementType()) {
        case TypeSig.ELEMENT_TYPE_CLASS, TypeSig.ELEMENT_TYPE_VALUETYPE -> create(
            typeSig.getCliTablePtr(), mvars, vars, module);
        case TypeSig.ELEMENT_TYPE_VAR -> vars[typeSig.getIndex()];
        case TypeSig.ELEMENT_TYPE_MVAR -> mvars[typeSig.getIndex()];
        case TypeSig.ELEMENT_TYPE_GENERICINST -> {
          var genType = (NamedTypeSymbol) create(typeSig.getCliTablePtr(), mvars, vars, module);
          TypeSymbol[] typeArgs = new TypeSymbol[typeSig.getTypeArgs().length];
          for (int i = 0; i < typeArgs.length; i++) {
            typeArgs[i] = create(typeSig.getTypeArgs()[i], mvars, vars, module);
          }
          yield genType.construct(typeArgs);
        }
        case TypeSig.ELEMENT_TYPE_I4 -> SymbolResolver.getInt32(module.getContext());
        case TypeSig.ELEMENT_TYPE_I8 -> SymbolResolver.getInt64(module.getContext());
        case TypeSig.ELEMENT_TYPE_I2 -> SymbolResolver.getInt16(module.getContext());
        case TypeSig.ELEMENT_TYPE_I1 -> SymbolResolver.getSByte(module.getContext());
        case TypeSig.ELEMENT_TYPE_U4 -> SymbolResolver.getUInt32(module.getContext());
        case TypeSig.ELEMENT_TYPE_U8 -> SymbolResolver.getUInt64(module.getContext());
        case TypeSig.ELEMENT_TYPE_U2 -> SymbolResolver.getUInt16(module.getContext());
        case TypeSig.ELEMENT_TYPE_U1 -> SymbolResolver.getByte(module.getContext());
        case TypeSig.ELEMENT_TYPE_R4 -> SymbolResolver.getSingle(module.getContext());
        case TypeSig.ELEMENT_TYPE_R8 -> SymbolResolver.getDouble(module.getContext());
        case TypeSig.ELEMENT_TYPE_BOOLEAN -> SymbolResolver.getBoolean(module.getContext());
        case TypeSig.ELEMENT_TYPE_CHAR -> SymbolResolver.getChar(module.getContext());
        case TypeSig.ELEMENT_TYPE_OBJECT, TypeSig.ELEMENT_TYPE_STRING -> SymbolResolver.getObject(
            module.getContext());
        case TypeSig.ELEMENT_TYPE_VOID -> SymbolResolver.getVoid(module.getContext());
        case TypeSig.ELEMENT_TYPE_ARRAY -> ArrayTypeSymbolFactory.create(
            create(typeSig.getInnerType(), mvars, vars, module),
            typeSig.getArrayShapeSig(),
            module);
        case TypeSig.ELEMENT_TYPE_SZARRAY -> ArrayTypeSymbolFactory.create(
            create(typeSig.getInnerType(), mvars, vars, module), module);
        default -> null;
      };
    }

    public static TypeSymbol create(
        CLITablePtr ptr, TypeSymbol[] mvars, TypeSymbol[] vars, ModuleSymbol module) {
      return switch (ptr.getTableId()) {
        case CLITableConstants.CLI_TABLE_TYPE_DEF -> NamedTypeSymbol.NamedTypeSymbolFactory.create(
            module.getDefiningFile().getTableHeads().getTypeDefTableHead().skip(ptr), module);
        case CLITableConstants.CLI_TABLE_TYPE_REF -> NamedTypeSymbol.NamedTypeSymbolFactory.create(
            module.getDefiningFile().getTableHeads().getTypeRefTableHead().skip(ptr), module);
        case CLITableConstants.CLI_TABLE_TYPE_SPEC -> create(
            module.getDefiningFile().getTableHeads().getTypeSpecTableHead().skip(ptr),
            mvars,
            vars,
            module);
        default -> throw new TypeSystemException(
            CILOSTAZOLBundle.message("cilostazol.exception.constructor.withoutDefType"));
      };
    }

    public static TypeSymbol create(
        CLITypeSpecTableRow row, TypeSymbol[] mvars, TypeSymbol[] vars, ModuleSymbol module) {
      TypeSig signature =
          TypeSig.read(
              new SignatureReader(
                  row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap())));
      return TypeSymbol.TypeSymbolFactory.create(signature, mvars, vars, module);
    }
  }
}
