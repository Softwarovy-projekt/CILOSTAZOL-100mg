package com.vztekoverflow.cilostazol.runtime.other;

import com.vztekoverflow.cil.parser.CILParserException;
import com.vztekoverflow.cil.parser.cli.CLIFileUtils;
import com.vztekoverflow.cil.parser.cli.signature.*;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.*;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.symbols.*;

public final class SymbolResolver
{
  //region assembly resolution - CLI file
  public static AssemblySymbol resolveAssembly(CLIAssemblyRefTableRow row, ModuleSymbol module)
  {
    var referencedAssemblyIdentity =
            AssemblyIdentity.fromAssemblyRefRow(
                    module.getDefiningFile().getStringHeap(),
                    row);
    return resolveAssembly(referencedAssemblyIdentity, module.getContext());
  }
  //endregion

  //region assembly resolution - other
  public static AssemblySymbol resolveAssembly(AssemblyIdentity assemblyIdentity, CILOSTAZOLContext ctx)
  {
    return ctx.findAssembly(assemblyIdentity);
  }
  //endregion

  //region type resolution - CLI file
  public static TypeSymbol resolveType(CLITablePtr row, ModuleSymbol module)
  {
    return resolveType(row, new TypeSymbol[0], new TypeSymbol[0], module);
  }

  public static TypeSymbol resolveType(CLITablePtr row, TypeSymbol[] methodTypeArgs, TypeSymbol[] typeTypeArgs, ModuleSymbol module)
  {
    return switch (row.getTableId()) {
      case CLITableConstants.CLI_TABLE_TYPE_DEF -> resolveType(module.getDefiningFile().getTableHeads().getTypeDefTableHead().skip(row), module);
      case CLITableConstants.CLI_TABLE_TYPE_REF -> resolveType(module.getDefiningFile().getTableHeads().getTypeRefTableHead().skip(row), module);
      case CLITableConstants.CLI_TABLE_TYPE_SPEC -> resolveType(module.getDefiningFile().getTableHeads().getTypeSpecTableHead().skip(row), methodTypeArgs, typeTypeArgs,module);
      default -> throw new CILParserException();
    };
  }
  public static TypeSymbol resolveType(CLITypeDefTableRow row, ModuleSymbol module)
  {
    final var nameAndNamespace = CLIFileUtils.getNameAndNamespace(module.getDefiningFile(), row);
    return resolveType(nameAndNamespace.getLeft(), nameAndNamespace.getRight(), module.getDefiningFile().getAssemblyIdentity(), module.getContext());
  }

  public static TypeSymbol resolveType(CLITypeRefTableRow row, ModuleSymbol module)
  {
    final var nameAndNamespace = CLIFileUtils.getNameAndNamespace(module.getDefiningFile(), row);

    var resolutionScope = row.getResolutionScopeTablePtr();
    AssemblyIdentity identity = switch (resolutionScope.getTableId())
    {
      case CLITableConstants.CLI_TABLE_MODULE -> module.getDefiningFile().getAssemblyIdentity();
      case CLITableConstants.CLI_TABLE_MODULE_REF -> module.getDefiningFile().getAssemblyIdentity();
      case CLITableConstants.CLI_TABLE_ASSEMBLY_REF -> AssemblyIdentity.fromAssemblyRefRow(
          module.getDefiningFile().getStringHeap(),
          module.getDefiningFile().getTableHeads().getAssemblyRefTableHead().skip(resolutionScope));
      default -> throw new CILParserException();
    };

    return resolveType(nameAndNamespace.getLeft(), nameAndNamespace.getRight(), identity, module.getContext());
  }

