package com.vztekoverflow.cilostazol.tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MultidimensionalArraysTests extends TestBase {

  @Test
  public void simpleMultidimensionalArray() {
    var result = runTestFromDll("MultidimensionalArrays");

    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleArray2D() {
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
                      var arr = new int[2, 2];
                      arr[0, 0] = 42;
                      return arr[0, 0];
                  }
              }
            }
            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleArray3D() {
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
                      var arr = new int[5, 8, 3];
                      arr[3, 4, 2] = 42;
                      return arr[3, 4, 2];
                  }
              }
            }
            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleArray5D() {
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
                      var arr = new int[2, 3, 2, 4, 2];
                      arr[1, 2, 1, 3, 1] = 42;
                      return arr[1, 2, 1, 3, 1];
                  }
              }
            }
            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void arrayTouchAllElementsBig() {
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
                    var arr = new int[2, 3, 2, 4, 2];
                    for(int i = 0; i < 2; i++)
                      for(int j = 0; j < 3; j++)
                        for(int k = 0; k < 2; k++)
                          for(int l = 0; l < 4; l++)
                            for(int m = 0; m < 2; m++)
                              arr[i, j, k, l, m] = i + j + k + l + m;
                    int sum = 0;
                    for(int i = 0; i < 2; i++)
                      for(int j = 0; j < 3; j++)
                        for(int k = 0; k < 2; k++)
                          for(int l = 0; l < 4; l++)
                            for(int m = 0; m < 2; m++)
                              sum += arr[i, j, k, l, m];
                    return sum;
                  }
              }
            }
            """);
    assertEquals(384, result.exitCode());
  }

  @Test
  public void arrayTouchAllElementsSmall() {
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
                    var arr = new int[2, 3];
                    for(int i = 0; i < 2; i++)
                      for(int j = 0; j < 3; j++)
                        arr[i, j] = i + j;
                    int sum = 0;
                    for(int i = 0; i < 2; i++)
                      for(int j = 0; j < 3; j++)
                        sum += arr[i, j];
                    return sum;
                  }
              }
            }
            """);

    assertEquals(9, result.exitCode());
  }

  @Test
  public void printAllElementsSmall() {
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
                            var counter = 1;
                            var arr = new int[2, 3];
                            for(int i = 0; i < 2; i++)
                              for(int j = 0; j < 3; j++)
                                  arr[i, j] = counter++;

                            foreach(var i in arr)
                              System.Console.Write(i);
                            return counter;
                          }
                      }
                    }
                    """);

    assertEquals(7, result.exitCode());
    assertEquals("123456", result.output());
  }

  @Test
  public void printAllElementsBig() {
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
                    var counter = 1;
                    var arr = new int[2, 3, 2, 4, 2];
                    for(int i = 0; i < 2; i++)
                      for(int j = 0; j < 3; j++)
                        for(int k = 0; k < 2; k++)
                          for(int l = 0; l < 4; l++)
                            for(int m = 0; m < 2; m++)
                              arr[i, j, k, l, m] = counter++;
                    foreach(var i in arr)
                      System.Console.Write(i);
                    return counter;
                  }
              }
            }
            """);
    assertEquals(97, result.exitCode());
    assertEquals(
        "123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596",
        result.output());
  }

  @Test
  public void checkRowBasedMemorySavingSmall() {
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
                            var arr = new int[2, 4];
                            for(int i = 0; i < 2; i++)
                              for(int j = 0; j < 4; j++)
                                  arr[i, j] = i;

                            foreach(var i in arr)
                              System.Console.Write(i);
                            return 42;
                          }
                      }
                    }
                    """);

    assertEquals(42, result.exitCode());
    assertEquals("00001111", result.output());
  }

  @Test
  public void checkRowBasedMemorySavingBig() {
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
                    var arr = new int[2, 3, 2, 4, 2];
                    for(int i = 0; i < 2; i++)
                      for(int j = 0; j < 3; j++)
                        for(int k = 0; k < 2; k++)
                          for(int l = 0; l < 4; l++)
                            for(int m = 0; m < 2; m++)
                              arr[i, j, k, l, m] = i + j + k + l;;

                    foreach(var i in arr)
                      System.Console.Write(i);
                    return 42;
                  }
              }
            }
            """);

    assertEquals(42, result.exitCode());
    assertEquals(
        "001122331122334411223344223344552233445533445566112233442233445522334455334455663344556644556677",
        result.output());
  }

  @Test
  public void testArrayEnumerationSmall() {
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
                    var arr = new int[2, 3];
                    for(int i = 0; i < 2; i++)
                      for(int j = 0; j < 3; j++)
                          arr[i, j] = i + j;
                    int sum = 0;
                    foreach(var i in arr)
                      sum += i;
                    return sum;
                  }
              }
            }
            """);

    assertEquals(9, result.exitCode());
  }

  @Test
  public void testArrayEnumerationBig() {
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
                    var counter = 1;
                    var arr = new int[2, 3, 2, 4, 2];
                    for(int i = 0; i < 2; i++)
                      for(int j = 0; j < 3; j++)
                        for(int k = 0; k < 2; k++)
                          for(int l = 0; l < 4; l++)
                            for(int m = 0; m < 2; m++)
                              arr[i, j, k, l, m] = counter++;
                    int sum = 0;
                    foreach(var i in arr)
                      sum += i;
                    return sum;
                  }
              }
            }
            """);

    assertEquals(4656, result.exitCode());
  }

  @Test
  public void simpleArray3DInitialization() {
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
                      var arr = new int[5, 8, 3];
                      return arr[3, 4, 2];
                  }
              }
            }
            """);

    assertEquals(0, result.exitCode());
  }

  @Test
  public void simpleArray3Dbool() {
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
                      var arr = new bool[5, 8, 3];
                      arr[3, 4, 2] = true;
                      if(arr[3, 4, 2])
                        return 42;
                      return -1;
                  }
              }
            }
            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void simpleArray3DboolInitialization() {
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
                      var arr = new bool[5, 8, 3];
                      if(arr[3, 4, 2])
                        return 42;
                      return -1;
                  }
              }
            }
            """);

    assertEquals(-1, result.exitCode());
  }

  @Test
  public void array2DObject() {
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
                    var arr = new A[5, 8];
                    var a = new A();
                    a.prop = 42;
                    arr[3, 4] = a;

                    return arr[3, 4].prop;
                }
            }

            class A {
              public int prop;
            }
          }
          """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void array2DObjectInitialization() {
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
                      var arr = new A[5, 8];
                      if (arr[3, 4] == null)
                        return 42;

                      return -1;
                  }
              }

              class A {
                public int prop;
              }
            }
            """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void array2DObjectInitialization2() {
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
                      var arr = new A[5, 8];
                      arr[0, 0] = new A();

                      if (arr[3, 4] == null)
                        return 42;

                      return -1;
                  }
              }

              class A {
                public int prop;
              }
            }
            """);

    assertEquals(42, result.exitCode());
  }
}
