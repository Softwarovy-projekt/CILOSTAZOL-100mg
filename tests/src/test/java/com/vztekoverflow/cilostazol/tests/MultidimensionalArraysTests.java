package com.vztekoverflow.cilostazol.tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultidimensionalArraysTests extends TestBase {

  @Test
  public void simpleMultidimensionalArray() {
    var result = runTestFromDll("MultidimensionalArrays");

    assertEquals(42, result.exitCode());
  }

}
