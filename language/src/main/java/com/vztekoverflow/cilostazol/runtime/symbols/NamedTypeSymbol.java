package com.vztekoverflow.cilostazol.runtime.symbols;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.staticobject.StaticShape;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cil.parser.cli.CLIFileUtils;
import com.vztekoverflow.cil.parser.cli.signature.ElementTypeFlag;
import com.vztekoverflow.cil.parser.cli.signature.TypeSpecSig;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.*;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.exceptions.InvalidCLIException;
import com.vztekoverflow.cilostazol.exceptions.NotImplementedException;
import com.vztekoverflow.cilostazol.exceptions.TypeSystemException;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.nodes.nodeized.CALLNode;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.LinkedFieldLayout;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticField;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.objectmodel.SystemType;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import java.util.*;

public class NamedTypeSymbol extends TypeSymbol {
  // region constants
  private static final int ABSTRACT_FLAG_MASK = 0x80;
  private static final int SEALED_FLAG_MASK = 0x100;
  private static final int SPECIAL_NAME_FLAG_MASK = 0x400;
  private static final int IMPORT_FLAG_MASK = 0x1000;
  private static final int SERIALIZABLE_FLAG_MASK = 0x2000;
  private static final int BEFORE_FIELD_INIT_FLAG_MASK = 0x100000;
  private static final int RT_SPECIAL_NAME_FLAG_MASK = 0x800;
  private static final int HAS_SECURITY_FLAG_MASK = 0x40000;
  private static final int IS_TYPE_FORWARDER_FLAG_MASK = 0x200000;
  // endregion

  protected final int flags;
  protected final String name;
  protected final String namespace;
  protected final TypeParameterSymbol[] typeParameters;
  protected final TypeMap map;
  protected final CLITablePtr definingRow;
  protected final Map<FieldSymbol, Integer> instanceFieldIndexMapping = new HashMap<>();
  protected final Map<FieldSymbol, Integer> staticFieldIndexMapping = new HashMap<>();
  @CompilerDirectives.CompilationFinal protected NamedTypeSymbol lazyDirectBaseClass;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  protected NamedTypeSymbol[] lazyInterfaces;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  protected NamedTypeSymbol[] lazySuperClasses;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  protected MethodSymbol[] lazyMethods;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  protected MethodSymbol[] lazyMethodImpl;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  protected MethodSymbol[] lazyVMethodTable;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  protected FieldSymbol[] lazyFields;

  @CompilerDirectives.CompilationFinal private boolean isValueType = false;

  // region SOM - fields
  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> instanceShape;

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> staticShape;

  @CompilerDirectives.CompilationFinal private StaticObject staticInstance;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private StaticField[] instanceFields;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private StaticField[] staticFields;
  // endregion

  protected NamedTypeSymbol(
      ModuleSymbol definingModule,
      int flags,
      String name,
      String namespace,
      TypeParameterSymbol[] typeParameters,
      CLITablePtr definingRow) {
    this(
        definingModule,
        flags,
        name,
        namespace,
        typeParameters,
        definingRow,
        new TypeMap(new TypeParameterSymbol[0], new TypeSymbol[0]));
  }

  protected NamedTypeSymbol(
      ModuleSymbol definingModule,
      int flags,
      String name,
      String namespace,
      TypeParameterSymbol[] typeParameters,
      CLITablePtr definingRow,
      TypeMap map) {
    super(
        definingModule,
        CILOSTAZOLFrame.getStackTypeKind(name, namespace),
        SystemType.getTypeKind(
            name, namespace, definingModule.getDefiningFile().getAssemblyIdentity()));
    assert definingRow.getTableId() == CLITableConstants.CLI_TABLE_TYPE_DEF;

    this.flags = flags;
    this.name = name;
    this.namespace = namespace;
    this.typeParameters = typeParameters;
    this.definingRow = definingRow;
    this.map = map;
  }

  // region Getters
  public String getName() {
    return name;
  }

  public String getNamespace() {
    return namespace;
  }

