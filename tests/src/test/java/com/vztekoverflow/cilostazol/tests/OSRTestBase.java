package com.vztekoverflow.cilostazol.tests;

import static com.vztekoverflow.cilostazol.launcher.CILOSTAZOLLauncher.LANGUAGE_ID;

import com.vztekoverflow.cilostazol.CILOSTAZOLEngineOption;
import java.io.File;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

public class OSRTestBase extends TestBase {
  protected Context.Builder setupContext() {
    return Context.newBuilder(LANGUAGE_ID)
        .engine(
            Engine.newBuilder(LANGUAGE_ID)
                .allowExperimentalOptions(true)
                .option(CILOSTAZOLEngineOption.LIBRARY_PATH_NAME, directoryDlls)
                .option("engine.TraceCompilationDetails", "true")
                .option("engine.MultiTier", "false")
                .option("engine.OSR", "true")
                .build())
        .out(outputStream)
        .err(outputStream)
        .allowAllAccess(true);
  }

  protected int evaluate(File sourceFilePath) {
    long startTime = System.nanoTime();
    int retCode = context.eval(getSource(sourceFilePath)).asInt();
    long endTime = System.nanoTime();
    System.out.println("Execution time: " + (endTime - startTime) / 1000000 + "ms");
    return retCode;
  }
}
