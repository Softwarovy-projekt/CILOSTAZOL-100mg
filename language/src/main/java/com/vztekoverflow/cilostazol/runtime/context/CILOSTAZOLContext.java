package com.vztekoverflow.cilostazol.runtime.context;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.staticobject.DefaultStaticProperty;
import com.oracle.truffle.api.staticobject.StaticProperty;
import com.oracle.truffle.api.staticobject.StaticShape;
import com.vztekoverflow.bacil.parser.cli.tables.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.CILOSTAZOLEngineOption;
import com.vztekoverflow.cilostazol.CILOSTAZOLLanguage;
import com.vztekoverflow.cilostazol.runtime.objectmodel.GuestAllocator;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.AppDomain;
import com.vztekoverflow.cilostazol.runtime.symbols.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.ByteSequence;
import org.jetbrains.annotations.TestOnly;

public class CILOSTAZOLContext {
  public static final TruffleLanguage.ContextReference<CILOSTAZOLContext> CONTEXT_REF =
      TruffleLanguage.ContextReference.create(CILOSTAZOLLanguage.class);
  private final Path[] libraryPaths;
  private final CILOSTAZOLLanguage language;
  private final TruffleLanguage.Env env;

  private final Map<TypeDefinitionCacheKey, NamedTypeSymbol> typeDefinitionCache = new HashMap<>();
  private final Map<TypeInstantiationCacheKey, TypeSymbol> typeInstantiationCache = new HashMap<>();
  private final Map<MethodInstantiationCacheKey, MethodSymbol> methodInstantiationCache =
      new HashMap<>();

  private final Map<ArrayCacheKey, ArrayTypeSymbol> arrayCache = new HashMap<>();
  private final ReferenceSymbol localReference;
  private final ReferenceSymbol argumentReference;
  private final ReferenceSymbol fieldReference;
  private final ReferenceSymbol arrayElementReference;
  private final ReferenceSymbol typedReference;

  private final AppDomain appDomain;

  // region shapes
  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> typedReferenceShape;

  @CompilerDirectives.CompilationFinal private StaticProperty typedReferenceInnerRefProperty;
  @CompilerDirectives.CompilationFinal private StaticProperty typedReferenceTypeTokenProperty;
  @CompilerDirectives.CompilationFinal private StaticProperty arrayProperty;

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> arrayShape;

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> stackReferenceShape;

  @CompilerDirectives.CompilationFinal private StaticProperty stackReferenceFrameProperty;
  @CompilerDirectives.CompilationFinal private StaticProperty stackReferenceIndexProperty;
  private StaticShape<StaticObject.StaticObjectFactory> fieldReferenceShape;
  @CompilerDirectives.CompilationFinal private StaticProperty fieldReferenceObjectProperty;
  @CompilerDirectives.CompilationFinal private StaticProperty fieldReferenceFieldProperty;

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> arrayElementReferenceShape;

  @CompilerDirectives.CompilationFinal private StaticProperty arrayElementReferenceArrayProperty;
  @CompilerDirectives.CompilationFinal private StaticProperty arrayElementReferenceIndexProperty;
  // endregion

  // region symbols
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Boolean = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Byte = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol SByte = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Char = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Double = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Single = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Int32 = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol UInt32 = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Int64 = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol UInt64 = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Int16 = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol UInt16 = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Object = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Void = null;

  @CompilerDirectives.CompilationFinal private NamedTypeSymbol String = null;
  @CompilerDirectives.CompilationFinal private NamedTypeSymbol Array = null;
  // endregion

  public CILOSTAZOLContext(CILOSTAZOLLanguage lang, TruffleLanguage.Env env) {
    language = lang;
    this.env = env;
    getLanguage().initializeGuestAllocator(env);
    libraryPaths =
        Arrays.stream(CILOSTAZOLEngineOption.getPolyglotOptionSearchPaths(env))
            .filter(
                p -> {
                  TruffleFile file = getEnv().getInternalTruffleFile(p.toString());
                  return file.isDirectory();
                })
            .distinct()
            .toArray(Path[]::new);
    appDomain = new AppDomain();

    // init ref symbols
    localReference = ReferenceSymbol.ReferenceSymbolFactory.createLocalReference();
    argumentReference = ReferenceSymbol.ReferenceSymbolFactory.createArgumentReference();
    fieldReference = ReferenceSymbol.ReferenceSymbolFactory.createFieldReference();
    arrayElementReference = ReferenceSymbol.ReferenceSymbolFactory.createArrayElemReference();
    typedReference = ReferenceSymbol.ReferenceSymbolFactory.createTypedReference();
  }

