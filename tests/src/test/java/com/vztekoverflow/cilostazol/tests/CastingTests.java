package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CastingTests extends TestBase {
  @Test
  public void castFromInt32() {
    var result =
        runTestFromCode(
            """
                      int a = 1;
                      long b = (long)a;
                      float c = (float)a;
                      double d = (double)a;

                      bool equal = b == 1 && c == 1.0f && d == 1.0;
                      return equal ? 42 : 0;
                      """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void castFromInt64() {
    var result =
        runTestFromCode(
            """
                      long a = 1;
                      int b = (int)a;
                      float c = (float)a;
                      double d = (double)a;

                      bool equal = b == 1 && c == 1.0f && d == 1.0;
                      return equal ? 42 : 0;
                      """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void castFromFloat() {
    var result =
        runTestFromCode(
            """
                      float a = 1.0f;
                      int b = (int)a;
                      long c = (long)a;
                      double d = (double)a;

                      bool equal = b == 1 && c == 1 && d == 1.0;
                      return equal ? 42 : 0;
                      """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void castFromDouble() {
    var result =
        runTestFromCode(
            """
                      double a = 1.0;
                      int b = (int)a;
                      long c = (long)a;
                      float d = (float)a;

                      bool equal = b == 1 && c == 1 && d == 1.0f;
                      return equal ? 42 : 0;
                      """);

    assertEquals(42, result.exitCode());
  }
}
