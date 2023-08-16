package com.vztekoverflow.cilostazol.runtime.other;

import static com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol.IS_TYPE_FORWARDER_FLAG_MASK;

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
  public static NamedTypeSymbol getBoolean(CILOSTAZOLContext ctx) {
    return ctx.getBoolean();
  }

  public static NamedTypeSymbol getByte(CILOSTAZOLContext ctx) {
    return ctx.getByte();
  }

  public static NamedTypeSymbol getSByte(CILOSTAZOLContext ctx) {
    return ctx.getSByte();
  }

  public static NamedTypeSymbol getChar(CILOSTAZOLContext ctx) {
    return ctx.getChar();
  }

  public static NamedTypeSymbol getDouble(CILOSTAZOLContext ctx) {
    return ctx.getDouble();
  }

  public static NamedTypeSymbol getSingle(CILOSTAZOLContext ctx) {
    return ctx.getSingle();
  }

  public static NamedTypeSymbol getInt32(CILOSTAZOLContext ctx) {
    return ctx.getInt32();
  }

  public static NamedTypeSymbol getUInt32(CILOSTAZOLContext ctx) {
    return ctx.getUInt32();
  }

  public static NamedTypeSymbol getInt64(CILOSTAZOLContext ctx) {
    return ctx.getInt64();
  }

  public static NamedTypeSymbol getUInt64(CILOSTAZOLContext ctx) {
    return ctx.getUInt64();
  }

  public static NamedTypeSymbol getInt16(CILOSTAZOLContext ctx) {
    return ctx.getInt16();
  }

  public static NamedTypeSymbol getUInt16(CILOSTAZOLContext ctx) {
    return ctx.getUInt16();
  }

  public static NamedTypeSymbol getObject(CILOSTAZOLContext ctx) {
    return ctx.getObject();
  }

  public static NamedTypeSymbol getVoid(CILOSTAZOLContext ctx) {
    return ctx.getVoid();
  }

  public static NamedTypeSymbol getString(CILOSTAZOLContext ctx) {
    return ctx.getString();
  }

  public static NamedTypeSymbol getArray(CILOSTAZOLContext ctx) {
    return ctx.getArray();
  }
  public static NamedTypeSymbol getIntPtr(CILOSTAZOLContext ctx) {
    return ctx.getIntPtr();
  }

  public static NamedTypeSymbol getUIntPtr(CILOSTAZOLContext ctx) {
    return ctx.getUIntPtr();
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
      CLITablePtr row, ModuleSymbol module, TypeSymbol[] typeArguments) {
    return resolveType(row, new TypeSymbol[0], typeArguments, module);
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
          case CLITableConstants.CLI_TABLE_MODULE, CLITableConstants.CLI_TABLE_MODULE_REF -> module
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

  public static TypeSymbol resolveType(CLIExportedTypeTableRow row, ModuleSymbol module) {
    if (row.getImplementationTablePtr().getTableId() == CLITableConstants.CLI_TABLE_ASSEMBLY_REF
        && (row.getFlags() & IS_TYPE_FORWARDER_FLAG_MASK)
            != 0) // type is forwarded to difference assembly
    {
      var rowName = row.getTypeNameHeapPtr().read(module.getDefiningFile().getStringHeap());
      var rowNamespace =
          row.getTypeNamespaceHeapPtr().read(module.getDefiningFile().getStringHeap());

      var assemblyIdentity =
          AssemblyIdentity.fromAssemblyRefRow(
              module.getDefiningFile().getStringHeap(),
              module
                  .getDefiningFile()
                  .getTableHeads()
                  .getAssemblyRefTableHead()
                  .skip(row.getImplementationTablePtr()));

      return resolveType(rowName, rowNamespace, assemblyIdentity, module.getContext());
    }

    return null;
  }

  public static TypeSymbol resolveType(
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
      case TypeSig.ELEMENT_TYPE_U -> getUIntPtr(module.getContext());
      case TypeSig.ELEMENT_TYPE_I -> getIntPtr(module.getContext());
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
          // TODO: maybe throw NotImplemented exception as non-vector arrays are not supported
          // Partition IV 4.1.2
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

  public static ClassMember<FieldSymbol> resolveField(CLITablePtr ptr, ModuleSymbol module) {
    return switch (ptr.getTableId()) {
      case CLITableConstants.CLI_TABLE_FIELD -> resolveField(
          module.getDefiningFile().getTableHeads().getFieldTableHead().skip(ptr), module);
      case CLITableConstants.CLI_TABLE_MEMBER_REF -> resolveField(
          module.getDefiningFile().getTableHeads().getMemberRefTableHead().skip(ptr), module);
      default -> throw new CILParserException();
    };
  }

  public static ClassMember<FieldSymbol> resolveField(
      CLITablePtr ptr, TypeSymbol[] typeTypeArgs, ModuleSymbol module) {
    return switch (ptr.getTableId()) {
      case CLITableConstants.CLI_TABLE_FIELD -> resolveField(
          module.getDefiningFile().getTableHeads().getFieldTableHead().skip(ptr), module);
      case CLITableConstants.CLI_TABLE_MEMBER_REF -> resolveField(
          module.getDefiningFile().getTableHeads().getMemberRefTableHead().skip(ptr),
          typeTypeArgs,
          module);
      default -> throw new CILParserException();
    };
  }

  public static ClassMember<FieldSymbol> resolveField(CLIFieldTableRow row, ModuleSymbol module) {
    var idx = module.getLocalField(row);
    return new ClassMember<FieldSymbol>(
        idx.getSymbol(), idx.getSymbol().getFields()[idx.getIndex()]);
  }

  public static ClassMember<FieldSymbol> resolveField(
      CLIMemberRefTableRow row, ModuleSymbol module) {
    return resolveField(row, new TypeSymbol[0], module);
  }

  public static ClassMember<FieldSymbol> resolveField(
      CLIMemberRefTableRow row, TypeSymbol[] typeTypeArgs, ModuleSymbol module) {
    var name = row.getNameHeapPtr().read(module.getDefiningFile().getStringHeap());
    var type =
        (NamedTypeSymbol)
            resolveType(row.getKlassTablePtr(), new TypeSymbol[0], typeTypeArgs, module);
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
    var paramTypes = new TypeSymbol[sig.getParams().length];
    for (int i = 0; i < paramTypes.length; i++) {
      paramTypes[i] =
          resolveType(
              sig.getParams()[i].getTypeSig(),
              methodTypeArgs,
              ((NamedTypeSymbol) type).getTypeArguments(),
              module);
    }

    return resolveMethod(
        type,
        name,
        (type instanceof NamedTypeSymbol)
            ? ((NamedTypeSymbol) type).getTypeArguments()
            : new TypeSymbol[0],
        paramTypes,
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
      int genParams) {
    // Note: CSharp prohibits overloading on return type, so we can ignore return type

    var currentType = type;
    while (currentType != null) {
      if (currentType instanceof NamedTypeSymbol n) {
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
