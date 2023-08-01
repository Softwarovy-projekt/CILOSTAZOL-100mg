package com.vztekoverflow.cilostazol.runtime.typesystem;

import com.vztekoverflow.cilostazol.runtime.TestBase;

public abstract class TypeSystemTestBase extends TestBase {
  @Override
  protected String getDirectory() {
    return _directory;
  }

  private static final String _directory = "src/test/resources/TypeParsingTestTargets";
}
