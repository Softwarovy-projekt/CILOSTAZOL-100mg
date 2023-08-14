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

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> typedReferenceShape;

  @CompilerDirectives.CompilationFinal private StaticProperty typedReferenceInnerRefProperty;
  @CompilerDirectives.CompilationFinal private StaticProperty typedReferenceTypeTokenProperty;
  // region SOM
  @CompilerDirectives.CompilationFinal private StaticProperty arrayProperty;

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> arrayShape;

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> stackReferenceShape;

  // region Symbols
  @CompilerDirectives.CompilationFinal private StaticProperty stackReferenceFrameProperty;
  @CompilerDirectives.CompilationFinal private StaticProperty stackReferenceIndexProperty;
  private StaticShape<StaticObject.StaticObjectFactory> fieldReferenceShape;
  @CompilerDirectives.CompilationFinal private StaticProperty fieldReferenceObjectProperty;
  @CompilerDirectives.CompilationFinal private StaticProperty fieldReferenceFieldProperty;

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> arrayElementReferenceShape;

  @CompilerDirectives.CompilationFinal private StaticProperty arrayElementReferenceArrayProperty;
  // endregion
  @CompilerDirectives.CompilationFinal private StaticProperty arrayElementReferenceIndexProperty;

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
      AssemblySymbol assemblySymbol = appDomain.getAssembly(cacheKey.assemblyIdentity());
      if (assemblySymbol == null) {
        assemblySymbol = findAssembly(cacheKey.assemblyIdentity());
      }

      var defAssembly = assemblySymbol.getLocalTypeDefiningAssembly(name, namespace);
      if (defAssembly != assembly) {
        cacheKey = new TypeDefinitionCacheKey(name, namespace, defAssembly);

        if (typeDefinitionCache.containsKey(cacheKey)) {
          return typeDefinitionCache.get(cacheKey);
        }

        assemblySymbol = appDomain.getAssembly(cacheKey.assemblyIdentity());
        if (assemblySymbol == null) {
          assemblySymbol = findAssembly(cacheKey.assemblyIdentity());
        }
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

  public ReferenceSymbol resolveReference(ReferenceSymbol.ReferenceType type) {
    return switch (type) {
      case Local -> localReference;
      case Argument -> argumentReference;
      case Field -> fieldReference;
      case ArrayElement -> arrayElementReference;
      case Typed -> typedReference;
    };
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
