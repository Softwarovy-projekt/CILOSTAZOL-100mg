package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import org.jetbrains.annotations.NotNull;

public class NEWOBJNode extends NodeizedNodeBase {

  private final NamedTypeSymbol type;
  private final MethodSymbol constructor;
  private final int topStack;
  private final int returnStackTop;

  @Child private IndirectCallNode indirectCallNode;

  public NEWOBJNode(MethodSymbol constructor, int topStack) {
    this.type = constructor.getDefiningType();
    this.constructor = constructor;
    this.returnStackTop = topStack - constructor.getParameters().length + 1;
    this.topStack = topStack;
    this.indirectCallNode = IndirectCallNode.create();
  }

  @Override
  public int execute(VirtualFrame frame) {
    // NEWOBJ handles the constructor call differently
    // First, clear all args arg1, ..., argN
    Object[] args = getMethodArgsFromStack(frame);

    // Then, create the object as arg0
    StaticObject object = CILOSTAZOLContext.get(this).getAllocator().createNew(type);
    args[0] = object;

    // Finally, call the constructor and push the result to the stack
    indirectCallNode.call(constructor.getNode().getCallTarget(), args);
    CILOSTAZOLFrame.put(frame, object, returnStackTop - 1, type);
    return returnStackTop;
  }

  @NotNull
  @ExplodeLoop
  private Object[] getMethodArgsFromStack(VirtualFrame frame) {
    final var argTypes = constructor.getParameters();
    final Object[] args = new Object[argTypes.length + 1];
    for (int i = 1; i < args.length; i++) {
      final var idx = topStack - args.length + i;
      args[i] = CILOSTAZOLFrame.pop(frame, idx, argTypes[i - 1].getType());
    }
    return args;
  }
}
