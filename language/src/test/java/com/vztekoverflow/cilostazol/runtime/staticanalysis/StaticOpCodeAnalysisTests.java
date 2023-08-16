package com.vztekoverflow.cilostazol.runtime.staticanalysis;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.staticanalysis.StaticOpCodeAnalyser;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StaticOpCodeAnalysisTests extends StaticAnalysisTestBase {

  // just to make junit shut up about missing tests...
  public void test() {}

  @Test
  public void LoadTwoInt32_Add_SaveToInt32() {
    /*
    public void LoadTwoInt32_Add_SaveToInt32(){
      int a = 1;
      int b = 2;
      int c = a + b;
    }
     */
    MethodSymbol method = getMethodSymbol("LoadTwoInt32_Add_SaveToInt32", "BinaryOperationsTest");
    /*
      IL_0000: nop
      IL_0001: ldc.i4.1
      IL_0002: stloc.0     //this should be int32
      IL_0003: ldc.i4.2
      IL_0004: stloc.1     //this should be int32
      IL_0005: ldloc.0
      IL_0006: ldloc.1
      IL_0007: add        //this should be int32
      IL_0008: stloc.2    //this should be int32
      IL_0009: ret
    */
    var opCodeTypes = method.getOpCodeTypes();

    assertEquals(10, opCodeTypes.length);
    assertEquals(
        4,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Int32).count());

    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[2]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[4]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[7]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[8]);
  }

  @Test
  public void LoadTwoByte_Add_SaveToInt32() {
    /*
    public void LoadTwoByte_Add_SaveToInt32(){
        byte a = 1;
        byte b = 2;
        int c = a + b;
    }
     */
    MethodSymbol method = getMethodSymbol("LoadTwoByte_Add_SaveToInt32", "BinaryOperationsTest");
    /*
        IL_0000: nop
        IL_0001: ldc.i4.1
        IL_0002: stloc.0    //this is int32
        IL_0003: ldc.i4.2
        IL_0004: stloc.1    //this is int32
        IL_0005: ldloc.0
        IL_0006: ldloc.1
        IL_0007: add        //this is int32
        IL_0008: stloc.2    //this is int32
        IL_0009: ret
    */
    var opCodeTypes = method.getOpCodeTypes();

    assertEquals(10, opCodeTypes.length);
    assertEquals(
        4,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Int32).count());

    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[2]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[4]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[7]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[8]);
  }

  @Test
  public void LoadByteAndLong_Add_SaveToLong() {
    /*
    public void LoadByteAndLong_Add_SaveToLong(){
        byte a = 1;
        long b = 2;
        long c = a + b;
    }
    */
    MethodSymbol method = getMethodSymbol("LoadByteAndLong_Add_SaveToLong", "BinaryOperationsTest");
    /*
        IL_0000: nop
        IL_0001: ldc.i4.1
        IL_0002: stloc.0   //this is int32
        IL_0003: ldc.i4.2
        IL_0004: conv.i8  //this is int32
        IL_0005: stloc.1  //this is int64
        IL_0006: ldloc.0
        IL_0007: conv.u8  //this is int32
        IL_0008: ldloc.1
        IL_0009: add      //this is int64
        IL_000a: stloc.2  //this is int64
        IL_000b: ret
    */
    var opCodeTypes = method.getOpCodeTypes();

    assertEquals(12, opCodeTypes.length);
    assertEquals(
        3,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Int32).count());
    assertEquals(
        3,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Int64).count());

    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[2]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[4]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[7]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int64, opCodeTypes[5]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int64, opCodeTypes[9]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int64, opCodeTypes[10]);
  }

  @Test
  public void BrFalse() {
    /*
    public int brfalse(){
        int a = 1;
        if (a < 100)
            return 42;
        return 10;
    }
    */
    MethodSymbol method = getMethodSymbol("BrFalse", "BranchOperationsTest");
    /*
        //IL_0000: nop
        //IL_0001: ldc.i4.1
        //IL_0002: stloc.0
        //IL_0003: ldloc.0
        //IL_0004: ldc.i4.s 100
        //IL_0006: clt
        //IL_0008: stloc.1
        // sequence point: hidden
        //IL_0009: ldloc.1
        //IL_000a: brfalse.s IL_0011

        IL_000c: ldc.i4.s 42
        IL_000e: stloc.2
        IL_000f: br.s IL_0016

        IL_0011: ldc.i4.s 10
        IL_0013: stloc.2
        IL_0014: br.s IL_0016

        IL_0016: ldloc.2
        IL_0017: ret
    */
    var opCodeTypes = method.getOpCodeTypes();

    assertEquals(24, opCodeTypes.length);
    assertEquals(
        6,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Int32).count());

    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[2]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[6]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[8]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[10]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[14]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[19]);
  }

  @Test
  public void Switch() {
    /*
    public int Switch(){
        byte a = 1;
        switch (a)
        {
            case 1:
                return 42;
            case 2:
                return 52;
            case 3:
                return 64;
            case 5:
                return 20;
            default:
                return 10;
        }
    }
    */
    MethodSymbol method = getMethodSymbol("Switch", "BranchOperationsTest");
    /*
        IL_0000: nop
        IL_0001: ldc.i4.1
        IL_0002: stloc.0
        IL_0003: ldloc.0
        IL_0004: stloc.2
        // sequence point: hidden
        IL_0005: ldloc.2
        IL_0006: stloc.1
        // sequence point: hidden
        IL_0007: ldloc.1
        IL_0008: ldc.i4.1
        IL_0009: sub
        IL_000a: switch (IL_0025, IL_002a, IL_002f, IL_0039, IL_0034)

        IL_0023: br.s IL_0039

        IL_0025: ldc.i4.s 42
        IL_0027: stloc.3
        IL_0028: br.s IL_003e

        IL_002a: ldc.i4.s 52
        IL_002c: stloc.3
        IL_002d: br.s IL_003e

        IL_002f: ldc.i4.s 64
        IL_0031: stloc.3
        IL_0032: br.s IL_003e

        IL_0034: ldc.i4.s 20
        IL_0036: stloc.3
        IL_0037: br.s IL_003e

        IL_0039: ldc.i4.s 10
        IL_003b: stloc.3
        IL_003c: br.s IL_003e

        IL_003e: ldloc.3
        IL_003f: ret
    */
    var opCodeTypes = method.getOpCodeTypes();

    assertEquals(64, opCodeTypes.length);
    assertEquals(
        9,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Int32).count());

    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[2]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[4]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[6]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[9]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[39]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[44]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[49]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[54]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[59]);
  }

  @Test
  public void SimpleTryCatch() {
    /*
    public int SimpleTryCatchWithThrow()
    {
        try
        {
            throw new Exception();
        }
        catch(Exception ex)
        {
            Console.Write("1");
        }

        return 42;
    }
    */
    MethodSymbol method = getMethodSymbol("SimpleTryCatchWithThrow", "OthersTest");

    var opCodeTypes = method.getOpCodeTypes();

    assertEquals(31, opCodeTypes.length);
    assertEquals(
        1,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Int32).count());
    assertEquals(
        1,
        Arrays.stream(opCodeTypes)
            .filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Object)
            .count());

    assertEquals(StaticOpCodeAnalyser.OpCodeType.Object, opCodeTypes[8]);
    assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[26]);
  }

  @ParameterizedTest
  @MethodSource("getTryCatchClassNames")
  public void TryCatch(String className, int[] intOpCodes, int[] objectOpCodes) {
    MethodSymbol method = getMethodSymbol(className, "OthersTest");

    var opCodeTypes = method.getOpCodeTypes();

    System.out.println("----------------------------");
    System.out.println(className);
    for (int i = 0; i < opCodeTypes.length; i++) {
      if (opCodeTypes[i] != null) System.out.println(i + " " + opCodeTypes[i]);
    }

    System.out.println("----------------------------");

    assertEquals(
        intOpCodes.length,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Int32).count());
    assertEquals(
        objectOpCodes.length,
        Arrays.stream(opCodeTypes)
            .filter(x -> x == StaticOpCodeAnalyser.OpCodeType.Object)
            .count());

    for (int intOpCode : intOpCodes) {
      assertEquals(StaticOpCodeAnalyser.OpCodeType.Int32, opCodeTypes[intOpCode]);
    }
    for (int objectOpCode : objectOpCodes) {
      assertEquals(StaticOpCodeAnalyser.OpCodeType.Object, opCodeTypes[objectOpCode]);
    }
  }

  public static Stream<Arguments> getTryCatchClassNames() {
    /**
     * first array is of int opcodes, second array is of object opcodes int opcodes are usually
     * stores of int values, object opcodes are usually stores of exception objects, that's how we
     * know it was analysed
     */
    return Stream.of(
        Arguments.of("SimpleTryCatchWithThrow", new int[] {3, 15, 20}, new int[] {11}),
        Arguments.of(
            "SimpleTryCatchFinallyWithThrow", new int[] {3, 7, 18, 27, 31}, new int[] {14}),
        Arguments.of("SimpleTryCatchWithoutThrow", new int[] {3, 7, 15, 20}, new int[] {11}),
        Arguments.of(
            "SimpleTryCatchFinallyWithoutThrow",
            new int[] {3, 6, 10, 13, 21, 24, 33, 38, 39},
            new int[] {17}),
        Arguments.of("ReturnInCatch", new int[] {3, 8, 10, 12, 27, 31}, new int[] {23}),
        Arguments.of("ReturnInTry", new int[] {3, 8, 10, 12, 16, 26, 31}, new int[] {22}),
        Arguments.of("DoubleCatch", new int[] {3, 7, 15, 23, 28}, new int[] {11, 19}),
        Arguments.of("NestedTry", new int[] {3, 6, 10, 14, 22, 28, 36, 42, 43}, new int[] {18, 32}),
        Arguments.of(
            "NestedTryDoubleCatch",
            new int[] {3, 6, 10, 14, 22, 30, 36, 45, 51, 52},
            new int[] {18, 26, 40}),
        Arguments.of("Rethrow", new int[] {3, 6, 10, 22, 28, 29}, new int[] {14, 18}),
        Arguments.of("OnlyThrow", new int[0], new int[0]),
        Arguments.of("OnlyConditionalThrow", new int[] {3, 7, 9, 11, 21, 23}, new int[0]),
        Arguments.of(
            "NestedTryInCatchClause", new int[] {3, 6, 10, 19, 27, 33, 39, 41}, new int[] {14, 23}),
        Arguments.of(
            "NestedTryInCatchClauseWithThrow",
            new int[] {3, 6, 10, 19, 30, 36, 42, 43},
            new int[] {14, 26}));
  }

  private MethodSymbol getMethodSymbol(String methodName, String projectName) {
    final CILOSTAZOLContext ctx = init(getDllPath(projectName));
    final AssemblyIdentity assemblyID = getAssemblyID(projectName);

    // class `Class`
    NamedTypeSymbol type = ctx.resolveType("Class", projectName, assemblyID);

    return getMethod(type, methodName)[0];
  }
}
