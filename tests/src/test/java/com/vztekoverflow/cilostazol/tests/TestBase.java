package com.vztekoverflow.cilostazol.tests;

import static com.vztekoverflow.cilostazol.launcher.CILOSTAZOLLauncher.LANGUAGE_ID;

import com.vztekoverflow.cilostazol.CILOSTAZOLEngineOption;
import com.vztekoverflow.cilostazol.CILOSTAZOLLanguage;
import com.vztekoverflow.cilostazol.launcher.CILOSTAZOLLauncher;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

public abstract class TestBase {
  private static final String directoryDllTests = "src/test/resources/BasicTests";
  private static final String directoryCustomTest =
      "src/test/resources/InterpreterTests/CustomTest";
  private static final String testSourcesDirectory =
      "src/test/resources/InterpreterTests/TestSources";
  private static final String configuration = "Debug";
  private static final String dotnetVersion = "net7.0";

  protected static final String directoryDlls = "../runtime";
  protected static OutputStream outputStream;

  public static Context context;

  @BeforeEach
  public void setup() {
    outputStream = new ByteArrayOutputStream();
  }

  /**
   * Use this for quick/additional tests who's core features are tested by {@link
   * #runTestFromFile(String)} or {@link #runTestFromDll(String)} tests already.
   */
  protected RunResult runTestFromCode(@NotNull @Language("cs") String sourceCode) {
    return runTestFromCode(sourceCode, null);
  }

  protected RunResult runTestFromCode(
      @NotNull @Language("cs") String sourceCode, String[] arguments) {
    if (arguments != null)
      context = setupContext().arguments(CILOSTAZOLLanguage.ID, arguments).build();
    else context = setupContext().build();

    // create random directory for the dummy project
    var randomName = java.util.UUID.randomUUID().toString();
    var directory = Paths.get(directoryCustomTest, randomName);

    try {
      var sourceFile = compileCode(sourceCode, directory).toFile();

      var retCode = evaluate(sourceFile);

      return new RunResult(outputStream.toString(), retCode);
    } finally {
      // delete random directory
      deleteDirectory(directory.toFile());
    }
  }
  /**
   * Use this or {@link #runTestFromDll(String)} for each major feature of the interpreter (return,
   * if, while, etc.).
   */
  protected RunResult runTestFromFile(@NotNull String sourceFile) {
    context = setupContext().build();
    // create random directory for the dummy project
    var randomName = java.util.UUID.randomUUID().toString();
    var directory = Paths.get(directoryCustomTest, randomName);

    try {
      var sourceFilePath = compileFile(sourceFile, directory).toFile();

      var retCode = evaluate(sourceFilePath);

      return new RunResult(outputStream.toString(), retCode);
    } finally {
      // delete random directory
      deleteDirectory(directory.toFile());
    }
  }

  protected int evaluate(File sourceFilePath) {
    return context.eval(getSource(sourceFilePath)).asInt();
  }

  /**
   * Use this or {@link #runTestFromFile(String)} for each major feature of the interpreter (return,
   * if, while, etc.).
   */
  protected RunResult runTestFromDll(@NotNull String projectName) {
    context = setupContext().build();
    var sourceFilePath = this.getDllPathFromProject(projectName).toFile();

    var retCode = evaluate(sourceFilePath);
    return new RunResult(outputStream.toString(), retCode);
  }

  /**
   * Runs a test with sourcecode from <code>dll</code> located at: <code>
   * tests\src\test\resources\BasicTests\*projectName*\bin\Release\.net7.0\*projectName*.dll</code>
   * @Deprecated Use {@link #runTestFromDll(String)} instead
   */
  @Deprecated()
  protected CILOSTAZOLLauncher runTestFromDllViaLauncher(String projectName) {
    CILOSTAZOLLauncher launcher = new CILOSTAZOLLauncher();
    launcher.test(new String[] {getDllPathFromProject(projectName).toString()});
    return launcher;
  }

