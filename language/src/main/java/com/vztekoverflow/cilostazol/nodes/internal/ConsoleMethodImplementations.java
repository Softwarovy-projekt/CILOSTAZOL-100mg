package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import java.io.OutputStream;
import java.io.PrintStream;

public final class ConsoleMethodImplementations {

  public static Object consoleWriteBoolean(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.booleanToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteChar(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.charToString(frame);
    print(env.out(), value);
    return null;
  }

  // TODO
  public static Object consoleWriteArray(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    StaticObject wrappedArray = (StaticObject) frame.getArguments()[0];
    char[] value = (char[]) CILOSTAZOLContext.get(null).getArrayProperty().getObject(wrappedArray);
    print(env.out(), value);
    return null;
  }

  // TODO
  public static Object consoleWriteArrayIntInt(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteDecimal(VirtualFrame frame) {
    throw new InterpreterException("Decimal is not supported");
  }

  public static Object consoleWriteDouble(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteInt32(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteUInt32(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.unsignedIntToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteInt64(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteUInt64(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteObject(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    char[] value = ToStringConvertor.objectToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteString(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    char[] value = ToStringConvertor.stringToString(frame);
    print(env.out(), value);
    return null;
  }

  public static Object consoleWriteLine(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    println(env.out(), "");
    return null;
  }

  public static Object consoleWriteLineBoolean(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.booleanToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object consoleWriteLineChar(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.charToString(frame);
    println(env.out(), value);
    return null;
  }

  // TODO
  public static Object consoleWriteLineArray(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  // TODO
  public static Object consoleWriteLineArrayIntInt(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object consoleWriteLineDecimal(VirtualFrame frame) {
    throw new InterpreterException("Decimal is not supported");
  }

  public static Object consoleWriteLineDouble(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object consoleWriteLineInt32(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object consoleWriteLineUInt32(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.unsignedIntToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object consoleWriteLineInt64(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  // TODO
  public static Object consoleWriteLineUInt64(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    String value = ToStringConvertor.primitiveToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object consoleWriteLineObject(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    char[] value = ToStringConvertor.objectToString(frame);
    println(env.out(), value);
    return null;
  }

  public static Object consoleWriteLineString(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(null).getEnv();
    char[] value = ToStringConvertor.stringToString(frame);
    println(env.out(), value);
    return null;
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
}
