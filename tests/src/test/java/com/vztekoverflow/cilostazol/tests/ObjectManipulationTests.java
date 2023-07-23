package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ObjectManipulationTests extends TestBase {
  @Test
  public void createObject() {
    var result =
        runTestFromCode(
            """
                            var obj = new TestClass();
                            return 0;

                            public class TestClass { }
                            """);

    assertEquals(0, result.exitCode());
  }

  @Test
  public void initClass() {
    var result =
        runTestFromCode(
            """
                            TestClass obj = default;

                            if (obj != null)
                                return obj.a;

                            return 1;

                            public class TestClass
                            {
                                public int a;
                                public object b;
                            }
                            """);

    assertEquals(1, result.exitCode());
  }

  @Test
  public void initStruct() {
    var result =
        runTestFromCode(
            """
                            TestStruct obj = default;
                            return obj.a;

                            public struct TestStruct
                            {
                                public int a;
                                public object b;
                            }
                            """);

    assertEquals(0, result.exitCode());
  }
}
