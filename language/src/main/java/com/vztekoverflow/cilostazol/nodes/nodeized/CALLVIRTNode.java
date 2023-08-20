package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.vztekoverflow.cil.parser.cli.signature.MethodDefFlags;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.exceptions.RuntimeCILException;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import com.vztekoverflow.cilostazol.runtime.symbols.ConstructedNamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class CALLVIRTNode extends NodeizedNodeBase {
  private final MethodSymbol method;
  private final int topStack;
  private final int returnStackTop;

  @Child private IndirectCallNode indirectCallNode;

  public CALLVIRTNode(MethodSymbol method, int topStack) {
    this.method = method;
    this.returnStackTop = topStack - method.getParameterCountIncludingInstance();
    this.topStack = topStack;
    this.indirectCallNode = IndirectCallNode.create();
  }

  @Override
  public int execute(VirtualFrame frame) {
    MethodSymbol virtMethod = resolveVirtMethod(frame);
    Object[] args = getMethodArgsFromStack(frame, virtMethod);
    if (method.getMethodDefFlags().hasFlag(MethodDefFlags.Flag.HAS_THIS)
        && args[0].equals(StaticObject.NULL))
      throw RuntimeCILException.RuntimeCILExceptionFactory.create(
          RuntimeCILException.Exception.NullReference, method.getContext(), frame, topStack);

    Object returnValue = indirectCallNode.call(virtMethod.getNode().getCallTarget(), args);

    if (method.hasReturnValue()) {
      CILOSTAZOLFrame.put(frame, returnValue, returnStackTop, method.getReturnType().getType());
      return returnStackTop + 1;
    }

    return returnStackTop;
  }

  private MethodSymbol resolveVirtMethod(VirtualFrame frame) {
    MethodSymbol virtMethod = method;
    var instance =
        CILOSTAZOLFrame.getLocalObject(frame, topStack - 1 - method.getParameters().length);

    if (method.getMethodFlags().hasFlag(MethodSymbol.MethodFlags.Flag.VIRTUAL)
        // Allow looking for overrides on Multidimensional Array implementation
        || (instance.getTypeSymbol() instanceof ConstructedNamedTypeSymbol constrType
            && constrType
                .getName()
                .equals(CILOSTAZOLBundle.message("cilostazol.multidimensional.array.name"))
            && constrType
                .getNamespace()
                .equals(CILOSTAZOLBundle.message("cilostazol.multidimensional.array.namespace")))) {
      var candidateMethod =
          SymbolResolver.resolveMethod(
              instance.getTypeSymbol(),
              method.getName(),
              method.getTypeArguments(),
              method.getParameterTypes(),
              method.getTypeParameters().length);

      if (candidateMethod == null)
        candidateMethod = SymbolResolver.resolveMethodImpl(method, instance.getTypeSymbol());

      virtMethod = Objects.requireNonNull(candidateMethod).member;
    }

    return virtMethod;
  }

  @NotNull
  @ExplodeLoop
  private Object[] getMethodArgsFromStack(VirtualFrame frame, MethodSymbol virtMethod) {
    final var argTypes = virtMethod.getParameters();
    final var instantiableOffset = CILOSTAZOLFrame.isInstantiable(virtMethod);
    final Object[] args = new Object[argTypes.length + instantiableOffset];
    for (int i = instantiableOffset; i < args.length; i++) {
      final var idx = topStack - args.length + i;
      args[i] = CILOSTAZOLFrame.pop(frame, idx, argTypes[i - instantiableOffset].getType());
    }

    if (instantiableOffset > 0) {
      final var idx = topStack - args.length;
      args[0] =
          CILOSTAZOLFrame.popObjectFromPossibleReference(
              frame, virtMethod.getDefiningType(), idx, virtMethod.getContext());
    }

    return args;
  }
}
