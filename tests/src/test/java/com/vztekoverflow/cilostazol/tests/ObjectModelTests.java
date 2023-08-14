package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                    TestClass2 obj = new TestClass2();
                    obj.a = 42;
                    return obj.a;

                    public class TestClass
                    {
                        public int a;
                    }

                    public class TestClass2 : TestClass { }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void grandParentFieldAccess() {
    var result =
        runTestFromCode(
            """
                    TestClass3 obj = new TestClass3();
                    obj.a = 42;
                    obj.b = 42L;
                    obj.c = obj.a + obj.b;
                    return (int) (long) obj.c;

                    public class TestClass
                    {
                        public int a;
                    }

                    public class TestClass2 : TestClass
                    {
                        public long b;
                    }
                    public class TestClass3 : TestClass2
                    {
                        public object c;
                    }
                    """);

    assertEquals(84, result.exitCode());
  }

  @Test
  public void interfacePropertyAccess() {
    var result =
        runTestFromCode(
            """
                            TestClass obj = new TestClass();
                            obj.a = 42;
                            return obj.a;

                            public interface TestInterface
                            {
                                public int a { get; set; }
                            }

                            public class TestClass : TestInterface
                            {
                                public int a { get; set; }
                            }
                            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void interfaceTransitiveFieldAccess() {
    var result =
        runTestFromCode(
            """
                    TestClass2 obj = new TestClass2();
                    obj.a = 42;
                    return obj.a;

                    public interface TestInterface
                    {
                        public int a { get; set; }
                    }

                    public class TestClass : TestInterface
                    {
                        public int a { get; set; }
                    }

                    public class TestClass2 : TestClass { }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void sizeOfClassTest() {
    var result =
        runTestFromCode(
            """
                    unsafe
                    {
                        return sizeof(TestClass);
                    }

                    public class TestClass
                    {
                        public int a;
                        public object b;
                    }
                    """);

    assertEquals(4, result.exitCode());
  }

  @Test
  @Disabled("Requires handling padding (also on windows the reference is 8B when run with maven)")
  public void sizeOfStructTest() {
    var result =
        runTestFromCode(
            """
                    unsafe
                    {
                      return sizeof(TestStruct);
                    }

                    public struct TestStruct
                    {
                        public int a;
                        public object b;
                    }
                    """);

    assertEquals(8, result.exitCode());
  }

  @Test
  public void sizeOfPrimitivesTest() {
    var result =
        runTestFromCode(
            """
                    return sizeof(int) + sizeof(long) + sizeof(float)
                      + sizeof(double) + sizeof(char) + sizeof(bool);

                    """);

    assertEquals(4 + 8 + 4 + 8 + 2 + 1, result.exitCode());
  }
}