  public NamedTypeSymbol getDirectBaseClass() {
    if (lazyDirectBaseClass == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyDirectBaseClass = LazyFactory.createDirectBaseClass(this);
      isValueType =
          lazyDirectBaseClass != null
              && lazyDirectBaseClass.getNamespace().equals("System")
              && lazyDirectBaseClass.getName().equals("ValueType");
    }

    return lazyDirectBaseClass;
  }

  public NamedTypeSymbol[] getSuperClasses() {
    if (lazySuperClasses == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazySuperClasses = LazyFactory.createSuperClasses(this);
    }

    return lazySuperClasses;
  }

  public NamedTypeSymbol[] getInterfaces() {
    if (lazyInterfaces == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyInterfaces = LazyFactory.createInterfaces(this);
    }

    return lazyInterfaces;
  }

  protected int getHierarchyDepth() {
    if (lazySuperClasses == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazySuperClasses = LazyFactory.createSuperClasses(this);
    }

    return lazySuperClasses.length;
  }

  public MethodSymbol[] getMethods() {
    if (lazyMethods == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      // TODO: create on demand
      lazyMethods =
          switch (definingRow.getTableId()) {
            case CLITableConstants.CLI_TABLE_TYPE_DEF -> LazyFactory.createMethods(
                this,
                definingModule
                    .getDefiningFile()
                    .getTableHeads()
                    .getTypeDefTableHead()
                    .skip(definingRow));
            case CLITableConstants.CLI_TABLE_TYPE_REF -> null; // TODO: implement case for ref table
            case CLITableConstants
                .CLI_TABLE_TYPE_SPEC -> null; // TODO: implement case for spec table
            default -> throw new TypeSystemException(
                CILOSTAZOLBundle.message("typeSystem.unknownTableType"));
          };
    }

    return lazyMethods;
  }

  public MethodSymbol[] getMethodsImpl() {
    throw new NotImplementedException();
  }

  public MethodSymbol[] getVMT() {
    throw new NotImplementedException();
  }

  public FieldSymbol[] getFields() {
    if (lazyFields == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      lazyFields =
          LazyFactory.createFields(
              this,
              definingModule
                  .getDefiningFile()
                  .getTableHeads()
                  .getTypeDefTableHead()
                  .skip(definingRow));
    }

    return lazyFields;
  }

  public StaticField getAssignableInstanceField(
      FieldSymbol field, VirtualFrame frame, int topStack) {
    if (instanceShape == null) {
      createShapes(frame, topStack);
    }

    assert !field.isStatic();
    return instanceFields[instanceFieldIndexMapping.get(field)];
  }

  public StaticField getAssignableStaticField(FieldSymbol field, VirtualFrame frame, int topStack) {
    if (staticShape == null) {
      createShapes(frame, topStack);
    }

    assert field.isStatic();
    return staticFields[staticFieldIndexMapping.get(field)];
  }

  public StaticObject getStaticInstance(VirtualFrame frame, int topStack) {
    if (staticInstance == null) {
      createShapes(frame, topStack);
    }

    return staticInstance;
  }

  public ConstructedNamedTypeSymbol construct(TypeSymbol[] typeArguments) {
    return ConstructedNamedTypeSymbol.ConstructedNamedTypeSymbolFactory.create(
        this, this, typeArguments);
  }

  public TypeSymbol[] getTypeArguments() {
    return getTypeParameters();
  }

  public TypeParameterSymbol[] getTypeParameters() {
    return typeParameters;
  }

  public TypeMap getTypeMap() {
    return map;
  }

  public NamedTypeSymbolVisibility getVisibility() {
    return NamedTypeSymbolVisibility.fromFlags(flags & NamedTypeSymbolVisibility.MASK);
  }

  public NamedTypeSymbolLayout getLayout() {
    return NamedTypeSymbolLayout.fromFlags(flags & NamedTypeSymbolLayout.MASK);
  }

  public NamedTypeSymbolSemantics getSemantics() {
    return NamedTypeSymbolSemantics.fromFlags(flags & NamedTypeSymbolSemantics.MASK);
  }

  public boolean isSealed() {
    return (flags & SEALED_FLAG_MASK) != 0;
  }

  public boolean isAbstract() {
    return (flags & ABSTRACT_FLAG_MASK) != 0;
  }

