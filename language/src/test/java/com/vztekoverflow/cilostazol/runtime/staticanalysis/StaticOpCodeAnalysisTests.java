package com.vztekoverflow.cilostazol.runtime.staticanalysis;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticTypeAnalyser;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

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
    MethodSymbol method = getMethodSymbol("LoadTwoInt32_Add_SaveToInt32");
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
        Arrays.stream(opCodeTypes).filter(x -> x == StaticTypeAnalyser.OpCodeTypes.Int32).count());

    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[2]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[4]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[7]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[8]);
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
    MethodSymbol method = getMethodSymbol("LoadTwoByte_Add_SaveToInt32");
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
        Arrays.stream(opCodeTypes).filter(x -> x == StaticTypeAnalyser.OpCodeTypes.Int32).count());

    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[2]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[4]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[7]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[8]);
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
    MethodSymbol method = getMethodSymbol("LoadByteAndLong_Add_SaveToLong");
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
        Arrays.stream(opCodeTypes).filter(x -> x == StaticTypeAnalyser.OpCodeTypes.Int32).count());
    assertEquals(
        3,
        Arrays.stream(opCodeTypes).filter(x -> x == StaticTypeAnalyser.OpCodeTypes.Int64).count());

    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[2]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[4]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int32, opCodeTypes[7]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int64, opCodeTypes[5]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int64, opCodeTypes[9]);
    assertEquals(StaticTypeAnalyser.OpCodeTypes.Int64, opCodeTypes[10]);
  }

  private MethodSymbol getMethodSymbol(String methodName) {
    final String projectName = "BinaryOperationsTest";
    final CILOSTAZOLContext ctx = init(getDllPath(projectName));
    final AssemblyIdentity assemblyID = getAssemblyID(projectName);

    // class `Class`
    NamedTypeSymbol type = ctx.resolveType("Class", projectName, assemblyID);

    return getMethod(type, methodName)[0];
  }
}
