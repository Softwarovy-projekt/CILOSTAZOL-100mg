package com.vztekoverflow.cilostazol.runtime;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.CILOSTAZOLLanguage;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;
import junit.framework.TestCase;

/**
 * You have to first build C# projects in test resources on path in: getDirectory().
 *
 * <p>Build it with configuration: {@value _configuration} and .NET version: {@value _dotnetVersion}
 */
public abstract class TestBase extends TestCase {
  protected abstract String getDirectory();

  protected static final String _runtimeDirectory = "../runtime";
  protected static final String _configuration = "Debug";
  protected static final String _dotnetVersion = "net7.0";

  protected CILOSTAZOLContext ctx;
  protected CILOSTAZOLLanguage lang;

  protected Path[] getDllPath(String projectName) {
    try {
      var paths = Files.walk(Paths.get(_runtimeDirectory));
      return Stream.concat(paths, Stream.of(Paths.get(getDirectory(), projectName, "bin")))
          .toArray(Path[]::new);

    } catch (IOException e) {
      throw new RuntimeException("Unable to get runtime dlls", e);
    }
  }

  protected Path[] getDllPath(String projectName, String otherProjectName) {
    try {
      var paths = Files.walk(Paths.get(_runtimeDirectory));
      var paths2 =
          Stream.concat(
              Stream.of(Paths.get(getDirectory(), projectName, "bin")),
              Stream.of(Paths.get(getDirectory(), otherProjectName, "bin")));
      return Stream.concat(paths, paths2).toArray(Path[]::new);

    } catch (IOException e) {
      throw new RuntimeException("Unable to get runtime dlls", e);
    }
  }

  protected CILOSTAZOLContext init(Path[] dllPaths) {
    this.lang = new CILOSTAZOLLanguage();
    this.ctx = new CILOSTAZOLContext(lang, dllPaths);
    ContextProviderImpl.getInstance().setContext(() -> ctx);
    return ContextProviderImpl.getInstance().getContext();
  }

  protected AssemblyIdentity getAssemblyID(String name) {
    return new AssemblyIdentity((short) 1, (short) 0, (short) 0, (short) 0, name);
  }

  protected MethodSymbol[] getMethod(NamedTypeSymbol type, String name) {
    return Arrays.stream(type.getMethods())
        .filter(m -> m.getName().equals(name))
        .toArray(MethodSymbol[]::new);
  }
}
