package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ConsoleTests extends TestBase {
  @Test
  public void consoleWriteBoolean() {
    var result =
        runTestFromCode(
            """
                        using System;
                        Console.Write(true);
                        Console.Write(false);
                        """);

    assertEquals("TrueFalse", result.output());
  }

  @Test
  public void consoleWriteChar() {
    var result =
        runTestFromCode(
            """
                        using System;
                        Console.Write('a');
                        Console.Write('b');
                        """);

    assertEquals("ab", result.output());
  }

  @Test
  public void consoleWriteArray() {
    var result =
        runTestFromCode(
            """
                        using System;
                        char[] arr = new char[] { 'a', 'b' };
                        Console.Write(arr);
                        """);

    assertEquals("ab", result.output());
  }

  @Test
  public void consoleWriteInt() {
    var result =
        runTestFromCode(
            """
                        using System;
                        Console.Write(1);
                        Console.Write(2);
                        """);

    assertEquals("12", result.output());
  }

  @Test
  public void consoleWriteString() {
    var result =
        runTestFromCode(
            """
                        using System;
                        Console.Write("ab");
                        """);

    assertEquals("ab", result.output());
  }

  @Test
  public void consoleWriteObject() {
    var result =
        runTestFromCode(
            """
                        using System;
                        Console.Write(new object());
                        """);

    assertEquals("System.Object", result.output());
  }

  @Test
  public void consoleWriteLineString() {
    var result =
        runTestFromCode(
            """
                        using System;
                        Console.WriteLine("ab");
                        Console.WriteLine();
                        """);

    assertEquals("ab\n\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void consoleWriteLineObjectOverride() {
    var result =
        runTestFromCode(
            """
                        using System;
                        Console.WriteLine(new MyObject());

                        class MyObject {
                            public override string ToString() {
                                return "MyObject";
                            }
                        }
                        """);

    assertEquals("MyObject\n", result.output().replace("\r\n", "\n"));
  }
}
