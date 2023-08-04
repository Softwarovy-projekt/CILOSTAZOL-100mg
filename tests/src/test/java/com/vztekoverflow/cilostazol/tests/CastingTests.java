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
}
