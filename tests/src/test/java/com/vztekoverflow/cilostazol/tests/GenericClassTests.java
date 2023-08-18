package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GenericClassTests extends TestBase {
  @Test
  public void test1() {
    var result =
        runTestFromCode(
            """
                      using System;

                      C<int> t = new D<int>();
                      Console.WriteLine(t.Foo(1));

                      class C<T1>
                      {
                        public virtual T1 Foo(T1 p1)
                        {
                          return p1;
                        }
                      }

                      class D<T1> : C<T1>
                      {}
                                            """);

    assertEquals(0, result.exitCode());
    assertEquals("1\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test2() {
    var result =
        runTestFromCode(
            """
                              using System;

                              C<int> t = new D<int>();
                              Console.WriteLine((int)t.Foo(1));

                              class C<T1>
                              {
                                public virtual object Foo(T1 p1)
                                {
                                  return (object)p1;
                                }
                              }

                              class D<T1> : C<T1>
                              {}
                                                    """);

    assertEquals(0, result.exitCode());
    assertEquals("1\n", result.output().replace("\r\n", "\n"));
  }
}
