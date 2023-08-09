package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ComparisonAndBranchingTests extends TestBase {
  @Test
  public void binaryLessThanInt32() {
    var result =
        runTestFromCode(
            """
                int a = 42;
                if (a < 43)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
                """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryLessThanInt64() {
    var result =
        runTestFromCode(
            """
                long a = 42_000_000_000_000_000L;
                if (a < 43_000_000_000_000_000L)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
                """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryLessThanFloat32() {
    var result =
        runTestFromCode(
            """
                    float a = 1.0f;
                    if (a < 43.0f)
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                    """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryLessThanFloat64() {
    var result =
        runTestFromCode(
            """
                    double a = 1.0;
                    if (a < 43.0d)
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                    """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryLessThanEqualsInt32() {
    var result =
        runTestFromCode(
            """
                int a = 42;
                if (a <= 42)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
                """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryLessThanEqualsInt64() {
    var result =
        runTestFromCode(
            """
                  long a = 42_000_000_000_000_000L;
                  if (a <= 42_000_000_000_000_000L)
                  {
                      return 1;
                  }
                  else
                  {
                      return 0;
                  }
                  """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryLessThanEqualsFloat32() {
    var result =
        runTestFromCode(
            """
                      float a = 1.0f;
                      if (a <= 1.0f)
                      {
                          return 1;
                      }
                      else
                      {
                          return 0;
                      }
                      """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryLessThanEqualsFloat64() {
    var result =
        runTestFromCode(
            """
                        double a = 1.0;
                        if (a <= 1.0d)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryGreaterThanInt32() {
    var result =
        runTestFromCode(
            """
                    int a = 42;
                    if (a > 41)
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                    """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryGreaterThanInt64() {
    var result =
        runTestFromCode(
            """
                    long a = 42_000_000_000_000_000L;
                    if (a > 41_000_000_000_000_000L)
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                    """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryGreaterThanFloat32() {
    var result =
        runTestFromCode(
            """
                        float a = 1.0f;
                        if (a > 0.0f)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryGreaterThanFloat64() {
    var result =
        runTestFromCode(
            """
                        double a = 1.0;
                        if (a > 0.0d)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryGreaterThanEqualsInt32() {
    var result =
        runTestFromCode(
            """
                        int a = 42;
                        if (a >= 42)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryGreaterThanEqualsInt64() {
    var result =
        runTestFromCode(
            """
                        long a = 42_000_000_000_000_000L;
                        if (a >= 42_000_000_000_000_000L)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryGreaterThanEqualsFloat32() {
    var result =
        runTestFromCode(
            """
                            float a = 1.0f;
                            if (a >= 1.0f)
                            {
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                            """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryGreaterThanEqualsFloat64() {
    var result =
        runTestFromCode(
            """
                            double a = 1.0;
                            if (a >= 1.0d)
                            {
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                            """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryEqualsInt32() {
    var result =
        runTestFromCode(
            """
                        int a = 42;
                        if (a == 42)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryEqualsInt64() {
    var result =
        runTestFromCode(
            """
                        long a = 42_000_000_000_000_000L;
                        if (a == 42_000_000_000_000_000L)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryEqualsFloat32() {
    var result =
        runTestFromCode(
            """
                        float a = 1.0f;
                        if (a == 1.0f)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryEqualsFloat64() {
    var result =
        runTestFromCode(
            """
                        double a = 1.0;
                        if (a == 1.0d)
                        {
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                        """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryNotEqualsInt32() {
    var result =
        runTestFromCode(
            """
                            int a = 42;
                            if (a != 41)
                            {
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                            """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryNotEqualsInt64() {
    var result =
        runTestFromCode(
            """
                            long a = 42_000_000_000_000_000L;
                            if (a != 41_000_000_000_000_000L)
                            {
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                            """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryNotEqualsFloat32() {
    var result =
        runTestFromCode(
            """
                            float a = 1.0f;
                            if (a != 0.0f)
                            {
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                            """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void binaryNotEqualsFloat64() {
    var result =
        runTestFromCode(
            """
                            double a = 1.0;
                            if (a != 0.0d)
                            {
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                            """);

    assertEquals(1, result.exitCode());
  }

  private static Stream<Arguments> switchParameters() {
    return Stream.of(
        Arguments.of("int a = 1;", 10),
        Arguments.of("int a = 2;", 20),
        Arguments.of("int a = 3;", 30),
        Arguments.of("int a = 0;", 40) // default
        );
  }

  @ParameterizedTest
  @MethodSource("switchParameters")
  public void switchStatement(String input, int expected) {
    var result =
        runTestFromCode(
            input
                + """
            switch (a)
            {
                case 1:
                    return 10;
                case 2:
                    return 20;
                case 3:
                    return 30;
                default:
                    return 40;
            }
            """);
    assertEquals(expected, result.exitCode());
  }

  // TODO: Test reference comparison once new object creation is implemented
}
