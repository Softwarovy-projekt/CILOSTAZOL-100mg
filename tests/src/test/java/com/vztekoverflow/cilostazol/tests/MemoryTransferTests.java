package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MemoryTransferTests extends TestBase {
  @Test
  public void storeArg() {
    var result =
        runTestFromCode(
            """
                        return Foo(1);

                        static int Foo(int a)
                        {
                            a = 42;
                            return a;
                        }
                        """);

    assertEquals(42, result.exitCode());
  }
}
