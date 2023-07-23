package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReturnTests extends TestBase {
  @Test
  public void Return42() {
    var result = runTestFromCode("""
return 42;
""");

    assertEquals("", result.output());
    assertEquals(42, result.exitCode());
  }
}
