package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CastingTests extends TestBase {
  @Test
  public void castFromSignedInt32ToSigned() {
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
  public void castFromSignedInt64ToSigned() {
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
  public void castFromFloatToSigned() {
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
  public void castFromDoubleToSigned() {
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

  @Test
  public void castFromSignedInt32ToUnsigned() {
    var result =
        runTestFromCode(
            """
                    int a = -1;
                    ushort b = (ushort)a;
                    ulong c = (ulong)a;

                    bool equal = b == 65535 && c == 18446744073709551615;
                    return equal ? 42 : 0;
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void castFromSignedInt64ToUnsigned() {
    var result =
        runTestFromCode(
            """
                    long a = -1;
                    uint b = (uint)a;
                    ushort c = (ushort)a;

                    bool equal = b == 4294967295 && c == 65535;
                    return equal ? 42 : 0;
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void castFromUnsignedInt32ToSigned() {
    var result =
        runTestFromCode(
            """
                  uint a = 4294967295;
                  int b = (int)a;
                  long c = (long)a;

                  bool equal = b == -1 && c == 4294967295;
                  return equal ? 42 : 0;
                  """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void castFromUnsignedInt64ToSigned() {
    var result =
        runTestFromCode(
            """
                      ulong a = 18446744073709551615;
                      int b = (int)a;
                      long c = (long)a;

                      bool equal = b == -1 && c == -1;
                      return equal ? 42 : 0;
                      """);

    assertEquals(42, result.exitCode());
  }
}