  public boolean isInterface() {
    return getSemantics() == NamedTypeSymbolSemantics.Interface;
  }

  public boolean isClass() {
    return !isInterface();
  }

  public boolean isSpecialName() {
    return (flags & SPECIAL_NAME_FLAG_MASK) != 0;
  }

  public boolean isImport() {
    return (flags & IMPORT_FLAG_MASK) != 0;
  }

  public boolean isSerializable() {
    return (flags & SERIALIZABLE_FLAG_MASK) != 0;
  }

  public boolean isBeforeFieldInit() {
    return (flags & BEFORE_FIELD_INIT_FLAG_MASK) != 0;
  }

  public boolean isRTSpecialName() {
    return (flags & RT_SPECIAL_NAME_FLAG_MASK) != 0;
  }

  public boolean hasSecurity() {
    return (flags & HAS_SECURITY_FLAG_MASK) != 0;
  }

  public boolean isTypeForwarder() {
    return (flags & IS_TYPE_FORWARDER_FLAG_MASK) != 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NamedTypeSymbol other
        && other.getName().equals(getName())
        && other.getNamespace().equals(getNamespace())
        && Arrays.equals(other.getTypeArguments(), getTypeArguments());
  }

  // endregion

  // region SOM shapes
  public StaticShape<StaticObject.StaticObjectFactory> getShape(
      VirtualFrame frame, int topStack, boolean isStatic) {
    if (isStatic && staticShape == null || !isStatic && instanceShape == null) {
      createShapes(frame, topStack);
    }

    return isStatic ? staticShape : instanceShape;
  }

  public StaticField[] getInstanceFields(VirtualFrame frame, int topStack) {
    if (instanceShape == null) {
      createShapes(frame, topStack);
    }

    return instanceFields;
  }

  public StaticField[] getStaticFields(VirtualFrame frame, int topStack) {
    if (staticFields == null) {
      createShapes(frame, topStack);
    }

    return staticFields;
  }

  private void createShapes(VirtualFrame frame, int topStack) {
    CompilerDirectives.transferToInterpreterAndInvalidate();

    LinkedFieldLayout layout =
        new LinkedFieldLayout(
            getContext(),
            this,
            getDirectBaseClass(),
            instanceFieldIndexMapping,
            staticFieldIndexMapping,
            frame,
            topStack);
    instanceShape = layout.instanceShape;
    staticShape = layout.staticShape;
    instanceFields = layout.instanceFields;
    staticFields = layout.staticFields;
    initializeStaticInstance(frame, topStack);
  }

  @ExplodeLoop
  private void initializeStaticInstance(VirtualFrame frame, int topStack) {
    staticInstance = staticShape.getFactory().create(this);
    if (frame == null) {
      return;
    }

    for (MethodSymbol method : getMethods()) {
      if (method.getName().equals(".cctor")) {
        callStaticConstructor(frame, topStack, method);
        break;
      }
    }
  }

  private void callStaticConstructor(VirtualFrame frame, int topStack, MethodSymbol constructor) {
    var callNode = new CALLNode(constructor, topStack);
    callNode.execute(frame);
  }
  // endregion

  public boolean isValueType() {
    if (lazyDirectBaseClass == null) {
      getDirectBaseClass();
    }

    return isValueType;
  }

  @Override
  public String toString() {
    return this.namespace + "/" + this.name;
  }

  // region Flags
  public enum NamedTypeSymbolLayout {
    Auto,
    Sequential,
    Explicit;
    public static final int MASK = 0x18;

    public static NamedTypeSymbolLayout fromFlags(int flags) {
      return NamedTypeSymbolLayout.values()[(flags & MASK) >> 3];
    }
  }

  public enum NamedTypeSymbolSemantics {
    Class,
    Interface;
    public static final int MASK = 0x20;

    public static NamedTypeSymbolSemantics fromFlags(int flags) {
      return NamedTypeSymbolSemantics.values()[(flags & MASK) >> 5];
    }
  }

  public enum NamedTypeSymbolVisibility {
    NotPublic,
    Public,
    NestedPublic,
    NestedPrivate,
    NestedFamily,
    NestedAssembly,
    NestedFamANDAssem,
    NestedFamORAssem;
    public static final int MASK = 0x7;

