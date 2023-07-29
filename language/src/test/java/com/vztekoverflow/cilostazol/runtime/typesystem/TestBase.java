package com.vztekoverflow.cilostazol.runtime.typesystem;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.CILOSTAZOLLanguage;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import junit.framework.TestCase;

/**
 * You have to first build C# projects in test resources: {@value _directory}.
 *
 * <p>Build it with configuration: {@value _directory} and .NET version: {@value _dotnetVersion}
 */
public abstract class TestBase extends TestCase {
  protected static final String _directory = "src/test/resources/TypeParsingTestTargets";
  protected static final String _runtimeDirectory = "../runtime";
  protected static final String _configuration = "Debug";
  protected static final String _dotnetVersion = "net7.0";

  protected CILOSTAZOLContext ctx;
  protected CILOSTAZOLLanguage lang;

  protected Path[] getDllPath(String projectName) {
    try {
      var paths = Files.walk(Paths.get(_runtimeDirectory));
      return Stream.concat(paths, Stream.of(Paths.get(_directory, projectName, "bin")))
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
              Stream.of(Paths.get(_directory, projectName, "bin")),
              Stream.of(Paths.get(_directory, otherProjectName, "bin")));
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
}
