package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NonVirtualCallsTests extends TestBase {
  @Test
  public void simpleStaticCall() {
    var result =
        runTestFromCode(
            """
          using System;
          namespace CustomTest
          {
            public class Program
            {
                public static int Main()
                {
                    return Foo();
                }

                public static int Foo()
                {
                    return 42;
                }
            }
          }
            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleStructInstanceCall() {
    var result =
        runTestFromCode(
            """
                            var obj = new TestStruct();
                            return obj.Foo();

                            public struct TestStruct
                            {
                                public int a;
                                public object b;

                                public int Foo()
                                {
                                    return 42;
                                }
                            }
                            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleStaticCallWithArgs() {
    var result =
        runTestFromCode(
            """
          using System;
          namespace CustomTest
          {
            public class Program
            {
                public static int Main()
                {
                    return Foo(42);
                }

                public static int Foo(int x)
                {
                    return x;
                }
            }
          }
            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleStructInstanceCallWithArgs() {
    var result =
        runTestFromCode(
            """
                    var obj = new TestStruct();
                    return obj.Foo(42);

                    public struct TestStruct
                    {
                        public int a;
                        public object b;

                        public int Foo(int x)
                        {
                            return x;
                        }
                    }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  @Disabled(
      "Missing branch for klass.getTableId() == CLI_TABLE_TYPE_SPEC in getMethodSymbolFromMemberRef")
  public void simpleClassInstanceCallWithArgs() {
    var result =
        runTestFromCode(
            """
                    var obj = new TestClass();
                    return (int) obj?.Foo(42);

                    public class TestClass
                    {
                        public int Foo(int x)
                        {
                            return x;
                        }
                    }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  @Disabled("WIP. missing comparison of strings")
  public void callWithReturnString() {
    var result =
        runTestFromCode(
            """
                  using System;
                  namespace CustomTest
                  {
                    public class Program
                    {
                        public static int Main()
                        {
                            var result = Foo();

                            if (result == "Hello World!")
                            {
                                return 42;
                            }
                            else
                            {
                                return -42;
                            }
                        }

                        public static string Foo()
                        {
                            return "Hello World!";
                        }
                    }
                  }
                    """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void consoleWriteLine() {
    var result = runTestFromDll("ConsoleWriteLine");
    assertEquals(0, result.exitCode());
    assertEquals(String.format("Hello World!%s", System.lineSeparator()), result.output());
  }

  @Test
  public void nestedConsoleWriteLine() {
    var launcher =
        runTestFromCode(
            """
          using System;
          namespace CustomTest
          {
            public class Program
            {
                public static void Main()
                {
                    Foo();
                }

                public static void Foo()
                {
                    Console.WriteLine("Hello World!");
                }
            }
          }
            """);

    assertEquals(0, launcher.exitCode());
    assertEquals(String.format("Hello World!%s", System.lineSeparator()), launcher.output());
  }

  @Test
  public void printArgumentsFromMain() {
    var launcher =
        runTestFromCode(
            """
                  using System;
                  namespace CustomTest
                  {
                    public class Program
                    {
                        public static void Main(string[] args)
                        {
                            Console.WriteLine(args[0]);
                        }
                    }
                  }
                    """,
            new String[] {"Hello World!"});

    assertEquals(0, launcher.exitCode());
    assertEquals(String.format("Hello World!%s", System.lineSeparator()), launcher.output());
  }
}