    public static NamedTypeSymbolVisibility fromFlags(int flags) {
      return NamedTypeSymbolVisibility.values()[flags & MASK];
    }
  }

  private static class LazyFactory {
    private static MethodSymbol[] createMethods(
        NamedTypeSymbol containingType, CLITypeDefTableRow row) {
      var methodRange =
          CLIFileUtils.getMethodRange(containingType.definingModule.getDefiningFile(), row);

      var methodRow =
          containingType
              .definingModule
              .getDefiningFile()
              .getTableHeads()
              .getMethodDefTableHead()
              .skip(new CLITablePtr(CLITableConstants.CLI_TABLE_METHOD_DEF, methodRange.getLeft()));

      var methods = new MethodSymbol[methodRange.getRight() - methodRange.getLeft()];
      while (methodRow.getRowNo() < methodRange.getRight()) {
        methods[methodRow.getRowNo() - methodRange.getLeft()] =
            MethodSymbol.MethodSymbolFactory.create(methodRow, containingType);
        methodRow = methodRow.skip(1);
      }

      return methods;
    }

    private static NamedTypeSymbol[] createInterfaces(NamedTypeSymbol symbol) {
      List<NamedTypeSymbol> interfaces = new ArrayList<>();
      for (var interfaceRow :
          symbol.definingModule.getDefiningFile().getTableHeads().getInterfaceImplTableHead()) {
        if (interfaceExtendsClass(
            interfaceRow, symbol.name, symbol.namespace, symbol.definingModule)) {
          interfaces.add(getInterface(interfaceRow, symbol.definingModule, symbol));
        }
      }

      return interfaces.toArray(new NamedTypeSymbol[0]);
    }

    static boolean interfaceExtendsClass(
        CLIInterfaceImplTableRow interfaceRow,
        String extendingClassName,
        String extendingClassNamespace,
        ModuleSymbol module) {
      var potentialExtendingClassRow =
          module
              .getDefiningFile()
              .getTableHeads()
              .getTypeDefTableHead()
              .skip(interfaceRow.getKlassTablePtr());
      // we can not create the whole klass because of circular dependency, we only need the name and
      // namespace
      var potentialClassName =
          potentialExtendingClassRow
              .getTypeNameHeapPtr()
              .read(module.getDefiningFile().getStringHeap());
      var potentialClassNamespace =
          potentialExtendingClassRow
              .getTypeNamespaceHeapPtr()
              .read(module.getDefiningFile().getStringHeap());

      return extendingClassName.equals(potentialClassName)
          && extendingClassNamespace.equals(potentialClassNamespace);
    }

    static NamedTypeSymbol getInterface(
        CLIInterfaceImplTableRow row, ModuleSymbol module, NamedTypeSymbol symbol) {
      CLITablePtr tablePtr = row.getInterfaceTablePtr();
      assert tablePtr != null; // Should never should be
      return (NamedTypeSymbol)
          SymbolResolver.resolveType(tablePtr, module, symbol.getTypeArguments());
    }

    public static NamedTypeSymbol createDirectBaseClass(NamedTypeSymbol namedTypeSymbol) {
      CLITablePtr baseClassPtr =
          namedTypeSymbol
              .definingModule
              .getDefiningFile()
              .getTableHeads()
              .getTypeDefTableHead()
              .skip(namedTypeSymbol.definingRow)
              .getExtendsTablePtr();

      assert baseClassPtr != null; // Never should be null
      return baseClassPtr.isEmpty()
          ? null
          : (NamedTypeSymbol)
              SymbolResolver.resolveType(
                  baseClassPtr, namedTypeSymbol.definingModule, namedTypeSymbol.getTypeArguments());
    }

    public static NamedTypeSymbol[] createSuperClasses(NamedTypeSymbol namedTypeSymbol) {
      NamedTypeSymbol baseClass = namedTypeSymbol.getDirectBaseClass();
      if (baseClass == null) {
        return new NamedTypeSymbol[0];
      }

      NamedTypeSymbol[] baseTypeSuperClasses = baseClass.getSuperClasses();
      NamedTypeSymbol[] result = new NamedTypeSymbol[baseTypeSuperClasses.length + 1];
      result[baseTypeSuperClasses.length] = baseClass;
      System.arraycopy(baseTypeSuperClasses, 0, result, 0, baseTypeSuperClasses.length);

      return result;
    }

