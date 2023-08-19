package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.internal.ConsoleMethodImplementations;
import com.vztekoverflow.cilostazol.nodes.internal.MathMethodImplementations;
import com.vztekoverflow.cilostazol.nodes.internal.ObjectMethodImplementations;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RuntimeSpecificMethodImplementations {
  private static final Map<String, Function<VirtualFrame, Object>> methodImplementations =
      new HashMap<>() {
        {
          put(
              "System.Double System.Math::Acos(System.Double)",
              MathMethodImplementations::MathAcos);
          put(
              "System.Double System.Math::Acosh(System.Double)",
              MathMethodImplementations::MathAcosh);
          put(
              "System.Double System.Math::Asin(System.Double)",
              MathMethodImplementations::MathAsin);
          put(
              "System.Double System.Math::Asinh(System.Double)",
              MathMethodImplementations::MathAsinh);
          put(
              "System.Double System.Math::Atan(System.Double)",
              MathMethodImplementations::MathAtan);
          put(
              "System.Double System.Math::Atanh(System.Double)",
              MathMethodImplementations::MathAtanh);
          put(
              "System.Double System.Math::Atan2(System.Double,System.Double)",
              MathMethodImplementations::MathAtan2);
          put(
              "System.Double System.Math::Cbrt(System.Double)",
              MathMethodImplementations::MathCbrt);
          put(
              "System.Double System.Math::Ceiling(System.Double)",
              MathMethodImplementations::MathCeiling);
          put("System.Double System.Math::Cos(System.Double)", MathMethodImplementations::MathCos);
          put(
              "System.Double System.Math::Cosh(System.Double)",
              MathMethodImplementations::MathCosh);
          put("System.Double System.Math::Exp(System.Double)", MathMethodImplementations::MathExp);
          put(
              "System.Double System.Math::Floor(System.Double)",
              MathMethodImplementations::MathFloor);
          put("System.Double System.Math::Log(System.Double)", MathMethodImplementations::MathLog);
          put(
              "System.Double System.Math::Log(System.Double,System.Double)",
              MathMethodImplementations::MathLog2);
          put(
              "System.Double System.Math::Log10(System.Double)",
              MathMethodImplementations::MathLog10);
          put(
              "System.Double System.Math::Pow(System.Double,System.Double)",
              MathMethodImplementations::MathPow);
          put("System.Double System.Math::Sin(System.Double)", MathMethodImplementations::MathSin);
          put(
              "System.Double System.Math::Sinh(System.Double)",
              MathMethodImplementations::MathSinh);
          put(
              "System.Double System.Math::Sqrt(System.Double)",
              MathMethodImplementations::MathSqrt);
          put("System.Double System.Math::Tan(System.Double)", MathMethodImplementations::MathTan);
          put(
              "System.Double System.Math::Tanh(System.Double)",
              MathMethodImplementations::MathTanh);
          put(
              "System.Double System.Math::ModF(System.Double,System.Double)",
              MathMethodImplementations::MathModF);
          put(
              "System.Double[] System.Math::SinCos(System.Double,System.Double)",
              MathMethodImplementations::MathSinCos);
          put(
              "System.Void System.Console::Write(System.Boolean)",
              ConsoleMethodImplementations::ConsoleWriteBoolean);
          put(
              "System.Void System.Console::Write(System.Char)",
              ConsoleMethodImplementations::ConsoleWriteChar);
          put(
              "System.Void System.Console::Write(System.Array)",
              ConsoleMethodImplementations::ConsoleWriteArray);
          put(
              "System.Void System.Console::Write(System.ArraySystem.Int32System.Int32)",
              ConsoleMethodImplementations::ConsoleWriteArrayIntInt);
          put(
              "System.Void System.Console::Write(System.Decimal)",
              ConsoleMethodImplementations::ConsoleWriteDecimal);
          put(
              "System.Void System.Console::Write(System.Double)",
              ConsoleMethodImplementations::ConsoleWriteDouble);
          put(
              "System.Void System.Console::Write(System.Single)",
              ConsoleMethodImplementations::ConsoleWriteDouble);
          put(
              "System.Void System.Console::Write(System.Int32)",
              ConsoleMethodImplementations::ConsoleWriteInt32);
          put(
              "System.Void System.Console::Write(System.UInt32)",
              ConsoleMethodImplementations::ConsoleWriteUInt32);
          put(
              "System.Void System.Console::Write(System.Int64)",
              ConsoleMethodImplementations::ConsoleWriteInt64);
          put(
              "System.Void System.Console::Write(System.UInt64)",
              ConsoleMethodImplementations::ConsoleWriteUInt64);
          put(
              "System.Void System.Console::Write(System.Object)",
              ConsoleMethodImplementations::ConsoleWriteObject);
          put(
              "System.Void System.Console::Write(System.String)",
              ConsoleMethodImplementations::ConsoleWriteString);
          put(
              "System.Void System.Console::WriteLine()",
              ConsoleMethodImplementations::ConsoleWriteLine);
          put(
              "System.Void System.Console::WriteLine(System.Boolean)",
              ConsoleMethodImplementations::ConsoleWriteLineBoolean);
          put(
              "System.Void System.Console::WriteLine(System.Char)",
              ConsoleMethodImplementations::ConsoleWriteLineChar);
          put(
              "System.Void System.Console::WriteLine(System.Array)",
              ConsoleMethodImplementations::ConsoleWriteLineArray);
          put(
              "System.Void System.Console::WriteLine(System.ArraySystem.Int32System.Int32)",
              ConsoleMethodImplementations::ConsoleWriteLineArrayIntInt);
          put(
              "System.Void System.Console::WriteLine(System.Decimal)",
              ConsoleMethodImplementations::ConsoleWriteLineDecimal);
          put(
              "System.Void System.Console::WriteLine(System.Double)",
              ConsoleMethodImplementations::ConsoleWriteLineDouble);
          put(
              "System.Void System.Console::WriteLine(System.Single)",
              ConsoleMethodImplementations::ConsoleWriteLineDouble);
          put(
              "System.Void System.Console::WriteLine(System.Int32)",
              ConsoleMethodImplementations::ConsoleWriteLineInt32);
          put(
              "System.Void System.Console::WriteLine(System.UInt32)",
              ConsoleMethodImplementations::ConsoleWriteLineUInt32);
          put(
              "System.Void System.Console::WriteLine(System.Int64)",
              ConsoleMethodImplementations::ConsoleWriteLineInt64);
          put(
              "System.Void System.Console::WriteLine(System.UInt64)",
              ConsoleMethodImplementations::ConsoleWriteLineUInt64);
          put(
              "System.Void System.Console::WriteLine(System.Object)",
              ConsoleMethodImplementations::ConsoleWriteLineObject);
          put(
              "System.Void System.Console::WriteLine(System.String)",
              ConsoleMethodImplementations::ConsoleWriteLineString);
          put(
              "System.String System.Object::ToString()",
              ObjectMethodImplementations::ObjectToString);
        }
      };

  public static Function<VirtualFrame, Object> getImplementation(String methodIdentifier) {
    return methodImplementations.get(methodIdentifier);
  }

  public static boolean hasCustomImplementation(String methodIdentifier) {
    return methodImplementations.containsKey(methodIdentifier);
  }
}
