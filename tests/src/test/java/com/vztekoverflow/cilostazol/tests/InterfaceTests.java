package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class InterfaceTests extends TestBase {
  @Test
  public void test1() {
    var result =
        runTestFromCode(
            """
using System;

var t = new T();
IA ta = t;
IB tb = t;
ta.Foo(1);
tb.Foo(1);

interface IA
{
    void Foo(int a) {}
}

interface IB
{
    void Foo(int b) {}
}

class T : IA, IB
{
    void IA.Foo(int a)
    {
        Console.WriteLine("IA");
    }

    void IB.Foo(int b)
    {
        Console.WriteLine("IB");
    }
}
                                    """);

    assertEquals(0, result.exitCode());
    assertEquals("IA\nIB\n", result.output().replace("\r\n", "\n"));
  }
}
