package com.vztekoverflow.cilostazol.runtime.staticanalysis;

import com.vztekoverflow.cilostazol.runtime.TestBase;

public abstract class StaticAnalysisTestBase extends TestBase {
  @Override
  protected String getDirectory() {
    return _directory;
  }

  private static final String _directory = "src/test/resources/StaticAnalysisTestTargets";
}
