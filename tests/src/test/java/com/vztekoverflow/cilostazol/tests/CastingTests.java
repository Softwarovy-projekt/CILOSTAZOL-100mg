package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.graalvm.polyglot.PolyglotException;
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

  @Test
  public void castClassInvalid() {
    try {
      runTestFromCode(
          """
                    object a = new object();
                    string b = (string)a;

                    return b == null ? 42 : 0;
                    """);
    } catch (PolyglotException e) {
      assertTrue(e.getMessage().contains("InvalidCastException"));
      return;
    }

    fail("Expected InvalidCastException");
  }

  @Test
  public void castClassValid() {
    var result =
        runTestFromCode(
            """
                    A objB = new B();
                    return ((B)objB).a;

                    public class A
                    {
                        public int a = 42;
                    }

                    public class B : A {}
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void isInstance() {
    var result =
        runTestFromCode(
            """
                            TestClass obj = new TestClass();
                            if (obj.b is int)
                                return 42;

                            return 41;

                            public class TestClass
                            {
                                public int a;
                                public object b;

                                public TestClass()
                                {
                                    b = 42;
                                }
                            }
                            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void boxUnboxStruct() {
    var result =
        runTestFromCode(
            """
                            object obj = new TestStruct();
                            int b = ((TestStruct)obj).a;
                            return b;

                            public struct TestStruct
                            {
                                public int a;
                            }
                            """);

    assertEquals(0, result.exitCode());
  }

  @Test
  public void boxUnboxPrimitives() {
    var result =
        runTestFromCode(
            """
                      object obj = 1;
                      int b = (int)obj;
                      obj = 1L;
                      long c = (long)obj;
                      obj = 1.0f;
                      float d = (float)obj;
                      obj = 1.0;
                      double e = (double)obj;

                      return b + (int)c + (int)d + (int)e;
                      """);

    assertEquals(4, result.exitCode());
  }
}
