package com.vztekoverflow.cilostazol.tests;

import static com.vztekoverflow.cilostazol.launcher.CILOSTAZOLLauncher.LANGUAGE_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vztekoverflow.cilostazol.CILOSTAZOLEngineOption;
import java.io.File;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.junit.jupiter.api.AfterEach;

public class OSRTestBase extends TestBase {
  /** Tests have to finish under 5 seconds, otherwise OSR might not have taken place. */
  private static final long TEST_TIME_LIMIT_S = 5L;

  protected static long NS_TO_S_FACTOR = 1_000_000_000L;
  protected long startTime;
  protected long endTime;

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
    startTime = System.nanoTime();
    int retCode = context.eval(getSource(sourceFilePath)).asInt();
    endTime = System.nanoTime();
    return retCode;
  }

  @AfterEach
  protected void checkTime() {
    System.out.println("Execution time: " + (endTime - startTime) / 1000000 + "ms");
    assertTrue(
        endTime - startTime < TEST_TIME_LIMIT_S * NS_TO_S_FACTOR,
        "Runtime time limit. Did OSR really take place?");
  }
}
