package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.internal.MathMethodImplementations;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RuntimeSpecificMethodImplementations {
  private static final Map<String, Function<VirtualFrame, Object>> methodImplementations =
      new HashMap<>() {
        {
          put(
              "System::Double System::Math::Acos(System::Double)",
              MathMethodImplementations::MathAcos);
          put(
              "System::Double System::Math::Acosh(System::Double)",
              MathMethodImplementations::MathAcosh);
          put(
              "System::Double System::Math::Asin(System::Double)",
              MathMethodImplementations::MathAsin);
          put(
              "System::Double System::Math::Asinh(System::Double)",
              MathMethodImplementations::MathAsinh);
          put(
              "System::Double System::Math::Atan(System::Double)",
              MathMethodImplementations::MathAtan);
          put(
              "System::Double System::Math::Atanh(System::Double)",
              MathMethodImplementations::MathAtanh);
          put(
              "System::Double System::Math::Atan2(System::Double,System::Double)",
              MathMethodImplementations::MathAtan2);
          put(
              "System::Double System::Math::Cbrt(System::Double)",
              MathMethodImplementations::MathCbrt);
          put(
              "System::Double System::Math::Ceiling(System::Double)",
              MathMethodImplementations::MathCeiling);
          put(
              "System::Double System::Math::Cos(System::Double)",
              MathMethodImplementations::MathCos);
          put(
              "System::Double System::Math::Cosh(System::Double)",
              MathMethodImplementations::MathCosh);
          put(
              "System::Double System::Math::Exp(System::Double)",
              MathMethodImplementations::MathExp);
          put(
              "System::Double System::Math::Floor(System::Double)",
              MathMethodImplementations::MathFloor);
          put(
              "System::Double System::Math::Log(System::Double)",
              MathMethodImplementations::MathLog);
          put(
              "System::Double System::Math::Log(System::Double,System::Double)",
              MathMethodImplementations::MathLog2);
          put(
              "System::Double System::Math::Log10(System::Double)",
              MathMethodImplementations::MathLog10);
          put(
              "System::Double System::Math::Pow(System::Double,System::Double)",
              MathMethodImplementations::MathPow);
          put(
              "System::Double System::Math::Sin(System::Double)",
              MathMethodImplementations::MathSin);
          put(
              "System::Double System::Math::Sinh(System::Double)",
              MathMethodImplementations::MathSinh);
          put(
              "System::Double System::Math::Sqrt(System::Double)",
              MathMethodImplementations::MathSqrt);
          put(
              "System::Double System::Math::Tan(System::Double)",
              MathMethodImplementations::MathTan);
          put(
              "System::Double System::Math::Tanh(System::Double)",
              MathMethodImplementations::MathTanh);
          put(
              "System::Double System::Math::ModF(System::Double,System::Double)",
              MathMethodImplementations::MathModF);
          put(
              "System::Double[] System::Math::SinCos(System::Double,System::Double)",
              MathMethodImplementations::MathSinCos);
        }
      };

  public static Function<VirtualFrame, Object> getImplementation(String methodIdentifier) {
    return methodImplementations.get(methodIdentifier);
  }

  public static boolean hasCustomImplementation(String methodIdentifier) {
    return methodImplementations.containsKey(methodIdentifier);
  }
}
