package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ArrayTests extends TestBase {
  @Test
  public void test1() {
    var result =
        runTestFromCode(
            """
using System;

int[] temp = new int[2];
Console.WriteLine(temp.Length);
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("2\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test2() {
    var result =
        runTestFromCode(
            """
using System;

int[] temp = new int[2];
Console.WriteLine(temp[0]);
Console.WriteLine(temp[1]);
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("0\n0\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test3() {
    var result =
        runTestFromCode(
            """
using System;

int[] temp = new int[2];
temp[0] = 1;
temp[1] = temp[0];
Console.WriteLine(temp[0]);
Console.WriteLine(temp[1]);
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("1\n1\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void test4() {
    var result =
        runTestFromCode(
            """
using System;

object[] temp = new object[2];
if (temp[0] == null)
Console.WriteLine("T");
                            """);

    assertEquals(0, result.exitCode());
    assertEquals("T\n", result.output().replace("\r\n", "\n"));
  }
}
