package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import java.util.Map;
import java.util.function.Function;

public final class RuntimeSpecificMethodImplementations {
  private static final Map<String, Function<VirtualFrame, Object>> methodImplementations =
      Map.of(
          "System::Double System::Math::Sqrt(System::Double)",
          RuntimeSpecificMethodImplementations::Sqrt);

  public static Function<VirtualFrame, Object> getImplementation(String methodIdentifier) {
    return methodImplementations.get(methodIdentifier);
  }

  private static Object Sqrt(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.sqrt(d);
  }
}
