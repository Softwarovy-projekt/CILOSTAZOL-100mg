package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  public void simpleClassInstanceCallWithArgsAndNullability() {
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
  public void callWithReturnStringOnTrue() {
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
                                Console.Write("TRUE");
                                return 42;
                            }
                            else
                            {
                                Console.Write("FALSE");
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
    assertEquals("TRUE", result.output());
  }

  @Test
  public void callWithReturnStringOnFalse() {
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
                              Console.Write("TRUE");
                              return 42;
                          }
                          else
                          {
                              Console.Write("FALSE");
                              return -42;
                          }
                      }

                      public static string Foo()
                      {
                          return "NOT Hello World!";
                      }
                  }
                }
                  """);

    assertEquals(-42, result.exitCode());
    assertEquals("FALSE", result.output());
  }

  @Test
  public void consoleWriteLine() {
    var result = runTestFromDll("ConsoleWriteLine");
    assertEquals(0, result.exitCode());
    assertEquals(
        String.format(
            "Hello World!%1$s42%1$s42.0%s1%1$s99%1$sMyString%1$sMyApp::WithoutOverride%1$s",
            System.lineSeparator()),
        result.output());
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

  @Test
  public void OverwrittenMethodFromAbstractPredecessorCall() {
    var result =
        runTestFromCode(
            """
                    using System;
                    namespace CallsTests;

                    public class Program
                    {
                        public static int Main()
                        {
                            return new B().Foo();
                        }
                    }

                    public abstract class A{
                        public int Foo()
                        {
                            Console.Write("A.Foo");
                            return 42;
                        }
                    }

                    public class B : A{
                        public int Foo(){
                            Console.Write("B.Foo");
                            return 52;
                        }
                    }

                    """);
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void CastedOverwrittenMethodFromAbstractPredecessorCall() {
    var result =
        runTestFromCode(
            """
                    namespace CallsTests;

                    public class Program
                    {
                        public static int Main()
                        {
                            return ((A)new B()).Foo();
                        }
                    }

                    public abstract class A{
                        public int Foo()
                        {
                            Console.Write("A.Foo");
                            return 42;
                        }
                    }

                    public class B : A{
                        public int Foo(){
                            Console.Write("B.Foo");
                            return 52;
                        }
                    }

                    """);
    assertEquals(42, result.exitCode());
    assertEquals("A.Foo", result.output());
  }

  @Test
  public void ExtensionMethodCall() {
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
                          return new A().Bar();
                      }
                  }

                  public class A{
                      public virtual int Foo(){
                          Console.Write("A.Foo");
                          return 42;
                      }
                  }

                  public static class AExtensions{
                      public static int Bar(this A a){
                          Console.Write("A.Bar");
                          return 43;
                      }
                  }
              }
                    """);
    assertEquals(43, result.exitCode());
    assertEquals("A.Bar", result.output());
  }
}
