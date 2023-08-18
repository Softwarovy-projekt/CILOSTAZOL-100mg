package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.nodes.TypeHelpers;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ConsoleMethodImplementations {

  public static Object ConsoleWriteBoolean(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = booleanToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteChar(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = charToString(frame);
    print(env.out(), value);
    return null;
  }

  // TODO
  public static Object ConsoleWriteArray(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    StaticObject wrappedArray = (StaticObject) frame.getArguments()[0];
    char[] value = (char[]) CILOSTAZOLContext.get(null).getArrayProperty().getObject(wrappedArray);
    print(env.out(), value);
    return null;
  }

  // TODO
  public static Object ConsoleWriteArrayIntInt(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteDecimal(VirtualFrame frame) {
    throw new InterpreterException("Decimal is not supported");
  }

  public static Object ConsoleWriteDouble(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteInt32(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteUInt32(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = unsignedIntToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteInt64(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteUInt64(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteObject(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    char[] value = objectToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteString(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    char[] value = stringToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteLine(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    println(env.out(), "");
    return null;
  }

  public static Object ConsoleWriteLineBoolean(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = booleanToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteLineChar(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = charToString(frame);
    println(env.out(), value);
    return null;
  }

  // TODO
  public static Object ConsoleWriteLineArray(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  // TODO
  public static Object ConsoleWriteLineArrayIntInt(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteLineDecimal(VirtualFrame frame) {
    throw new InterpreterException("Decimal is not supported");
  }

  public static Object ConsoleWriteLineDouble(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteLineInt32(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteLineUInt32(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = unsignedIntToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteLineInt64(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  // TODO
  public static Object ConsoleWriteLineUInt64(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteLineObject(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    char[] value = objectToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object ConsoleWriteLineString(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    char[] value = stringToString(frame);
    println(env.out(), value);
    return null;
  }

  @NotNull
  private static String booleanToString(VirtualFrame frame) {
    return ((int) frame.getArguments()[0]) != 0 ? "True" : "False";
  }

  @NotNull
  private static String charToString(VirtualFrame frame) {
    return String.valueOf((char) (int) frame.getArguments()[0]);
  }

  private static String primitiveToString(VirtualFrame frame) {
    return frame.getArguments()[0].toString();
  }

  @NotNull
  private static String unsignedIntToString(VirtualFrame frame) {
    return String.valueOf(TypeHelpers.zeroExtend32((int) frame.getArguments()[0]));
  }

  @NotNull
  private static char[] objectToString(VirtualFrame frame) {
    StaticObject object = (StaticObject) frame.getArguments()[0];
    MethodSymbol toString =
        Objects.requireNonNull(
                SymbolResolver.resolveMethod(
                    object.getTypeSymbol(), "ToString", new TypeSymbol[0], new TypeSymbol[0], 0))
            .member;
    StaticObject toStringResult = (StaticObject) toString.getNode().getCallTarget().call(object);
    return handleStringToString(
        frame, toStringResult, (NamedTypeSymbol) toStringResult.getTypeSymbol());
  }

  @NotNull
  private static char[] stringToString(VirtualFrame frame) {
    StaticObject string = (StaticObject) frame.getArguments()[0];
    return handleStringToString(frame, string, (NamedTypeSymbol) string.getTypeSymbol());
  }

  @CompilerDirectives.TruffleBoundary
  private static void print(OutputStream out, String value) {
    try (PrintStream p = new PrintStream(out)) {
      p.print(value);
    }
  }

  @CompilerDirectives.TruffleBoundary
  private static void print(OutputStream out, char[] value) {
    try (PrintStream p = new PrintStream(out)) {
      p.print(value);
    }
  }

  @CompilerDirectives.TruffleBoundary
  private static void println(OutputStream out, String value) {
    try (PrintStream p = new PrintStream(out)) {
      p.println(value);
    }
  }

  @CompilerDirectives.TruffleBoundary
  private static void println(OutputStream out, char[] value) {
    try (PrintStream p = new PrintStream(out)) {
      p.println(value);
    }
  }

  private static char[] handleStringToString(
      VirtualFrame frame, StaticObject object, NamedTypeSymbol type) {
    char[] charArray =
        (char[])
            CILOSTAZOLContext.get(null)
                .getArrayProperty()
                .getObject(type.getInstanceFields(frame, 0)[1].getObject(object));
    return charArray;
  }
}
