package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ObjectModelTests extends TestBase {
  @Test
  public void createObject() {
    var result =
        runTestFromCode(
            """
                    var obj = new TestClass();
                    return obj.a;

                    public class TestClass
                    {
                        public int a;
                        public object b;
                    }
                    """);

    assertEquals(0, result.exitCode());
  }

  @Test
  public void createObjectDefaultConstructor() {
    var result =
        runTestFromCode(
            """
                    var obj = new TestClass();
                    return obj.a;

                    public class TestClass
                    {
                        public int a;
                        public object b;

                        public TestClass()
                        {
                            a = 1;
                        }
                    }
                    """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void createObjectExplicitConstructor() {
    var result =
        runTestFromCode(
            """
                    var obj = new TestClass(42);
                    return obj.a;

                    public class TestClass
                    {
                        public int a;
                        public object b;

                        public TestClass(int value)
                        {
                            a = value;
                        }
                    }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void accessStaticFieldWithStaticConstructor() {
    var result =
        runTestFromCode(
            """
                    return TestClass.a;

                    public class TestClass
                    {
                        public static int a;

                        static TestClass()
                        {
                            a = 42;
                        }
                    }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void accessStaticFieldAfterInitialization() {
    var result =
        runTestFromCode(
            """
                            return TestClass.a;

                            public class TestClass
                            {
                                public static int a = 42;
                            }
                            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void initClass() {
    var result =
        runTestFromCode(
            """
                    TestClass obj = default;

                    if (obj != null)
                        return obj.a;

                    return 1;

                    public class TestClass
                    {
                        public int a;
                        public object b;
                    }
                    """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void initStruct() {
    var result =
        runTestFromCode(
            """
                    TestStruct obj = default;
                    return obj.a;

                    public struct TestStruct
                    {
                        public int a;
                    }
                    """);

    assertEquals(0, result.exitCode());
  }

  @Test
  public void fieldAccessInt32() {
    var result =
        runTestFromCode(
            """
                    TestStruct obj = default;
                    obj.a = 42;
                    return obj.a;

                    public struct TestStruct
                    {
                        public int a;
                    }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void fieldAccessInt64() {
    var result =
        runTestFromCode(
            """
                      TestStruct obj = default;
                      obj.a = 42L;
                      if (obj.a == 42L)
                        return 42;

                      return 0;

                      public struct TestStruct
                      {
                          public long a;
                      }
                      """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void fieldAccessFloat32() {
    var result =
        runTestFromCode(
            """
                      TestStruct obj = default;
                      obj.a = 42;

                      if (obj.a == 42)
                        return 42;

                      return 0;

                      public struct TestStruct
                      {
                          public float a;
                      }
                      """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void fieldAccessFloat64() {
    var result =
        runTestFromCode(
            """
                      TestStruct obj = default;
                      obj.a = 42;

                      if (obj.a == 42)
                        return 42;

                      return 0;
                      public struct TestStruct
                      {
                          public double a;
                      }
                      """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void fieldAccessChar() {
    var result =
        runTestFromCode(
            """
                      TestStruct obj = default;
                      obj.a = 'a';
                      return obj.a;

                      public struct TestStruct
                      {
                          public char a;
                      }
                      """);

    assertEquals('a', result.exitCode());
  }

  @Test
  public void fieldAccessBoolean() {
    var result =
        runTestFromCode(
            """
                      TestStruct obj = default;
                      obj.a = true;
                      if (obj.a)
                        return 1;

                      return 0;

                      public struct TestStruct
                      {
                          public bool a;
                      }
                      """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void fieldAccessObject() {
    var result =
        runTestFromCode(
            """
                    TestStruct obj = default;
                    obj.a = default;
                    if (obj.a == default(object))
                        return 1;

                    return 0;

                    public struct TestStruct
                    {
                        public object a;
                    }
                    """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void staticFieldAccessInt32() {
    var result =
        runTestFromCode(
            """
                    TestStruct.a = 42;
                    return TestStruct.a;

                    public struct TestStruct
                    {
                        public static int a;
                    }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void staticFieldAccessInt64() {
    var result =
        runTestFromCode(
            """
                    TestStruct.a = 42L;
                    if (TestStruct.a == 42L)
                        return 42;

                    return 0;

                    public struct TestStruct
                    {
                        public static long a;
                    }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void staticFieldAccessFloat32() {
    var result =
        runTestFromCode(
            """
                        TestStruct.a = 42;
                        if (TestStruct.a == 42)
                            return 42;

                        return 0;

                        public struct TestStruct
                        {
                            public static float a;
                        }
                        """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void staticFieldAccessFloat64() {
    var result =
        runTestFromCode(
            """
                        TestStruct.a = 42;
                        if (TestStruct.a == 42)
                            return 42;

                        return 0;

                        public struct TestStruct
                        {
                            public static double a;
                        }
                        """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void staticFieldAccessChar() {
    var result =
        runTestFromCode(
            """
                    TestStruct.a = 'a';
                    return TestStruct.a;

                    public struct TestStruct
                    {
                        public static char a;
                    }
                    """);

    assertEquals('a', result.exitCode());
  }

  @Test
  @Disabled("Branching is not taken into account during static analysis")
  public void staticFieldAccessBoolean() {
    var result =
        runTestFromCode(
            """
                        TestStruct.a = true;
                        if (TestStruct.a)
                            return 1;

                        return 0;

                        public struct TestStruct
                        {
                            public static bool a;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void staticFieldAccessObject() {
    var result =
        runTestFromCode(
            """
                        TestStruct.a = default;
                        if (TestStruct.a == default(object))
                            return 1;

                        return 0;

                        public struct TestStruct
                        {
                            public static object a;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void parentFieldAccess() {
    var result =
        runTestFromCode(
            """
                    TestStruct2 obj = new TestStruct2();
                    obj.a = 42;
                    return obj.a;

                    public class TestStruct
                    {
                        public int a;
                    }

                    public class TestStruct2 : TestStruct { }
                    """);

    assertEquals(42, result.exitCode());
  }
}
