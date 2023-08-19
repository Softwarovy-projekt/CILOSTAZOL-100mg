package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.Test;

public class ExceptionTests extends TestBase {
  @Test
  public void simpleEx1() {
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
                                        try
                                        {
                                          throw new Exception();
                                        }
                                        catch(Exception ex)
                                        {
                                          Console.Write("1");
                                        }

                                        return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("1", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx2() {
    try {
      runTestFromCode(
          """
                        using System;
                        namespace CustomTest
                        {
                            public class Program
                            {
                                public static int Main()
                                {
                                    throw new Exception();
                                }
                            }
                        }
                          """);
    } catch (PolyglotException e) {
      assertTrue(e.getMessage().contains("RuntimeCILException"));
      return;
    }

    fail("Expected exception");
  }

  @Test
  public void simpleEx3() {
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
                                        try
                                        {
                                          Console.Write("1");
                                        }
                                        catch(Exception ex)
                                        {
                                          Console.Write("2");
                                        }

                                        return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("1", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx4() {
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
                                        try
                                        {
                                          Console.Write("1");
                                        }
                                        finally
                                        {
                                          Console.Write("2");
                                        }

                                        return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("12", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx5() {
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
                                      try {
                                        try
                                        {
                                          Console.Write("1");
                                          throw new Exception();
                                        }
                                        finally
                                        {
                                          Console.Write("2");
                                        }
                                      }
                                      catch(Exception ex)
                                      {
                                        Console.Write("3");
                                      }

                                      return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("123", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx6() {
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
                                      try
                                      {
                                        Console.Write("1");
                                        throw new Exception();
                                      }
                                      catch(Exception ex)
                                      {
                                        Console.Write("2");
                                      }
                                      finally
                                      {
                                        Console.Write("3");
                                      }

                                      return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("123", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx7() {
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
                                      try
                                      {
                                        try
                                        {
                                          Console.Write("1");
                                          throw new Exception();
                                        }
                                        catch(NotImplementedException ex)
                                        {
                                          Console.Write("2");
                                        }
                                        finally
                                        {
                                          Console.Write("3");
                                        }
                                      }
                                      catch (Exception ex)
                                      {
                                        Console.Write("4");
                                      }

                                      return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("134", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx8() {
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
                                      try
                                      {
                                        try
                                        {
                                          Console.Write("1");
                                          throw new Exception();
                                        }
                                        catch(Exception ex)
                                        {
                                          Console.Write("2");
                                        }
                                        finally
                                        {
                                          Console.Write("3");
                                        }
                                      }
                                      catch (Exception ex)
                                      {
                                        Console.Write("4");
                                      }

                                      return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("123", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx9() {
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
                                      try
                                      {
                                        try
                                        {
                                          Console.Write("1");
                                          throw new Exception();
                                        }
                                        catch(Exception ex)
                                        {
                                          Console.Write("2");
                                          throw;
                                        }
                                        finally
                                        {
                                          Console.Write("3");
                                        }
                                      }
                                      catch (Exception ex)
                                      {
                                        Console.Write("4");
                                      }

                                      return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("1234", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx10() {
    var result =
        runTestFromCode(
            """
                            using System;
                            namespace CustomTest
                            {
                                public class Program
                                {
                                    public static void Foo()
                                    {
                                      throw new Exception();
                                    }

                                    public static int Main()
                                    {
                                      try
                                      {
                                        try
                                        {
                                          Console.Write("1");
                                          Foo();
                                        }
                                        catch(Exception ex)
                                        {
                                          Console.Write("2");
                                          throw;
                                        }
                                        finally
                                        {
                                          Console.Write("3");
                                        }
                                      }
                                      catch (Exception ex)
                                      {
                                        Console.Write("4");
                                      }

                                      return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("1234", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx11() {
    var result =
        runTestFromCode(
            """
                            using System;
                            namespace CustomTest
                            {
                                public class Program
                                {
                                    public class A\s
                                    {
                                      public void Foo() {}
                                    }
                                    public static int Main()
                                    {
                                        try
                                        {
                                          int a = 1;
                                          int b = 0;
                                          int c = a / b;
                                        }
                                        catch(DivideByZeroException ex)
                                        {
                                          Console.Write("1");
                                        }
                                        catch(NotImplementedException ex)
                                        {
                                          Console.Write("2");
                                        }
                                        try
                                        {
                                          int a = int.MaxValue;
                                          checked
                                          {
                                              a = a + 3;
                                          }
                                        }
                                        catch(OverflowException ex)
                                        {
                                          Console.Write("1");
                                        }
                                        catch(NotImplementedException ex)
                                        {
                                          Console.Write("2");
                                        }

                                        return 42;
                                    }
                                }
                            }
                              """);
    assertEquals("11", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx15() {
    var result =
        runTestFromCode(
            """
                                    using System;
                                    namespace CustomTest
                                    {
                                        public class Program
                                        {
                                            public class A\s
                                            {
                                              public void Foo() {}
                                            }
                                            public static int Main()
                                            {
                                                try
                                                {
                                                  uint a = uint.MaxValue;
                                                  checked
                                                  {
                                                      a = a + 3;
                                                  }
                                                }
                                                catch(OverflowException ex)
                                                {
                                                  Console.Write("1");
                                                }
                                                catch(NotImplementedException ex)
                                                {
                                                  Console.Write("2");
                                                }

                                                return 42;
                                            }
                                        }
                                    }
                                      """);
    assertEquals("1", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx12() {
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
                                              try
                                              {
                                                int[] a = new int[2];
                                                a[3] = 1;
                                                Console.WriteLine(a[1]);
                                              }
                                              catch(IndexOutOfRangeException ex)
                                              {
                                                Console.Write("1");
                                              }
                                              catch(NotImplementedException ex)
                                              {
                                                Console.Write("2");
                                              }
                                                return 42;
                                            }
                                        }
                                    }
                                      """);
    assertEquals("1", result.output());
    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleEx13() {
    var result =
        runTestFromCode(
            """
                                  try
                                  {
                                    object a = new object();
                                    Console.WriteLine((string)a);
                                  }
                                  catch(InvalidCastException ex)
                                  {
                                    Console.Write("1");
                                  }
                                  catch(NotImplementedException ex)
                                  {
                                    Console.Write("2");
                                  }
                                      """);
    assertEquals("1", result.output());
  }

  @Test
  public void simpleEx14() {
    var result =
        runTestFromCode(
            """
                                    using System;
                                    namespace CustomTest
                                    {
                                        public class A
                                        {
                                          public void Foo() {}
                                        }
                                        public class Program
                                        {

                                            public static int Main()
                                            {
                                              try
                                              {
                                                A a = null;
                                                a.Foo();
                                              }
                                              catch(NullReferenceException ex)
                                              {
                                                Console.Write("1");
                                              }
                                              catch(NotImplementedException ex)
                                              {
                                                Console.Write("2");
                                              }

                                                return 42;
                                            }
                                        }
                                    }
                                      """);
    assertEquals("1", result.output());
    assertEquals(42, result.exitCode());
  }
}