  // For test propose only
  @TestOnly
  public CILOSTAZOLContext(CILOSTAZOLLanguage lang, Path[] libraryPaths) {
    language = lang;
    env = null;
    this.libraryPaths = libraryPaths;
    appDomain = new AppDomain();

    // init ref symbols
    localReference = ReferenceSymbol.ReferenceSymbolFactory.createLocalReference();
    argumentReference = ReferenceSymbol.ReferenceSymbolFactory.createArgumentReference();
    fieldReference = ReferenceSymbol.ReferenceSymbolFactory.createFieldReference();
    arrayElementReference = ReferenceSymbol.ReferenceSymbolFactory.createArrayElemReference();
    typedReference = ReferenceSymbol.ReferenceSymbolFactory.createTypedReference();
  }

  public static CILOSTAZOLContext get(Node node) {
    return CONTEXT_REF.get(node);
  }

  public CILOSTAZOLLanguage getLanguage() {
    return language;
  }

  public GuestAllocator getAllocator() {
    return getLanguage().getAllocator();
  }

  public TruffleLanguage.Env getEnv() {
    return env;
  }

  // region symbol resolution
  public ArrayTypeSymbol resolveArray(TypeSymbol elemType, int rank) {
    var cacheKey = new ArrayCacheKey(elemType, rank);

    return arrayCache.computeIfAbsent(
        cacheKey,
        k ->
            ArrayTypeSymbol.ArrayTypeSymbolFactory.create(
                k.elemType(), k.rank(), elemType.getDefiningModule()));
  }

  /** This should be used on any path that queries a type. @ApiNote uses cache. */
  public NamedTypeSymbol resolveType(String name, String namespace, AssemblyIdentity assembly) {
    var cacheKey = new TypeDefinitionCacheKey(name, namespace, assembly);

    if (typeDefinitionCache.containsKey(cacheKey)) {
      return typeDefinitionCache.get(cacheKey);
    } else {

      AssemblySymbol assemblySymbol = resolveAssembly(assembly);
      var defAssembly = assemblySymbol.getLocalTypeDefiningAssembly(name, namespace);
      cacheKey = new TypeDefinitionCacheKey(name, namespace, defAssembly);
      assemblySymbol = resolveAssembly(cacheKey.assemblyIdentity());

      if (typeDefinitionCache.containsKey(cacheKey)) {
        return typeDefinitionCache.get(cacheKey);
      }

      if (assemblySymbol != null) {
        var result = assemblySymbol.getLocalType(cacheKey.name(), cacheKey.namespace());
        if (result != null) {
          typeDefinitionCache.put(cacheKey, result);
          return result;
        }
      }

      return null;
    }
  }

  /** This should be used on any path that queries a type. @ApiNote uses cache. */
  public TypeSymbol resolveGenericTypeInstantiation(NamedTypeSymbol type, TypeSymbol[] typeArgs) {

    var cacheKey = new TypeInstantiationCacheKey(type, typeArgs);

    return typeInstantiationCache.computeIfAbsent(
        cacheKey, k -> k.genType().construct(k.typeArgs()));
  }

  public MethodSymbol resolveGenericMethodInstantiation(
      MethodSymbol method, TypeSymbol[] typeArgs) {
    var cacheKey = new MethodInstantiationCacheKey(method, typeArgs);

    return methodInstantiationCache.computeIfAbsent(
        cacheKey,
        k -> {
          return k.genMethod().construct(k.typeArgs());
        });
  }

  public AssemblySymbol resolveAssembly(AssemblyIdentity assemblyIdentity) {
    AssemblySymbol assemblySymbol = appDomain.getAssembly(assemblyIdentity);
    if (assemblySymbol == null) {
      assemblySymbol = findAssembly(assemblyIdentity);
    }

    return assemblySymbol;
  }

