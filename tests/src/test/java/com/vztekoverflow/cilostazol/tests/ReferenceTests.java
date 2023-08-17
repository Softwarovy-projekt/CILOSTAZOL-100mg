package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReferenceTests extends TestBase {
  @Test
  public void test1() {
    var result =
        runTestFromCode(
            """
using System;

int a = 1;
int b = 2;
ref int mRef = ref a;
ref int mbRef = ref b;
mRef = 3;
mRef = ref mbRef;
mRef = 4;

Console.WriteLine(a);
Console.WriteLine(b);
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("3\n4\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test2() {
    var result =
        runTestFromCode(
            """
using System;

Foo(1);

void Foo(int a)
{
    ref int mRef = ref a;
    mRef = 2;
    Console.WriteLine(a);
}
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("2\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test3() {
    var result =
        runTestFromCode(
            """
using System;

Foo(1);

void Foo(int a)
{
    ref int mRef = ref a;
    mRef = 2;
    Console.WriteLine(a);
}
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("2\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test4() {
    var result =
        runTestFromCode(
            """
using System;

int[] temp = new int[2];
temp[1] = 1;
ref int t = ref temp[1];
t = 2;
Console.WriteLine(temp[1]);
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("2\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test5() {
    var result =
        runTestFromCode(
            """
using System;

var t = new T();
ref int mRef = ref t.a;
mRef = 4;
Console.WriteLine(t.a);

class T
{
    public int a = 2;
}
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("4\n", result.output().replace("\r\n", "\n"));
  }
}
