package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import org.jetbrains.annotations.NotNull;

public final class JMPNode extends NodeizedNodeBase {
  private final MethodSymbol method;
  private final int topStack;

  @Child private IndirectCallNode indirectCallNode;

  public JMPNode(MethodSymbol method, int topStack) {
    this.method = method;
    this.topStack = topStack;
    this.indirectCallNode = IndirectCallNode.create();
  }

  @Override
  public int execute(VirtualFrame frame) {
    Object[] args = getMethodArgsFromCurrentArgs(frame);
    indirectCallNode.call(method.getNode().getCallTarget(), args);
    return topStack;
  }

  @NotNull
  @ExplodeLoop
  private Object[] getMethodArgsFromCurrentArgs(VirtualFrame frame) {
    final int argsOffset = CILOSTAZOLFrame.getStartArgsOffset(method);
    Object[] args = new Object[method.getParameterCountIncludingInstance()];
    for (int i = 0; i < args.length; i++) {
      args[i] =
          CILOSTAZOLFrame.getLocal(
              frame, argsOffset + i, method.getParameterTypesIncludingInstance()[i]);
    }

    return args;
  }
}