  /**
   * @Deprecated Use {@link #runTestFromFile(String)} instead
   */
  @Deprecated()
  protected CILOSTAZOLLauncher runTestFromFileLauncher(String sourceFile) {
    CILOSTAZOLLauncher launcher = new CILOSTAZOLLauncher();
    var randomName = java.util.UUID.randomUUID().toString();
    var directory = Paths.get(directoryCustomTest, randomName);

    launcher.test(new String[] {compileFile(sourceFile, directory).toString()});

    deleteDirectory(directory.toFile());
    return launcher;
  }

  /**
   * @Deprecated Use {@link #runTestFromCode(String)} instead
   */
  @Deprecated()
  protected CILOSTAZOLLauncher runTestFromCodeLauncher(@Language("cs") String code) {
    CILOSTAZOLLauncher launcher = new CILOSTAZOLLauncher();
    var randomName = java.util.UUID.randomUUID().toString();
    var directory = Paths.get(directoryCustomTest, randomName);

    launcher.test(new String[] {compileCode(code, directory).toString()});

    deleteDirectory(directory.toFile());
    return launcher;
  }

  /**
   * Can be parallelized. Project must be at {@value directoryDllTests} and must be compiled before
   * running tests.
   */
  private Path getDllPathFromProject(String projectName) {
    return Paths.get(
        directoryDllTests, projectName, "bin", configuration, dotnetVersion, projectName + ".dll");
  }

  boolean deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        deleteDirectory(file);
      }
    }
    return directoryToBeDeleted.delete();
  }

  /** Can be parallelized */
  private Path compileCode(@Language("cs") String code, Path dummyDirectory) {
    // create dummy directory
    createDirectory(dummyDirectory);

    // copy proj file to the dummy project
    copyCsproj(dummyDirectory);

    // replace content of the dummy file with the source code
    writeSourceCode(code, dummyDirectory);

    // build the dummy project
    compile(Paths.get(dummyDirectory.toString(), "bin"));

    // return the path to the dll of a dummy project
    return Paths.get(dummyDirectory.toString(), "bin", "CustomTest.dll");
  }

  private static void createDirectory(Path dummyDirectory) {
    try {
      if (!java.nio.file.Files.exists(dummyDirectory))
        java.nio.file.Files.createDirectory(dummyDirectory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void copyCsproj(Path dummyDirectory) {
    try {
      java.nio.file.Files.copy(
          Paths.get(directoryCustomTest, "CustomTest.csproj"),
          Paths.get(dummyDirectory.toString(), "CustomTest.csproj"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void writeSourceCode(@Language("cs") String code, Path dummyDirectory) {
    try {
      var programPath = Paths.get(dummyDirectory.toString(), "Program.cs");
      if (!java.nio.file.Files.exists(programPath)) {
        java.nio.file.Files.createFile(programPath);
      }
      java.nio.file.Files.writeString(programPath, code);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void compile(Path dummyDirectory) {
    createDirectory(dummyDirectory);

    try {
      Runtime.getRuntime()
          .exec(
              "dotnet build %s -c %s -f %s -o %s"
                  .formatted(
                      dummyDirectory.getParent(), configuration, dotnetVersion, dummyDirectory))
          .waitFor();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /** Can be parallelized! */
  private Path compileFile(String sourceFilePath, Path directory) {
    // read content of the file
    @Language("cs")
    String sourceCode;
    sourceCode = getSourceCode(sourceFilePath);

    return compileCode(sourceCode, directory);
  }

  private static @Language("cs") String getSourceCode(String sourceFile) {
    @Language("cs")
    String sourceCode;
    try {
      sourceCode = java.nio.file.Files.readString(Paths.get(testSourcesDirectory, sourceFile));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sourceCode;
  }

  protected Context.Builder setupContext() {
    return Context.newBuilder(LANGUAGE_ID)
        .engine(
            Engine.newBuilder(LANGUAGE_ID)
                .option(CILOSTAZOLEngineOption.LIBRARY_PATH_NAME, directoryDlls)
                .build())
        .out(outputStream)
        .err(outputStream)
        .allowAllAccess(true);
  }

  protected Source getSource(File sourceFile) {
    try {
      return Source.newBuilder(LANGUAGE_ID, sourceFile).build();
    } catch (NoSuchFileException e) {
      throw new RuntimeException("Problem with compilation of test sources");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