  public ReferenceSymbol resolveReference(ReferenceSymbol.ReferenceType type) {
    return switch (type) {
      case Local -> localReference;
      case Argument -> argumentReference;
      case Field -> fieldReference;
      case ArrayElement -> arrayElementReference;
      case Typed -> typedReference;
    };
  }

  public AssemblySymbol findAssembly(AssemblyIdentity assemblyIdentity) {
    // Loading assemblies is an expensive task which should be never compiled
    CompilerAsserts.neverPartOfCompilation();

    // Locate dlls in paths
    for (Path path : libraryPaths) {
      File file = new File(path.toString() + "/" + assemblyIdentity.getName() + ".dll");
      if (file.exists()) {
        try {
          return loadAssembly(
              Source.newBuilder(
                      CILOSTAZOLLanguage.ID,
                      ByteSequence.create(Files.readAllBytes(file.toPath())),
                      file.getName())
                  .build());
        } catch (Exception e) {
          throw new RuntimeException(
              CILOSTAZOLBundle.message(
                  "cilostazol.exception.error.loading.assembly", assemblyIdentity.getName(), path),
              e);
        }
      }
    }
    throw new RuntimeException(
        CILOSTAZOLBundle.message(
            "cilostazol.exception.missing.assembly",
            assemblyIdentity.getName(),
            Arrays.toString(libraryPaths)));
  }

  public AssemblySymbol loadAssembly(Source source) {
    var result = AssemblySymbol.AssemblySymbolFactory.create(source);
    appDomain.loadAssembly(result);
    return result;
  }

