package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GenericMethodTests extends TestBase {
  @Test
  public void test1() {
    var result =
        runTestFromCode(
            """
              using System;

              Console.WriteLine(T.Foo(1));
              Console.WriteLine(T.Foo("Hi"));

              class T {

                  public static T Foo<T>(T a) {
                      return a;
                  }
              }
                                    """);

    assertEquals(0, result.exitCode());
    assertEquals("1\nHi\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test2() {
    var result =
        runTestFromCode(
            """
                      using System;

                      var a = T.Foo(1);
                      Console.WriteLine((int)a);

                      class T {

                          public static object Foo<T>(T a) {
                              return (object)a;
                          }
                      }
                                            """);
  }

  @Test
  public void test3() {
    var result =
        runTestFromCode(
            """
                                using System;

                                T.Foo(new B());

                                class T {

                                    public static void Foo<T>(T a) where T : A {
                                        a.Foo();
                                    }
                                }

                                class A
                                {
                                  public virtual void Foo()
                                  {
                                    Console.WriteLine("A");
                                  }
                                }
                                class B : A
                                {
                                  public override void Foo()
                                  {
                                    Console.WriteLine("B");
                                  }
                                }
                                                      """);

    assertEquals(0, result.exitCode());
    assertEquals("B\n", result.output().replace("\r\n", "\n"));
  }
}