    public static MethodSymbol[] createMethodsImpl(NamedTypeSymbol symbol) {
      HashMap<MethodSymbol, MethodSymbol> result = new HashMap<MethodSymbol, MethodSymbol>();
      for (var methodImpl :
          symbol.getDefiningModule().getDefiningFile().getTableHeads().getMethodImplTableHead()) {
        // TODO:...
      }
      return null;
    }

    public static FieldSymbol[] createFields(
        NamedTypeSymbol namedTypeSymbol, CLITypeDefTableRow row) {
      var fieldRange =
          CLIFileUtils.getFieldRange(namedTypeSymbol.definingModule.getDefiningFile(), row);

      var fieldRow =
          namedTypeSymbol
              .definingModule
              .getDefiningFile()
              .getTableHeads()
              .getFieldTableHead()
              .skip(new CLITablePtr(CLITableConstants.CLI_TABLE_FIELD, fieldRange.getLeft()));

      var fields = new FieldSymbol[fieldRange.getRight() - fieldRange.getLeft()];
      while (fieldRow.getRowNo() < fieldRange.getRight() && fieldRow.hasNext()) {
        var field =
            FieldSymbol.FieldSymbolFactory.create(
                fieldRow,
                new TypeSymbol[0],
                namedTypeSymbol.getTypeArguments(),
                namedTypeSymbol.getDefiningModule());

        fields[fieldRow.getRowNo() - fieldRange.getLeft()] = patch(field, namedTypeSymbol);
        fieldRow = fieldRow.next();
      }

      return fields;
    }

    /** We have to patch type of String to be able to represent it in SOM. */
    private static FieldSymbol patch(FieldSymbol symbol, NamedTypeSymbol type) {
      if (Objects.equals(type.getNamespace(), "System")
          && Objects.equals(type.getName(), "String")
          && symbol.getName().equals("_firstChar")) {
        return FieldSymbol.FieldSymbolFactory.createWith(
            symbol,
            ArrayTypeSymbol.ArrayTypeSymbolFactory.create(
                SymbolResolver.getChar(type.getContext()), type.getDefiningModule()));
      }
      return symbol;
    }
  }

  public static class NamedTypeSymbolFactory {
    public static NamedTypeSymbol create(CLIExportedTypeTableRow row, ModuleSymbol module) {
      if (row.getImplementationTablePtr().getTableId() == CLITableConstants.CLI_TABLE_ASSEMBLY_REF
          && (row.getFlags() & IS_TYPE_FORWARDER_FLAG_MASK)
              != 0) // type is forwarded to difference assembly
      {
        var rowName = row.getTypeNameHeapPtr().read(module.getDefiningFile().getStringHeap());
        var rowNamespace =
            row.getTypeNamespaceHeapPtr().read(module.getDefiningFile().getStringHeap());
        return NamedTypeSymbol.NamedTypeSymbolFactory.getTypeFromDifferentAssembly(
            rowName, rowNamespace, row.getImplementationTablePtr(), module);
      }

      return null;
    }

    public static NamedTypeSymbol create(CLITypeRefTableRow row, ModuleSymbol module) {
      var name = row.getTypeNameHeapPtr().read(module.getDefiningFile().getStringHeap());
      var namespace = row.getTypeNamespaceHeapPtr().read(module.getDefiningFile().getStringHeap());

      var resolutionScopeTablePtr = row.getResolutionScopeTablePtr();
      if (resolutionScopeTablePtr == null) {
        // TODO: find in ExportedType table
        throw new NotImplementedException();
      }

      return switch (resolutionScopeTablePtr.getTableId()) {
        case CLITableConstants.CLI_TABLE_TYPE_REF -> throw new UnsupportedOperationException(
            CILOSTAZOLBundle.message("cilostazol.exception.nested.typeRef.resolutionScope"));
        case CLITableConstants.CLI_TABLE_MODULE_REF -> getTypeFromDifferentModule(
            name, namespace, resolutionScopeTablePtr, module);
        case CLITableConstants.CLI_TABLE_MODULE -> throw new InvalidCLIException();
        case CLITableConstants.CLI_TABLE_ASSEMBLY_REF -> getTypeFromDifferentAssembly(
            name, namespace, resolutionScopeTablePtr, module);
        default -> throw new TypeSystemException(
            CILOSTAZOLBundle.message(
                "cilostazol.exception.unknown.resolutionScope",
                namespace,
                name,
                resolutionScopeTablePtr.getTableId()));
      };
    }

