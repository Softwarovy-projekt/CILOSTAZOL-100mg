package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.internal.ConsoleMethodImplementations;
import com.vztekoverflow.cilostazol.nodes.internal.MathMethodImplementations;
import com.vztekoverflow.cilostazol.nodes.internal.ObjectMethodImplementations;
import com.vztekoverflow.cilostazol.nodes.internal.StringMethodImplementations;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RuntimeSpecificMethodImplementations {
  private static final Map<String, Function<VirtualFrame, Object>> methodImplementations =
      new HashMap<>() {
        {
          put(
              "System.Double System.Math::Acos(System.Double)",
              MathMethodImplementations::mathAcos);
          put(
              "System.Double System.Math::Acosh(System.Double)",
              MathMethodImplementations::mathAcosh);
          put(
              "System.Double System.Math::Asin(System.Double)",
              MathMethodImplementations::mathAsin);
          put(
              "System.Double System.Math::Asinh(System.Double)",
              MathMethodImplementations::mathAsinh);
          put(
              "System.Double System.Math::Atan(System.Double)",
              MathMethodImplementations::mathAtan);
          put(
              "System.Double System.Math::Atanh(System.Double)",
              MathMethodImplementations::mathAtanh);
          put(
              "System.Double System.Math::Atan2(System.Double, System.Double)",
              MathMethodImplementations::mathAtan2);
          put(
              "System.Double System.Math::Cbrt(System.Double)",
              MathMethodImplementations::mathCbrt);
          put(
              "System.Double System.Math::Ceiling(System.Double)",
              MathMethodImplementations::mathCeiling);
          put("System.Double System.Math::Cos(System.Double)", MathMethodImplementations::mathCos);
          put(
              "System.Double System.Math::Cosh(System.Double)",
              MathMethodImplementations::mathCosh);
          put("System.Double System.Math::Exp(System.Double)", MathMethodImplementations::mathExp);
          put(
              "System.Double System.Math::Floor(System.Double)",
              MathMethodImplementations::mathFloor);
          put("System.Double System.Math::Log(System.Double)", MathMethodImplementations::mathLog);
          put(
              "System.Double System.Math::Log(System.Double, System.Double)",
              MathMethodImplementations::mathLog2);
          put(
              "System.Double System.Math::Log10(System.Double)",
              MathMethodImplementations::mathLog10);
          put(
              "System.Double System.Math::Pow(System.Double, System.Double)",
              MathMethodImplementations::mathPow);
          put("System.Double System.Math::Sin(System.Double)", MathMethodImplementations::mathSin);
          put(
              "System.Double System.Math::Sinh(System.Double)",
              MathMethodImplementations::mathSinh);
          put(
              "System.Double System.Math::Sqrt(System.Double)",
              MathMethodImplementations::mathSqrt);
          put("System.Double System.Math::Tan(System.Double)", MathMethodImplementations::mathTan);
          put(
              "System.Double System.Math::Tanh(System.Double)",
              MathMethodImplementations::mathTanh);
          put(
              "System.Double System.Math::ModF(System.Double, System.Double)",
              MathMethodImplementations::mathModF);
          put(
              "System.Double[] System.Math::SinCos(System.Double, System.Double)",
              MathMethodImplementations::mathSinCos);
          put(
              "System.Void System.Console::Write(System.Boolean)",
              ConsoleMethodImplementations::consoleWriteBoolean);
          put(
              "System.Void System.Console::Write(System.Char)",
              ConsoleMethodImplementations::consoleWriteChar);
          put(
              "System.Void System.Console::Write(System.Array)",
              ConsoleMethodImplementations::consoleWriteArray);
          put(
              "System.Void System.Console::Write(System.Array, System.Int32, System.Int32)",
              ConsoleMethodImplementations::consoleWriteArrayIntInt);
          put(
              "System.Void System.Console::Write(System.Decimal)",
              ConsoleMethodImplementations::consoleWriteDecimal);
          put(
              "System.Void System.Console::Write(System.Double)",
              ConsoleMethodImplementations::consoleWriteDouble);
          put(
              "System.Void System.Console::Write(System.Single)",
              ConsoleMethodImplementations::consoleWriteDouble);
          put(
              "System.Void System.Console::Write(System.Int32)",
              ConsoleMethodImplementations::consoleWriteInt32);
          put(
              "System.Void System.Console::Write(System.UInt32)",
              ConsoleMethodImplementations::consoleWriteUInt32);
          put(
              "System.Void System.Console::Write(System.Int64)",
              ConsoleMethodImplementations::consoleWriteInt64);
          put(
              "System.Void System.Console::Write(System.UInt64)",
              ConsoleMethodImplementations::consoleWriteUInt64);
          put(
              "System.Void System.Console::Write(System.Object)",
              ConsoleMethodImplementations::consoleWriteObject);
          put(
              "System.Void System.Console::Write(System.String)",
              ConsoleMethodImplementations::consoleWriteString);
          put(
              "System.Void System.Console::WriteLine()",
              ConsoleMethodImplementations::consoleWriteLine);
          put(
              "System.Void System.Console::WriteLine(System.Boolean)",
              ConsoleMethodImplementations::consoleWriteLineBoolean);
          put(
              "System.Void System.Console::WriteLine(System.Char)",
              ConsoleMethodImplementations::consoleWriteLineChar);
          put(
              "System.Void System.Console::WriteLine(System.Array)",
              ConsoleMethodImplementations::consoleWriteLineArray);
          put(
              "System.Void System.Console::WriteLine(System.Array, System.Int32, System.Int32)",
              ConsoleMethodImplementations::consoleWriteLineArrayIntInt);
          put(
              "System.Void System.Console::WriteLine(System.Decimal)",
              ConsoleMethodImplementations::consoleWriteLineDecimal);
          put(
              "System.Void System.Console::WriteLine(System.Double)",
              ConsoleMethodImplementations::consoleWriteLineDouble);
          put(
              "System.Void System.Console::WriteLine(System.Single)",
              ConsoleMethodImplementations::consoleWriteLineDouble);
          put(
              "System.Void System.Console::WriteLine(System.Int32)",
              ConsoleMethodImplementations::consoleWriteLineInt32);
          put(
              "System.Void System.Console::WriteLine(System.UInt32)",
              ConsoleMethodImplementations::consoleWriteLineUInt32);
          put(
              "System.Void System.Console::WriteLine(System.Int64)",
              ConsoleMethodImplementations::consoleWriteLineInt64);
          put(
              "System.Void System.Console::WriteLine(System.UInt64)",
              ConsoleMethodImplementations::consoleWriteLineUInt64);
          put(
              "System.Void System.Console::WriteLine(System.Object)",
              ConsoleMethodImplementations::consoleWriteLineObject);
          put(
              "System.Void System.Console::WriteLine(System.String)",
              ConsoleMethodImplementations::consoleWriteLineString);
          put(
              "System.String System.Object::ToString()",
              ObjectMethodImplementations::objectToString);
          put("System.String System.Byte::ToString()", ObjectMethodImplementations::byteToString);
          put("System.String System.Int16::ToString()", ObjectMethodImplementations::shortToString);
          put("System.String System.Int32::ToString()", ObjectMethodImplementations::int32ToString);
          put("System.String System.Int64::ToString()", ObjectMethodImplementations::int64ToString);
          put(
              "System.String System.Single::ToString()",
              ObjectMethodImplementations::floatToString);
          put(
              "System.String System.Double::ToString()",
              ObjectMethodImplementations::doubleToString);
          put(
              "System.String System.String::CreateFromChar(System.Char)",
              StringMethodImplementations::stringCreateFromChar);
          put(
              "System.String System.String::FastAllocateString(System.Int32)",
              StringMethodImplementations::stringFastAllocateString);
          put(
              "System.Void System.String::FillStringChecked(System.String, System.Int32, System.String)",
              StringMethodImplementations::stringFillStringChecked);
          put(
              "System.Int32 System.String::get_Length()",
              StringMethodImplementations::stringGetLength);
        }
      };

  @CompilerDirectives.TruffleBoundary
  public static Function<VirtualFrame, Object> getImplementation(String methodIdentifier) {
    return methodImplementations.get(methodIdentifier);
  }

  @CompilerDirectives.TruffleBoundary
  public static boolean hasCustomImplementation(String methodIdentifier) {
    return methodImplementations.containsKey(methodIdentifier);
  }
}