  private static TypeSymbol resolveType(TypeSig signature, TypeSymbol[] methodTypeArgs, TypeSymbol[] typeTypeArgs, ModuleSymbol module)
  {
    return switch (signature.getElementType()) {
      case TypeSig.ELEMENT_TYPE_CLASS, TypeSig.ELEMENT_TYPE_VALUETYPE -> resolveType(signature.getCliTablePtr(), methodTypeArgs, typeTypeArgs, module);
      case TypeSig.ELEMENT_TYPE_VAR -> typeTypeArgs[signature.getIndex()];
      case TypeSig.ELEMENT_TYPE_MVAR -> methodTypeArgs[signature.getIndex()];
      case TypeSig.ELEMENT_TYPE_GENERICINST -> {
        var genType = (NamedTypeSymbol) resolveType(signature.getCliTablePtr(), methodTypeArgs, typeTypeArgs, module);
        TypeSymbol[] typeArgs = new TypeSymbol[signature.getTypeArgs().length];
        for (int i = 0; i < typeArgs.length; i++) {
          typeArgs[i] = resolveType(signature.getTypeArgs()[i], methodTypeArgs, typeTypeArgs, module);
        }
        yield resolveType(genType, typeArgs, module.getContext());
      }
      case TypeSig.ELEMENT_TYPE_I4 -> resolveType(CILBuiltInType.Int32, module.getContext());
      case TypeSig.ELEMENT_TYPE_I8 -> resolveType(CILBuiltInType.Int64, module.getContext());
      case TypeSig.ELEMENT_TYPE_I2 -> resolveType(CILBuiltInType.Int16, module.getContext());
      case TypeSig.ELEMENT_TYPE_I1 -> resolveType(CILBuiltInType.SByte, module.getContext());
      case TypeSig.ELEMENT_TYPE_U4 -> resolveType(CILBuiltInType.UInt32, module.getContext());
      case TypeSig.ELEMENT_TYPE_U8 -> resolveType(CILBuiltInType.UInt64, module.getContext());
      case TypeSig.ELEMENT_TYPE_U2 -> resolveType(CILBuiltInType.UInt16, module.getContext());
      case TypeSig.ELEMENT_TYPE_U1 -> resolveType(CILBuiltInType.Byte, module.getContext());
      case TypeSig.ELEMENT_TYPE_R4 -> resolveType(CILBuiltInType.Single, module.getContext());
      case TypeSig.ELEMENT_TYPE_R8 -> resolveType(CILBuiltInType.Double, module.getContext());
      case TypeSig.ELEMENT_TYPE_BOOLEAN -> resolveType(CILBuiltInType.Boolean, module.getContext());
      case TypeSig.ELEMENT_TYPE_CHAR -> resolveType(CILBuiltInType.Char, module.getContext());
      case TypeSig.ELEMENT_TYPE_STRING -> resolveType(CILBuiltInType.String, module.getContext());
      case TypeSig.ELEMENT_TYPE_OBJECT -> resolveType(CILBuiltInType.Object, module.getContext());
      case TypeSig.ELEMENT_TYPE_VOID -> resolveType(CILBuiltInType.Void, module.getContext());
      case TypeSig.ELEMENT_TYPE_ARRAY -> ArrayTypeSymbol.ArrayTypeSymbolFactory.create( //TODO: cache also this type of array
              resolveType(signature.getInnerType(), methodTypeArgs, typeTypeArgs, module),
              signature.getArrayShapeSig(),
              module);
      case TypeSig.ELEMENT_TYPE_SZARRAY ->
              resolveType(
                      (NamedTypeSymbol) resolveType("Array", "System", AssemblyIdentity.SystemRuntimeLib700(), module.getContext()),
                      new TypeSymbol[]{resolveType(signature.getInnerType(), methodTypeArgs, typeTypeArgs, module)},
                      module.getContext()
              );
      default -> null;
    };
  }

  public static TypeSymbol resolveType(CLITypeSpecTableRow row, TypeSymbol[] methodTypeArgs, TypeSymbol[] typeTypeArgs, ModuleSymbol module)
  {
    TypeSig signature =
            TypeSig.read(
                    new SignatureReader(
                            row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap())));