    private static NamedTypeSymbol getTypeFromDifferentModule(
        String name, String namespace, CLITablePtr resolutionScopeTablePtr, ModuleSymbol module) {
      var moduleRefName =
          module
              .getDefiningFile()
              .getTableHeads()
              .getModuleRefTableHead()
              .skip(resolutionScopeTablePtr)
              .getNameHeapPtr()
              .read(module.getDefiningFile().getStringHeap());

      // We can omit looking for file since we know that it is in the same assembly as this module
      // TODO: can be improved by calling getLocalTypeFromModule that would filter modules right
      // away by name
      return CILOSTAZOLContext.get(null)
          .resolveType(name, namespace, module.getDefiningFile().getAssemblyIdentity());
    }

    private static NamedTypeSymbol getTypeFromDifferentAssembly(
        String name, String namespace, CLITablePtr resolutionScopeTablePtr, ModuleSymbol module) {
      var referencedAssemblyIdentity =
          AssemblyIdentity.fromAssemblyRefRow(
              module.getDefiningFile().getStringHeap(),
              module
                  .getDefiningFile()
                  .getTableHeads()
                  .getAssemblyRefTableHead()
                  .skip(resolutionScopeTablePtr));
      var referencedAssembly = module.getContext().findAssembly(referencedAssemblyIdentity);
      if (referencedAssembly == null) {
        // TODO: log CILOSTAZOLBundle.message("cilostazol.warning.referencedAssemblyNotFound",
        // referencedAssemblyIdentity.toString())
        return null;
      }
      return referencedAssembly.getLocalType(name, namespace);
    }

    public static NamedTypeSymbol create(CLITypeDefTableRow row, ModuleSymbol module) {
      var nameAndNamespace = CLIFileUtils.getNameAndNamespace(module.getDefiningFile(), row);

      final String name = nameAndNamespace.getLeft();
      final String namespace = nameAndNamespace.getRight();
      final TypeParameterSymbol[] typeParams =
          TypeParameterSymbol.TypeParameterSymbolFactory.create(
              row.getPtr(), new TypeSymbol[0], module);
      int flags = row.getFlags();

      return new NamedTypeSymbol(module, flags, name, namespace, typeParams, row.getPtr());
    }

    public static NamedTypeSymbol create(
        TypeSpecSig typeSig, TypeSymbol[] mvars, TypeSymbol[] vars, ModuleSymbol module) {
      // TODO: null reference exception might have occured here if TypeSig is not created from CLASS
      // TODO: resolve for other types (SZARRAY, GENERICINST, ...)
      if (typeSig.getFlag().getFlag() == ElementTypeFlag.Flag.ELEMENT_TYPE_GENERICINST) {
        NamedTypeSymbol genType =
            (NamedTypeSymbol)
                TypeSymbol.TypeSymbolFactory.create(typeSig.getGenType(), mvars, vars, module);
        TypeSymbol[] typeArgs = new NamedTypeSymbol[typeSig.getTypeArgs().length];
        for (int i = 0; i < typeArgs.length; i++) {
          typeArgs[i] =
              TypeSymbol.TypeSymbolFactory.create(typeSig.getTypeArgs()[i], mvars, vars, module);
        }
        return genType.construct(typeArgs); // TODO: is this correct? @Tomas
      } else if (typeSig.getFlag().getFlag() == ElementTypeFlag.Flag.ELEMENT_TYPE_MVAR) {
        return null;
      } else if (typeSig.getFlag().getFlag() == ElementTypeFlag.Flag.ELEMENT_TYPE_VAR) {
        return null;
      } else {
        return null;
      }
    }
  }
  // endregion
}
