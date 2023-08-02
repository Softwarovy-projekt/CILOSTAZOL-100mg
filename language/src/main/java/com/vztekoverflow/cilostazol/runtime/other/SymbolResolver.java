package com.vztekoverflow.cilostazol.runtime.other;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.CILParserException;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cil.parser.cli.CLIFileUtils;
import com.vztekoverflow.cil.parser.cli.signature.*;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.*;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.symbols.*;

public final class SymbolResolver {
  // region builtin types
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Boolean = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Byte = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol SByte = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Char = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Double = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Single = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Int32 = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol UInt32 = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Int64 = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol UInt64 = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Int16 = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol UInt16 = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Object = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Void = null;

  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol String = null;
  @CompilerDirectives.CompilationFinal private static NamedTypeSymbol Array = null;

  public static NamedTypeSymbol getBoolean(CILOSTAZOLContext ctx) {
    if (Boolean == null) {
      Boolean =
          (NamedTypeSymbol)
              resolveType("Boolean", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Boolean;
  }

  public static NamedTypeSymbol getByte(CILOSTAZOLContext ctx) {
    if (Byte == null) {
      Byte =
          (NamedTypeSymbol)
              resolveType("Byte", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Byte;
  }

  public static NamedTypeSymbol getSByte(CILOSTAZOLContext ctx) {
    if (SByte == null) {
      SByte =
          (NamedTypeSymbol)
              resolveType("SByte", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return SByte;
  }

  public static NamedTypeSymbol getChar(CILOSTAZOLContext ctx) {
    if (Char == null) {
      Char =
          (NamedTypeSymbol)
              resolveType("Char", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Char;
  }

  public static NamedTypeSymbol getDouble(CILOSTAZOLContext ctx) {
    if (Double == null) {
      Double =
          (NamedTypeSymbol)
              resolveType("Double", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Double;
  }

  public static NamedTypeSymbol getSingle(CILOSTAZOLContext ctx) {
    if (Single == null) {
      Single =
          (NamedTypeSymbol)
              resolveType("Single", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Single;
  }

  public static NamedTypeSymbol getInt32(CILOSTAZOLContext ctx) {
    if (Int32 == null) {
      Int32 =
          (NamedTypeSymbol)
              resolveType("Int32", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Int32;
  }

  public static NamedTypeSymbol getUInt32(CILOSTAZOLContext ctx) {
    if (UInt32 == null) {
      UInt32 =
          (NamedTypeSymbol)
              resolveType("UInt32", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return UInt32;
  }

  public static NamedTypeSymbol getInt64(CILOSTAZOLContext ctx) {
    if (Int64 == null) {
      Int64 =
          (NamedTypeSymbol)
              resolveType("Int64", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Int64;
  }

  public static NamedTypeSymbol getUInt64(CILOSTAZOLContext ctx) {
    if (UInt64 == null) {
      UInt64 =
          (NamedTypeSymbol)
              resolveType("UInt64", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return UInt64;
  }

  public static NamedTypeSymbol getInt16(CILOSTAZOLContext ctx) {
    if (Int16 == null) {
      Int16 =
          (NamedTypeSymbol)
              resolveType("Int16", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Int16;
  }

  public static NamedTypeSymbol getUInt16(CILOSTAZOLContext ctx) {
    if (UInt16 == null) {
      UInt16 =
          (NamedTypeSymbol)
              resolveType("UInt16", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return UInt16;
  }

  public static NamedTypeSymbol getObject(CILOSTAZOLContext ctx) {
    if (Object == null) {
      Object =
          (NamedTypeSymbol)
              resolveType("Object", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Object;
  }

  public static NamedTypeSymbol getVoid(CILOSTAZOLContext ctx) {
    if (Void == null) {
      Void =
          (NamedTypeSymbol)
              resolveType("Void", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Void;
  }

  public static NamedTypeSymbol getString(CILOSTAZOLContext ctx) {
    if (String == null) {
      String =
          (NamedTypeSymbol)
              resolveType("String", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return String;
  }

  public static NamedTypeSymbol getArray(CILOSTAZOLContext ctx) {
    if (Array == null) {
      Array =
          (NamedTypeSymbol)
              resolveType("String", "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
    }
    return Array;
  }
  // endregion

  // region assembly resolution - CLI file
  public static AssemblySymbol resolveAssembly(CLIAssemblyRefTableRow row, ModuleSymbol module) {
    var referencedAssemblyIdentity =
        AssemblyIdentity.fromAssemblyRefRow(module.getDefiningFile().getStringHeap(), row);
    return resolveAssembly(referencedAssemblyIdentity, module.getContext());
  }
  // endregion

  // region assembly resolution - other
  public static AssemblySymbol resolveAssembly(
      AssemblyIdentity assemblyIdentity, CILOSTAZOLContext ctx) {
    return ctx.findAssembly(assemblyIdentity);
  }
  // endregion

  // region type resolution - CLI file
  public static TypeSymbol resolveType(CLITablePtr row, ModuleSymbol module) {
    return resolveType(row, new TypeSymbol[0], new TypeSymbol[0], module);
  }

  public static TypeSymbol resolveType(
      CLITablePtr row,
      TypeSymbol[] methodTypeArgs,
      TypeSymbol[] typeTypeArgs,
      ModuleSymbol module) {
    return switch (row.getTableId()) {
      case CLITableConstants.CLI_TABLE_TYPE_DEF -> resolveType(
          module.getDefiningFile().getTableHeads().getTypeDefTableHead().skip(row), module);
      case CLITableConstants.CLI_TABLE_TYPE_REF -> resolveType(
          module.getDefiningFile().getTableHeads().getTypeRefTableHead().skip(row), module);
      case CLITableConstants.CLI_TABLE_TYPE_SPEC -> resolveType(
          module.getDefiningFile().getTableHeads().getTypeSpecTableHead().skip(row),
          methodTypeArgs,
          typeTypeArgs,
          module);
      default -> throw new CILParserException();
    };
  }

  public static TypeSymbol resolveType(CLITypeDefTableRow row, ModuleSymbol module) {
    final var nameAndNamespace = CLIFileUtils.getNameAndNamespace(module.getDefiningFile(), row);
    return resolveType(
        nameAndNamespace.getLeft(),
        nameAndNamespace.getRight(),
        module.getDefiningFile().getAssemblyIdentity(),
        module.getContext());
  }

  public static TypeSymbol resolveType(CLITypeRefTableRow row, ModuleSymbol module) {
    final var nameAndNamespace = CLIFileUtils.getNameAndNamespace(module.getDefiningFile(), row);

    var resolutionScope = row.getResolutionScopeTablePtr();
    AssemblyIdentity identity =
        switch (resolutionScope.getTableId()) {
          case CLITableConstants.CLI_TABLE_MODULE -> module.getDefiningFile().getAssemblyIdentity();
          case CLITableConstants.CLI_TABLE_MODULE_REF -> module
              .getDefiningFile()
              .getAssemblyIdentity();
          case CLITableConstants.CLI_TABLE_ASSEMBLY_REF -> AssemblyIdentity.fromAssemblyRefRow(
              module.getDefiningFile().getStringHeap(),
              module
                  .getDefiningFile()
                  .getTableHeads()
                  .getAssemblyRefTableHead()
                  .skip(resolutionScope));
          default -> throw new CILParserException();
        };

    return resolveType(
        nameAndNamespace.getLeft(), nameAndNamespace.getRight(), identity, module.getContext());
  }

  private static TypeSymbol resolveType(
      TypeSig signature,
      TypeSymbol[] methodTypeArgs,
      TypeSymbol[] typeTypeArgs,
      ModuleSymbol module) {
    return switch (signature.getElementType()) {
      case TypeSig.ELEMENT_TYPE_CLASS, TypeSig.ELEMENT_TYPE_VALUETYPE -> resolveType(
          signature.getCliTablePtr(), methodTypeArgs, typeTypeArgs, module);
      case TypeSig.ELEMENT_TYPE_VAR -> typeTypeArgs[signature.getIndex()];
      case TypeSig.ELEMENT_TYPE_MVAR -> methodTypeArgs[signature.getIndex()];
      case TypeSig.ELEMENT_TYPE_GENERICINST -> {
        var genType =
            (NamedTypeSymbol)
                resolveType(signature.getCliTablePtr(), methodTypeArgs, typeTypeArgs, module);
        TypeSymbol[] typeArgs = new TypeSymbol[signature.getTypeArgs().length];
        for (int i = 0; i < typeArgs.length; i++) {
          typeArgs[i] =
              resolveType(signature.getTypeArgs()[i], methodTypeArgs, typeTypeArgs, module);
        }
        yield resolveType(genType, typeArgs, module.getContext());
      }
      case TypeSig.ELEMENT_TYPE_I4 -> getInt32(module.getContext());
      case TypeSig.ELEMENT_TYPE_I8 -> getInt64(module.getContext());
      case TypeSig.ELEMENT_TYPE_I2 -> getInt16(module.getContext());
      case TypeSig.ELEMENT_TYPE_I1 -> getSByte(module.getContext());
      case TypeSig.ELEMENT_TYPE_U4 -> getUInt32(module.getContext());
      case TypeSig.ELEMENT_TYPE_U8 -> getUInt64(module.getContext());
      case TypeSig.ELEMENT_TYPE_U2 -> getUInt16(module.getContext());
      case TypeSig.ELEMENT_TYPE_U1 -> getByte(module.getContext());
      case TypeSig.ELEMENT_TYPE_R4 -> getSingle(module.getContext());
      case TypeSig.ELEMENT_TYPE_R8 -> getDouble(module.getContext());
      case TypeSig.ELEMENT_TYPE_BOOLEAN -> getBoolean(module.getContext());
      case TypeSig.ELEMENT_TYPE_CHAR -> getChar(module.getContext());
      case TypeSig.ELEMENT_TYPE_OBJECT, TypeSig.ELEMENT_TYPE_STRING -> getObject(
          module.getContext());
      case TypeSig.ELEMENT_TYPE_VOID -> getVoid(module.getContext());
      case TypeSig.ELEMENT_TYPE_ARRAY -> {
        var shapeSig = signature.getArrayShapeSig();
        if (shapeSig.lengths().length == 0 && shapeSig.lowerBounds().length == 0)
          yield resolveArray(
              resolveType(signature.getInnerType(), methodTypeArgs, typeTypeArgs, module),
              shapeSig.rank(),
              module.getContext());
        else
          yield ArrayTypeSymbol.ArrayTypeSymbolFactory.create(
              resolveType(signature.getInnerType(), methodTypeArgs, typeTypeArgs, module),
              shapeSig,
              module);
      }
      case TypeSig.ELEMENT_TYPE_SZARRAY -> resolveArray(
          resolveType(signature.getInnerType(), methodTypeArgs, typeTypeArgs, module),
          1,
          module.getContext());
      default -> null;
    };
  }

  public static TypeSymbol resolveType(
      CLITypeSpecTableRow row,
      TypeSymbol[] methodTypeArgs,
      TypeSymbol[] typeTypeArgs,
      ModuleSymbol module) {
    TypeSig signature =
        TypeSig.read(
            new SignatureReader(
                row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap())));

    return resolveType(signature, methodTypeArgs, typeTypeArgs, module);
  }
  // endregion

  // region type resolution - other
  public static ArrayTypeSymbol resolveArray(TypeSymbol elemType, int rank, CILOSTAZOLContext ctx) {
    return ctx.resolveArray(elemType, rank);
  }

  public static TypeSymbol resolveType(
      String name, String namespace, AssemblyIdentity assembly, CILOSTAZOLContext ctx) {
    return ctx.resolveType(name, namespace, assembly);
  }

  public static TypeSymbol resolveType(
      NamedTypeSymbol type, TypeSymbol[] typeArgs, CILOSTAZOLContext ctx) {
    return ctx.resolveGenericTypeInstantiation(type, typeArgs);
  }

  // endregion

  // region field resolution - CLI file
  public static final class ClassMember<T> {
    public final NamedTypeSymbol symbol;
    public final T member;

    public ClassMember(NamedTypeSymbol symbol, T member) {
      this.symbol = symbol;
      this.member = member;
    }
  }

  public static ClassMember<FieldSymbol> resolveField(CLIFieldTableRow row, ModuleSymbol module) {
    var idx = module.getLocalField(row);
    return new ClassMember<FieldSymbol>(
        idx.getSymbol(), idx.getSymbol().getFields()[idx.getIndex()]);
  }

  public static ClassMember<FieldSymbol> resolveField(
      CLIMemberRefTableRow row, ModuleSymbol module) {
    var name = row.getNameHeapPtr().read(module.getDefiningFile().getStringHeap());
    var type = (NamedTypeSymbol) resolveType(row.getKlassTablePtr(), module);
    var sig =
        FieldSig.parse(
            new SignatureReader(
                row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap())));
    return resolveField(
        type, name, resolveType(sig.getType(), new TypeSymbol[0], type.getTypeArguments(), module));
  }
  // endregion

  // region field resolution - other
  public static ClassMember<FieldSymbol> resolveField(
      NamedTypeSymbol symbol, String fieldName, TypeSymbol fieldType) {
    // Note: CSharp prohibits overloading on return type, so we can ignore fieldType
    var currentType = symbol;
    while (currentType != null) {
      for (FieldSymbol field : currentType.getFields()) {
        if (field.getName().equals(fieldName)) return new ClassMember<>(currentType, field);
      }
      currentType = currentType.getDirectBaseClass();
    }

    return null;
  }
  // endregion

  // region method resolution - CLI file
  public static ClassMember<MethodSymbol> resolveMethod(CLITablePtr row, ModuleSymbol module) {
    return resolveMethod(row, new TypeSymbol[0], new TypeSymbol[0], module);
  }

  public static ClassMember<MethodSymbol> resolveMethod(
      CLITablePtr row,
      TypeSymbol[] methodTypeArgs,
      TypeSymbol[] typeTypeArgs,
      ModuleSymbol module) {
    return switch (row.getTableId()) {
      case CLITableConstants.CLI_TABLE_METHOD_DEF -> resolveMethod(
          module.getDefiningFile().getTableHeads().getMethodDefTableHead().skip(row), module);
      case CLITableConstants.CLI_TABLE_MEMBER_REF -> resolveMethod(
          module.getDefiningFile().getTableHeads().getMemberRefTableHead().skip(row),
          methodTypeArgs,
          typeTypeArgs,
          module);
      case CLITableConstants.CLI_TABLE_METHOD_SPEC -> resolveMethod(
          module.getDefiningFile().getTableHeads().getMethodSpecTableHead().skip(row),
          typeTypeArgs,
          module);
      default -> throw new CILParserException();
    };
  }

  public static ClassMember<MethodSymbol> resolveMethod(
      CLIMethodDefTableRow row, ModuleSymbol module) {
    var idx = module.getLocalMethod(row);
    return new ClassMember<MethodSymbol>(idx.getSymbol(), idx.getItem());
  }

  public static ClassMember<MethodSymbol> resolveMethod(
      CLIMemberRefTableRow row,
      TypeSymbol[] methodTypeArgs,
      TypeSymbol[] typeTypeArgs,
      ModuleSymbol module) {
    var name = row.getNameHeapPtr().read(module.getDefiningFile().getStringHeap());
    var type = resolveType(row.getKlassTablePtr(), module);
    var sig =
        MethodRefSig.parse(
            new SignatureReader(
                row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap())));
    var retType = resolveType(sig.getRetType().getTypeSig(), methodTypeArgs, typeTypeArgs, module);
    var paramTypes = new TypeSymbol[sig.getParams().length];
    for (int i = 0; i < paramTypes.length; i++) {
      paramTypes[i] =
          resolveType(sig.getParams()[i].getTypeSig(), methodTypeArgs, typeTypeArgs, module);
    }

    return resolveMethod(
        type,
        name,
        (type instanceof NamedTypeSymbol)
            ? ((NamedTypeSymbol) type).getTypeArguments()
            : new TypeSymbol[0],
        paramTypes,
        retType,
        sig.getGenParamCount());
  }

  public static ClassMember<MethodSymbol> resolveMethod(
      CLIMethodSpecTableRow row, TypeSymbol[] typeTypeArgs, ModuleSymbol module) {
    var signature =
        MethodSpecSig.read(
            new SignatureReader(
                row.getInstantiationHeapPtr().read(module.getDefiningFile().getBlobHeap())));
    TypeSymbol[] typeArgs = new TypeSymbol[signature.getGenArgCount()];
    for (int i = 0; i < typeArgs.length; i++) {
      typeArgs[i] =
          resolveType(signature.getTypeArgs()[i], new TypeSymbol[0], typeTypeArgs, module);
    }
    var genMethod = resolveMethod(row.getMethodTablePtr(), new TypeSymbol[0], typeTypeArgs, module);

    return new ClassMember<MethodSymbol>(
        genMethod.symbol, resolveMethod(genMethod.member, typeArgs, module.getContext()));
  }
  // endregion

  // region method resolution - other
  public static MethodSymbol resolveMethod(
      MethodSymbol genMethod, TypeSymbol[] typeArgs, CILOSTAZOLContext ctx) {
    return ctx.resolveGenericMethodInstantiation(genMethod, typeArgs);
  }

  public static ClassMember<MethodSymbol> resolveMethod(
      TypeSymbol type,
      String methodName,
      TypeSymbol[] typeArgs,
      TypeSymbol[] parameterTypes,
      TypeSymbol returnType,
      int genParams) {
    // Note: CSharp prohibits overloading on return type, so we can ignore return type
    // TODO: resolving virtual methods

    var currentType = type;
    while (currentType != null) {
      if (currentType.isArray()) {
        // TOD0: Arrays
      } else if (currentType instanceof NamedTypeSymbol n) {
        for (MethodSymbol method : n.getMethods()) {
          if (isCompatible(method, methodName, parameterTypes, genParams))
            return new ClassMember<MethodSymbol>(n, method);
        }
        currentType = n.getDirectBaseClass();
      }
    }

    return null;
  }

  private static boolean isCompatible(
      MethodSymbol method, String name, TypeSymbol[] params, int genParams) {
    if (!method.getName().equals(name)) return false;

    if (genParams != method.getTypeParameters().length) return false;

    if (method.getParameters().length != params.length) return false;

    for (int i = 0; i < method.getParameters().length; i++) {
      if (!isCompatible(method.getParameters()[i].getType(), params[i])) return false;
    }

    return true;
  }

  private static boolean isCompatible(TypeSymbol type1, TypeSymbol type2) {
    return type1.isAssignableFrom(type2);
  }
  // endregion

  // region references
  public static ReferenceSymbol resolveReference(
      ReferenceSymbol.ReferenceType type, CILOSTAZOLContext ctx) {
    return ctx.resolveReference(type);
  }
  // endregion
}
