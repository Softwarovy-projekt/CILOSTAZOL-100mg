package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OSRTests extends OSRTestBase {
  /**
   * Time taken on M1 Macbook Air without OSR: ~23s Time taken on M1 Macbook Air in .NET Release:
   * ~0.04s Time taken on M1 Macbook Air with OSR: ~0.04s
   */
  @Test
  public void test() {
    var result =
        runTestFromCode(
            """
                      int i = 1;
                      while (i < 100000000)
                      {
                            i = i + 1 + 2 / 2 + 1 + 3 + 4 * 5 / 6 * 7 + 8 - 9 + 10 - 35;
                      }
                      return i;
                      """);

    assertEquals(100000000, result.exitCode());
  }
}