   return resolveType(signature, methodTypeArgs, typeTypeArgs, module);
  }
  //endregion

  //region type resolution - other
  public static TypeSymbol resolveType(String name, String namespace, AssemblyIdentity assembly, CILOSTAZOLContext ctx)
  {
    return ctx.resolveType(name, namespace, assembly);
  }

  public static TypeSymbol resolveType(NamedTypeSymbol type, TypeSymbol[] typeArgs, CILOSTAZOLContext ctx)
  {
    return ctx.resolveGenericTypeInstantiation(type, typeArgs);
  }

  public enum CILBuiltInType {
    Boolean("Boolean"),
    Byte("Byte"),
    SByte("SByte"),
    Char("Char"),
    Decimal("Decimal"),
    Double("Double"),
    Single("Single"),
    Int32("Int32"),
    UInt32("UInt32"),
    IntPtr("IntPtr"),
    UIntPtr("UIntPtr"),
    Int64("Int64"),
    UInt64("UInt64"),
    Int16("Int16"),
    UInt16("UInt16"),
    Object("Object"),
    String("String"),
    Void("Void");

    public final String Name;

    CILBuiltInType(String name) {
      Name = name;
    }
  }

  public static TypeSymbol resolveType(CILBuiltInType type, CILOSTAZOLContext ctx)
  {
    return resolveType(type.Name, "System", AssemblyIdentity.SystemPrivateCoreLib700(), ctx);
  }
  //endregion

  //region field resolution - CLI file
  public static final class ClassMember<T>
  {
    public final NamedTypeSymbol symbol;
    public final T member;

    public ClassMember(NamedTypeSymbol symbol, T member) {
      this.symbol = symbol;
      this.member = member;
    }
  }

  public static ClassMember<FieldSymbol> resolveField(CLIFieldTableRow row, ModuleSymbol module)
  {
    var idx = module.getLocalField(row);
    return new ClassMember<FieldSymbol>(idx.getSymbol(), idx.getSymbol().getFields()[idx.getIndex()]);
  }

  public static ClassMember<FieldSymbol> resolveField(CLIMemberRefTableRow row, ModuleSymbol module)
  {
    var name = row.getNameHeapPtr().read(module.getDefiningFile().getStringHeap());
    var type = (NamedTypeSymbol) resolveType(row.getKlassTablePtr(), module);
    var sig = FieldSig.parse(new SignatureReader(row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap())));
    return resolveField(type, name, resolveType(sig.getType(), new TypeSymbol[0], type.getTypeArguments(), module));
  }
  //endregion

  //region field resolution - other
  public static ClassMember<FieldSymbol> resolveField(NamedTypeSymbol symbol, String fieldName, TypeSymbol fieldType)
  {
    //Note: CSharp prohibits overloading on return type, so we can ignore fieldType
    var currentType = symbol;
    while (currentType != null)
    {
      for (FieldSymbol field : currentType.getFields()) {
          if (field.getName().equals(fieldName))
            return new ClassMember<>(currentType, field);
      }
      currentType = currentType.getDirectBaseClass();
    }

    return null;
  }
  //endregion

  //region method resolution - CLI file
  public static ClassMember<MethodSymbol> resolveMethod(CLITablePtr row, TypeSymbol[] methodTypeArgs, TypeSymbol[] typeTypeArgs, ModuleSymbol module)
  {
    return switch (row.getTableId())
            {
              case CLITableConstants.CLI_TABLE_METHOD_DEF -> resolveMethod(module.getDefiningFile().getTableHeads().getMethodDefTableHead().skip(row), module);
              case CLITableConstants.CLI_TABLE_MEMBER_REF -> resolveMethod(module.getDefiningFile().getTableHeads().getMemberRefTableHead().skip(row), methodTypeArgs, typeTypeArgs, module);
              case CLITableConstants.CLI_TABLE_METHOD_SPEC -> resolveMethod(module.getDefiningFile().getTableHeads().getMethodSpecTableHead().skip(row), typeTypeArgs, module);
              default -> throw new CILParserException();
            };
  }
  public static ClassMember<MethodSymbol> resolveMethod(CLIMethodDefTableRow row, ModuleSymbol module)
  {
    var idx = module.getLocalMethod(row);
    return new ClassMember<MethodSymbol>(idx.getSymbol(), idx.getSymbol().getMethods()[idx.getIndex()]);
  }

  public static ClassMember<MethodSymbol> resolveMethod(CLIMemberRefTableRow row, TypeSymbol[] methodTypeArgs, TypeSymbol[] typeTypeArgs, ModuleSymbol module)
  {
    var name = row.getNameHeapPtr().read(module.getDefiningFile().getStringHeap());
    var type = resolveType(row.getKlassTablePtr(), module);
    var sig = MethodRefSig.parse(new SignatureReader(row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap())));
    var retType = resolveType(sig.getRetType().getTypeSig(), methodTypeArgs, typeTypeArgs, module);
    var paramTypes = new TypeSymbol[sig.getParams().length];
    for(int i = 0; i < paramTypes.length; i++) {
      paramTypes[i] = resolveType(sig.getParams()[i].getTypeSig(), methodTypeArgs, typeTypeArgs, module);
    }

    return resolveMethod(type, name, (type instanceof NamedTypeSymbol) ? ((NamedTypeSymbol)type).getTypeArguments() : new TypeSymbol[0], paramTypes,retType, sig.getGenParamCount());
  }

  public static ClassMember<MethodSymbol> resolveMethod(CLIMethodSpecTableRow row, TypeSymbol[] typeTypeArgs, ModuleSymbol module)
  {
    var signature = MethodSpecSig.read(new SignatureReader(row.getInstantiationHeapPtr().read(module.getDefiningFile().getBlobHeap())));
    TypeSymbol[] typeArgs = new TypeSymbol[signature.getGenArgCount()];
    for(int i = 0; i < typeArgs.length; i++) {
      typeArgs[i] = resolveType(signature.getTypeArgs()[i], new TypeSymbol[0], typeTypeArgs, module);
    }
    var genMethod = resolveMethod(row.getMethodTablePtr(), new TypeSymbol[0], typeTypeArgs, module);

    return new ClassMember<MethodSymbol>(genMethod.symbol, resolveMethod(genMethod.member, typeArgs, module.getContext()));
  }
  //endregion

  //region method resolution - other
  public static MethodSymbol resolveMethod(MethodSymbol genMethod, TypeSymbol[] typeArgs, CILOSTAZOLContext ctx)
  {
    return ctx.resolveGenericMethodInstantation(genMethod, typeArgs);
  }

  public static ClassMember<MethodSymbol> resolveMethod(TypeSymbol type, String methodName, TypeSymbol[] typeArgs, TypeSymbol[] parameterTypes, TypeSymbol returnType, int genParams)
  {
    //Note: CSharp prohibits overloading on return type, so we can ignore return type
    //TODO: resolving virtual methods

    var currentType = type;
    while (currentType != null)
    {
      if (currentType.isArray())
      {
        //TOD0: Arrays
      }
      else if (currentType instanceof NamedTypeSymbol n)
      {
        for (MethodSymbol method : n.getMethods()) {
          if (isCompatible(method, methodName, parameterTypes, genParams))
            return new ClassMember<MethodSymbol>(n, method);
        }
        currentType = n.getDirectBaseClass();
      }
    }

    return null;
  }

  private static boolean isCompatible(MethodSymbol method, String name, TypeSymbol[] params, int genParams)
  {
    if (!method.getName().equals(name))
      return false;

    if (genParams != method.getTypeParameters().length)
      return false;

    if (method.getParameters().length != params.length)
      return false;

    for(int i = 0; i < method.getParameters().length; i++) {
      if (!isCompatible(method.getParameters()[i].getType(), params[i]))
        return false;
    }

    return true;
  }

  private static boolean isCompatible(TypeSymbol type1, TypeSymbol type2)
  {
    return type1.isAssignableFrom(type2);
  }
  //endregion
}
