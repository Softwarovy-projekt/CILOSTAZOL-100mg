package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import java.util.function.Function;

public class CILRuntimeSpecificMethodNode extends CILMethodNode {

  private final Function<VirtualFrame, Object> implementation;

  public CILRuntimeSpecificMethodNode(MethodSymbol method) {
    super(method);
    implementation = RuntimeSpecificMethodImplementations.getImplementation(method.toString());
    if (implementation == null) {
      throw new IllegalArgumentException("No implementation for " + method);
    }
  }

  public static CILRuntimeSpecificMethodNode create(MethodSymbol method) {
    return new CILRuntimeSpecificMethodNode(method);
  }

  @Override
  public Object execute(VirtualFrame frame) {
    return implementation.apply(frame);
  }
}
