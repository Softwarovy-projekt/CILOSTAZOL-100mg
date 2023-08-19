package com.vztekoverflow.cilostazol.runtime.typesystem;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.runtime.TestBase;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

public class MultidimensionalArrayParsingTests extends TestBase {
  public static Stream<Arguments> provideDesiredMethods() {
    return Stream.of(
        Arguments.of("System.Boolean", "get_IsSynchronized", new String[] {}),
        Arguments.of("System.Object", "get_SyncRoot", new String[] {}),
        Arguments.of("System.Boolean", "get_IsFixedSize", new String[] {}),
        Arguments.of("System.Boolean", "get_IsReadOnly", new String[] {}),
        Arguments.of("System.Int32", "get_Length", new String[] {}),
        Arguments.of("System.Int64", "get_LongLength", new String[] {}),
        Arguments.of("System.Int32", "get_Rank", new String[] {}),
        Arguments.of("System.Void", ".ctor", new String[] {"System.Int32", "System.Int32"}),
        Arguments.of(
            "System.Void", ".ctor", new String[] {"System.Int32", "System.Int32", "System.Int32"}),
        Arguments.of("System.Void", ".ctor", new String[] {"System.Array"}),
        Arguments.of("T", "Get", new String[] {"System.Int32", "System.Int32"}),
        Arguments.of("T", "Get", new String[] {"System.Int32", "System.Int32", "System.Int32"}),
        Arguments.of("T", "Get", new String[] {"System.Array"}),
        Arguments.of("System.Void", "Set", new String[] {"System.Int32", "System.Int32", "T"}),
        Arguments.of(
            "System.Void",
            "Set",
            new String[] {"System.Int32", "System.Int32", "System.Int32", "T"}),
        Arguments.of("System.Void", "Set", new String[] {"System.Array", "T"}),
        Arguments.of("T", "Get", new String[] {"System.Int64", "System.Int64"}),
        Arguments.of("T", "Get", new String[] {"System.Int64", "System.Int64", "System.Int64"}),
        Arguments.of("T", "Get", new String[] {"System.Array"}),
        Arguments.of("System.Void", "Set", new String[] {"System.Int64", "System.Int64", "T"}),
        Arguments.of(
            "System.Void",
            "Set",
            new String[] {"System.Int64", "System.Int64", "System.Int64", "T"}),
        Arguments.of("System.Void", "Set", new String[] {"System.Array", "T"}),
        Arguments.of("System.Int32", "GetLength", new String[] {"System.Int32"}),
        Arguments.of("System.Int64", "GetLongLength", new String[] {"System.Int32"}),
        Arguments.of("System.Int32", "GetLowerBound", new String[] {"System.Int32"}),
        Arguments.of("System.Int32", "GetUpperBound", new String[] {"System.Int32"}),
        Arguments.of("System.Void", "Initialize", new String[] {}),
        Arguments.of("System.Void", "CopyTo", new String[] {"System.Array", "System.Int64"}),
        Arguments.of("System.Object", "Clone", new String[] {}),
        Arguments.of("System.Int32", "System.Collections.ICollection.get_Count", new String[] {}),
        Arguments.of("System.Collections.IEnumerator", "GetEnumerator", new String[] {}),
        Arguments.of("System.Void", "CopyTo", new String[] {"System.Array", "System.Int32"}),
        Arguments.of(
            "System.Int32", "System.Collections.IList.Add", new String[] {"System.Object"}),
        Arguments.of("System.Void", "System.Collections.IList.Clear", new String[] {}),
        Arguments.of(
            "System.Boolean", "System.Collections.IList.Contains", new String[] {"System.Object"}),
        Arguments.of(
            "System.Int32", "System.Collections.IList.IndexOf", new String[] {"System.Object"}),
        Arguments.of(
            "System.Void",
            "System.Collections.IList.Insert",
            new String[] {"System.Int32", "System.Object"}),
        Arguments.of(
            "System.Void", "System.Collections.IList.Remove", new String[] {"System.Object"}),
        Arguments.of(
            "System.Void", "System.Collections.IList.RemoveAt", new String[] {"System.Int32"}),
        Arguments.of(
            "System.Object", "System.Collections.IList.get_Item", new String[] {"System.Int32"}),
        Arguments.of(
            "System.Void",
            "System.Collections.IList.set_Item",
            new String[] {"System.Int32", "System.Object"}),
        Arguments.of(
            "System.Int32",
            "System.Collections.IStructuralComparable.CompareTo",
            new String[] {"System.Object", "System.Collections.IComparer"}),
        Arguments.of(
            "System.Boolean",
            "System.Collections.IStructuralEquatable.Equals",
            new String[] {"System.Object", "System.Collections.IEqualityComparer"}),
        Arguments.of(
            "System.Int32",
            "System.Collections.IStructuralEquatable.GetHashCode",
            new String[] {"System.Collections.IEqualityComparer"}),
        Arguments.of("System.Int32", "GetFlatIndex", new String[] {"System.Int32", "System.Int32"}),
        Arguments.of(
            "System.Int32",
            "GetFlatIndex",
            new String[] {"System.Int32", "System.Int32", "System.Int32"}),
        Arguments.of("System.Int32", "GetFlatIndex", new String[] {"System.Array"}));
  }

  public static Stream<Arguments> provideDesiredFields() {
    return Stream.of(
        Arguments.of("System.Array", "_array"),
        Arguments.of("System.Array", "_lengths"),
        Arguments.of("System.Int32", "_rank"),
        Arguments.of("System.Int32", "_length"));
  }

  @Override
  protected String getDirectory() {
    return "src/main/resources/InternalBehaviourImplementation/InternalImpl";
  }

  public void testExists() {
    final String projectName = "CILOSTAZOLInternalImpl";
    CILOSTAZOLContext ctx = init(getDllPath(projectName));
    AssemblyIdentity assemblyIdentity = getAssemblyID(projectName);

    var type = ctx.resolveType("MultidimensionalArray`1", projectName, assemblyIdentity);

    assert type != null;
  }

  @ParameterizedTest
  @MethodSource("provideDesiredMethods")
  public void testContainsDesiredMethods(
      String returnType, String methodName, String[] parameterTypes) {
    final String projectName = "CILOSTAZOLInternalImpl";
    CILOSTAZOLContext ctx = init(getDllPath(projectName));
    AssemblyIdentity assemblyIdentity = getAssemblyID(projectName);

    var type = ctx.resolveType("MultidimensionalArray`1", projectName, assemblyIdentity);

    assertTrue(
        Arrays.stream(type.getMethods())
            .anyMatch(
                method ->
                    method.getReturnType().getType().toString().equals(returnType)
                        && method.getName().equals(methodName)
                        && Arrays.equals(
                            Arrays.stream(method.getParameters())
                                .map(param -> param.getType().toString())
                                .toArray(),
                            parameterTypes)));
  }

  @ParameterizedTest
  @MethodSource("provideDesiredFields")
  public void testContainsDesiredFields(String fieldType, String fieldName) {
    final String projectName = "CILOSTAZOLInternalImpl";
    CILOSTAZOLContext ctx = init(getDllPath(projectName));
    AssemblyIdentity assemblyIdentity = getAssemblyID(projectName);

    var type = ctx.resolveType("MultidimensionalArray`1", projectName, assemblyIdentity);

    assertTrue(
        Arrays.stream(type.getFields())
            .anyMatch(
                field ->
                    field.getType().toString().equals(fieldType)
                        && field.getName().equals(fieldName)));
  }
}
