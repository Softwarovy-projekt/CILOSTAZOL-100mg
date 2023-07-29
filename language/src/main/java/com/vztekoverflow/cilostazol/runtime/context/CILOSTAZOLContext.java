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
import com.vztekoverflow.cil.parser.cli.CLIFileUtils;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.CILOSTAZOLEngineOption;
import com.vztekoverflow.cilostazol.CILOSTAZOLLanguage;
import com.vztekoverflow.cilostazol.runtime.objectmodel.GuestAllocator;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.AppDomain;
import com.vztekoverflow.cilostazol.runtime.other.TypeSymbolCacheKey;
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

  private final Map<TypeSymbolCacheKey, NamedTypeSymbol> typeSymbolCache = new HashMap<>();
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
  public NamedTypeSymbol getType(CLITablePtr ptr, ModuleSymbol module) {
    return switch (ptr.getTableId()) {
        // TODO: support edgecases such as if it can not be found
      case CLITableConstants.CLI_TABLE_TYPE_DEF -> {
        var row = module.getDefiningFile().getTableHeads().getTypeDefTableHead().skip(ptr);
        var nameAndNamespace = CLIFileUtils.getNameAndNamespace(module.getDefiningFile(), row);

        yield getType(
            nameAndNamespace.getLeft(),
            nameAndNamespace.getRight(),
            module.getDefiningFile().getAssemblyIdentity());
      }
      case CLITableConstants.CLI_TABLE_TYPE_REF -> {
        var row = module.getDefiningFile().getTableHeads().getTypeRefTableHead().skip(ptr);
        yield NamedTypeSymbol.NamedTypeSymbolFactory.create(row, module);
      }
      case CLITableConstants.CLI_TABLE_TYPE_SPEC -> {
        var row = module.getDefiningFile().getTableHeads().getTypeSpecTableHead().skip(ptr);
        yield (NamedTypeSymbol)
            TypeSymbol.TypeSymbolFactory.create(row, new TypeSymbol[0], new TypeSymbol[0], module);
      }
      default -> throw new RuntimeException("Not implemented yet");
    };
  }

  /** This should be use on any path that queries a type. @ApiNote uses cache. */
  public NamedTypeSymbol getType(String name, String namespace, AssemblyIdentity assembly) {
    assembly = AssemblyForwarder.forwardedAssembly(assembly);
    // Note: Assembly in cacheKey is different from what is came in as an argument due to lack of
    // forwarding implementation
    var cacheKey = new TypeSymbolCacheKey(name, namespace, assembly);

    return typeSymbolCache.computeIfAbsent(
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

  // region Built-in type symbols
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

  /**
   * Method to get a built-in type assembly
   *
   * @return Assembly of a built-in type.
   */
  public NamedTypeSymbol getType(CILBuiltInType type) {
    return getType(type.Name, "System", AssemblyIdentity.SystemPrivateCoreLib700());
  }
  // endregion
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