  public NamedTypeSymbol getBoolean() {
    if (Boolean == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Boolean = resolveType("Boolean", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Boolean;
  }

  public NamedTypeSymbol getByte() {
    if (Byte == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Byte = resolveType("Byte", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Byte;
  }

  public NamedTypeSymbol getSByte() {
    if (SByte == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      SByte = resolveType("SByte", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return SByte;
  }

  public NamedTypeSymbol getChar() {
    if (Char == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Char = resolveType("Char", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Char;
  }

  public NamedTypeSymbol getDouble() {
    if (Double == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Double = resolveType("Double", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Double;
  }

  public NamedTypeSymbol getSingle() {
    if (Single == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Single = resolveType("Single", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Single;
  }

  public NamedTypeSymbol getInt32() {
    if (Int32 == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Int32 = resolveType("Int32", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Int32;
  }

  public NamedTypeSymbol getUInt32() {
    if (UInt32 == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      UInt32 = resolveType("UInt32", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return UInt32;
  }

  public NamedTypeSymbol getInt64() {
    if (Int64 == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Int64 = resolveType("Int64", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Int64;
  }

  public NamedTypeSymbol getUInt64() {
    if (UInt64 == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      UInt64 = resolveType("UInt64", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return UInt64;
  }

  public NamedTypeSymbol getInt16() {
    if (Int16 == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Int16 = resolveType("Int16", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Int16;
  }

  public NamedTypeSymbol getUInt16() {
    if (UInt16 == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      UInt16 = resolveType("UInt16", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return UInt16;
  }

  public NamedTypeSymbol getObject() {
    if (Object == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Object = resolveType("Object", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Object;
  }

  public NamedTypeSymbol getVoid() {
    if (Void == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Void = resolveType("Void", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Void;
  }

  public NamedTypeSymbol getString() {
    if (String == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      String = resolveType("String", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return String;
  }

  public NamedTypeSymbol getArray() {
    if (Array == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      Array = resolveType("String", "System", AssemblyIdentity.SystemRuntimeLib700());
    }
    return Array;
  }
  // endregion

  // region shapes
  public StaticProperty getArrayProperty() {
    if (arrayProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      arrayProperty = new DefaultStaticProperty("array");
    }
    return arrayProperty;
  }

  public StaticShape<StaticObject.StaticObjectFactory> getArrayShape() {
    if (arrayShape == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      arrayShape =
          StaticShape.newBuilder(CILOSTAZOLLanguage.get(null))
              .property(getArrayProperty(), Object.class, true)
              .build(StaticObject.class, StaticObject.StaticObjectFactory.class);
    }
    return arrayShape;
  }

  public StaticShape<StaticObject.StaticObjectFactory> getStackReferenceShape() {
    if (stackReferenceShape == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      stackReferenceShape =
          StaticShape.newBuilder(CILOSTAZOLLanguage.get(null))
              .property(getStackReferenceFrameProperty(), Object.class, true)
              .property(getStackReferenceIndexProperty(), int.class, true)
              .build(StaticObject.class, StaticObject.StaticObjectFactory.class);
    }
    return stackReferenceShape;
  }

  public StaticProperty getStackReferenceFrameProperty() {
    if (stackReferenceFrameProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      stackReferenceFrameProperty = new DefaultStaticProperty("frame");
    }
    return stackReferenceFrameProperty;
  }

  public StaticProperty getStackReferenceIndexProperty() {
    if (stackReferenceIndexProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      stackReferenceIndexProperty = new DefaultStaticProperty("index");
    }
    return stackReferenceIndexProperty;
  }

  public StaticShape<StaticObject.StaticObjectFactory> getFieldReferenceShape() {
    if (fieldReferenceShape == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      fieldReferenceShape =
          StaticShape.newBuilder(CILOSTAZOLLanguage.get(null))
              .property(getFieldReferenceObjectProperty(), Object.class, true)
              .property(getFieldReferenceFieldProperty(), Object.class, true)
              .build(StaticObject.class, StaticObject.StaticObjectFactory.class);
    }
    return fieldReferenceShape;
  }

  public StaticProperty getFieldReferenceObjectProperty() {
    if (fieldReferenceObjectProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      fieldReferenceObjectProperty = new DefaultStaticProperty("object");
    }
    return fieldReferenceObjectProperty;
  }

  public StaticProperty getFieldReferenceFieldProperty() {
    if (fieldReferenceFieldProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      fieldReferenceFieldProperty = new DefaultStaticProperty("field");
    }
    return fieldReferenceFieldProperty;
  }

  public StaticShape<StaticObject.StaticObjectFactory> getArrayElementReferenceShape() {
    if (arrayElementReferenceShape == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      arrayElementReferenceShape =
          StaticShape.newBuilder(CILOSTAZOLLanguage.get(null))
              .property(getArrayElementReferenceArrayProperty(), Object.class, true)
              .property(getArrayElementReferenceIndexProperty(), int.class, true)
              .build(StaticObject.class, StaticObject.StaticObjectFactory.class);
    }
    return arrayElementReferenceShape;
  }

  public StaticProperty getArrayElementReferenceArrayProperty() {
    if (arrayElementReferenceArrayProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      arrayElementReferenceArrayProperty = new DefaultStaticProperty("array");
    }
    return arrayElementReferenceArrayProperty;
  }

  public StaticProperty getArrayElementReferenceIndexProperty() {
    if (arrayElementReferenceIndexProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      arrayElementReferenceIndexProperty = new DefaultStaticProperty("index");
    }
    return arrayElementReferenceIndexProperty;
  }

  public StaticShape<StaticObject.StaticObjectFactory> getTypedReferenceShape() {
    if (typedReferenceShape == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      typedReferenceShape =
          StaticShape.newBuilder(CILOSTAZOLLanguage.get(null))
              .property(getTypedReferenceInnerRefProperty(), StaticObject.class, true)
              .property(getTypedReferenceTypeTokenProperty(), CLITablePtr.class, true)
              .build(StaticObject.class, StaticObject.StaticObjectFactory.class);
    }
    return typedReferenceShape;
  }

  public StaticProperty getTypedReferenceInnerRefProperty() {
    if (typedReferenceInnerRefProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      typedReferenceInnerRefProperty = new DefaultStaticProperty("innerRef");
    }

    return typedReferenceInnerRefProperty;
  }

  public StaticProperty getTypedReferenceTypeTokenProperty() {
    if (typedReferenceTypeTokenProperty == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      typedReferenceTypeTokenProperty = new DefaultStaticProperty("typeToken");
    }

    return typedReferenceTypeTokenProperty;
  }
  // endregion
}
