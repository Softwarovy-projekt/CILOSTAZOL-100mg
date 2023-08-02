package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public final class CALLNode extends NodeizedNodeBase {
  private final MethodSymbol method;
  private final int topStack;
  private final int returnStackTop;

  @Child private IndirectCallNode indirectCallNode;

  public CALLNode(MethodSymbol method, int topStack) {
    this.method = method;
    this.returnStackTop = topStack - method.getParameters().length;
    this.topStack = topStack;
    this.indirectCallNode = IndirectCallNode.create();
  }

  @Override
  public int execute(VirtualFrame frame) {
    Object[] args = getMethodArgsFromStack(frame);
    Object returnValue = indirectCallNode.call(method.getNode().getCallTarget(), args);

    clearArgsFromStack(frame);

    if (method.hasReturnValue()) {
      CILOSTAZOLFrame.put(frame, returnValue, returnStackTop, method.getReturnType().getType());
    }

    // +1 for return value
    return returnStackTop + 1;
  }

  @ExplodeLoop
  private void clearArgsFromStack(VirtualFrame frame) {
    // Clear the stack
    var topStack = this.topStack - 1;
    for (var arg : method.getParameters()) {
      CILOSTAZOLFrame.pop(frame, topStack, arg.getType());
      topStack--;
    }
  }

  @NotNull
  @ExplodeLoop
  private Object[] getMethodArgsFromStack(VirtualFrame frame) {
    final var argTypes = method.getParameters();
    final Object[] args = new Object[argTypes.length];
    for (int i = 0; i < args.length; i++) {
      final var idx = topStack - args.length + i;
      args[i] = CILOSTAZOLFrame.getLocal(frame, idx, argTypes[i].getType());
    }
    return args;
  }
}
