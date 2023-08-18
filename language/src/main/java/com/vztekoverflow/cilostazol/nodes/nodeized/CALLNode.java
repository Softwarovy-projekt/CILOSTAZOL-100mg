package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.vztekoverflow.cil.parser.cli.signature.MethodDefFlags;
import com.vztekoverflow.cilostazol.exceptions.RuntimeCILException;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import org.jetbrains.annotations.NotNull;

public final class CALLNode extends NodeizedNodeBase {
  private final MethodSymbol method;
  private final int topStack;
  private final int returnStackTop;

  @Child private IndirectCallNode indirectCallNode;

  public CALLNode(MethodSymbol method, int topStack) {
    this.method = method;
    this.returnStackTop = topStack - method.getParameterCountIncludingInstance();
    this.topStack = topStack;
    this.indirectCallNode = IndirectCallNode.create();
  }

  @Override
  public int execute(VirtualFrame frame) {
    Object[] args = getMethodArgsFromStack(frame);
    if (method.getMethodDefFlags().hasFlag(MethodDefFlags.Flag.HAS_THIS)
        && args[0].equals(StaticObject.NULL))
      throw RuntimeCILException.RuntimeCILExceptionFactory.create(
          RuntimeCILException.Exception.NullReference, method.getContext(), frame, topStack);

    Object returnValue = indirectCallNode.call(method.getNode().getCallTarget(), args);

    if (method.hasReturnValue()) {
      CILOSTAZOLFrame.put(frame, returnValue, returnStackTop, method.getReturnType().getType());
      return returnStackTop + 1;
    }

    return returnStackTop;
  }

  @NotNull
  @ExplodeLoop
  private Object[] getMethodArgsFromStack(VirtualFrame frame) {
    final var argTypes = method.getParameters();
    final var instantiableOffset = CILOSTAZOLFrame.isInstantiable(method);
    final Object[] args = new Object[argTypes.length + instantiableOffset];
    for (int i = instantiableOffset; i < args.length; i++) {
      final var idx = topStack - args.length + i;
      args[i] = CILOSTAZOLFrame.pop(frame, idx, argTypes[i - instantiableOffset].getType());
    }

    if (instantiableOffset > 0) {
      final var idx = topStack - args.length;
      args[0] =
          CILOSTAZOLFrame.popObjectFromPossibleReference(
              frame, method.getDefiningType(), idx, method.getContext());
    }

    return args;
  }
}
