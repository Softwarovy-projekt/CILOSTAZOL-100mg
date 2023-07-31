package com.vztekoverflow.cilostazol.runtime.context;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.staticobject.DefaultStaticProperty;
import com.oracle.truffle.api.staticobject.StaticProperty;
import com.oracle.truffle.api.staticobject.StaticShape;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.CILOSTAZOLEngineOption;
import com.vztekoverflow.cilostazol.CILOSTAZOLLanguage;
import com.vztekoverflow.cilostazol.runtime.objectmodel.GuestAllocator;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.AppDomain;
import com.vztekoverflow.cilostazol.runtime.other.MethodInstantiationCacheKey;
import com.vztekoverflow.cilostazol.runtime.other.TypeDefinitionCacheKey;
import com.vztekoverflow.cilostazol.runtime.other.TypeInstantiationCacheKey;
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

  private final AppDomain appDomain;

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
  }

  // For test propose only
  @TestOnly
  public CILOSTAZOLContext(CILOSTAZOLLanguage lang, Path[] libraryPaths) {
    language = lang;
    env = null;
    this.libraryPaths = libraryPaths;
    appDomain = new AppDomain();
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

  // region Symbols

  /** This should be used on any path that queries a type. @ApiNote uses cache. */
  public NamedTypeSymbol resolveType(String name, String namespace, AssemblyIdentity assembly) {
    assembly = AssemblyForwarder.forwardedAssembly(assembly);
    // Note: Assembly in cacheKey is different from what is came in as an argument due to lack of
    // forwarding implementation
    var cacheKey = new TypeDefinitionCacheKey(name, namespace, assembly);

    return typeDefinitionCache.computeIfAbsent(
        cacheKey,
        k -> {
          AssemblySymbol assemblySymbol = appDomain.getAssembly(cacheKey.assemblyIdentity());
          if (assemblySymbol == null) {
            assemblySymbol = findAssembly(cacheKey.assemblyIdentity());
          }

          if (assemblySymbol != null)
            return assemblySymbol.getLocalType(cacheKey.name(), cacheKey.namespace());

          return null;
        });
  }

  /** This should be used on any path that queries a type. @ApiNote uses cache. */
  public TypeSymbol resolveGenericTypeInstantiation(NamedTypeSymbol type, TypeSymbol[] typeArgs) {

    var cacheKey = new TypeInstantiationCacheKey(type, typeArgs);

    return typeInstantiationCache.computeIfAbsent(
        cacheKey,
        k -> {
          return k.genType().construct(k.typeArgs());
        });
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

    assemblyIdentity = AssemblyForwarder.forwardedAssembly(assemblyIdentity);

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
  // endregion

  // region SOM
  @CompilerDirectives.CompilationFinal private StaticProperty arrayProperty;

  @CompilerDirectives.CompilationFinal
  private StaticShape<StaticObject.StaticObjectFactory> arrayShape;

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
  // endregion
}
