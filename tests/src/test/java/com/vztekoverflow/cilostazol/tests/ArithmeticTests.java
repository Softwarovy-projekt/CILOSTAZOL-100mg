package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ArithmeticTests extends TestBase {
  @Test
  public void test1() {
    var result =
        runTestFromCode(
            """
using System;

int a = 5;
int b = 3;
var plus = a + b;
var mul = a * b;
var div = a / b;
var rem = a % b;
var minus = a - b;
var and = a & b;
var or = a | b;
var xor = a ^ b;
var rshift = a >> b;
var lshift = a << b;
Console.WriteLine(plus);
Console.WriteLine(mul);
Console.WriteLine(div);
Console.WriteLine(rem);
Console.WriteLine(minus);
Console.WriteLine(and);
Console.WriteLine(or);
Console.WriteLine(xor);
Console.WriteLine(rshift);
Console.WriteLine(lshift);
                    """);

    assertEquals(0, result.exitCode());
    assertEquals("8\n15\n1\n2\n2\n1\n7\n6\n0\n40\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test2() {
    var result =
        runTestFromCode(
            """
using System;

long a = 5;
long b = 3;
int c = 4;
var plus = a + b;
var mul = a * b;
var div = a / b;
var rem = a % b;
var minus = a - b;
var and = a & b;
var or = a | b;
var xor = a ^ b;
var rshift = a >> c;
var lshift = a << c;
Console.WriteLine(plus);
Console.WriteLine(mul);
Console.WriteLine(div);
Console.WriteLine(rem);
Console.WriteLine(minus);
Console.WriteLine(and);
Console.WriteLine(or);
Console.WriteLine(xor);
Console.WriteLine(rshift);
Console.WriteLine(lshift);
                    """);

    assertEquals(0, result.exitCode());
    assertEquals("8\n15\n1\n2\n2\n1\n7\n6\n0\n80\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test3() {
    var result =
        runTestFromCode(
            """
using System;

uint a = 5;
uint b = 3;
int c = 4;
var plus = a + b;
var mul = a * b;
var div = a / b;
var rem = a % b;
var minus = a - b;
var and = a & b;
var or = a | b;
var xor = a ^ b;
var rshift = a >> c;
var lshift = a << c;
Console.WriteLine(plus);
Console.WriteLine(mul);
Console.WriteLine(div);
Console.WriteLine(rem);
Console.WriteLine(minus);
Console.WriteLine(and);
Console.WriteLine(or);
Console.WriteLine(xor);
Console.WriteLine(rshift);
Console.WriteLine(lshift);
                    """);

    assertEquals(0, result.exitCode());
    assertEquals("8\n15\n1\n2\n2\n1\n7\n6\n0\n80\n", result.output().replace("\r\n", "\n"));
  }
}
