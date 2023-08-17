package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.RootNode;
import com.vztekoverflow.cilostazol.exceptions.ReturnException;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;

public final class CILOSTAZOLRootNode extends RootNode {
  @Child private CILMethodNode _node;

  private CILOSTAZOLRootNode(FrameDescriptor descriptor, CILMethodNode node) {
    super(node.getMethod().getContext().getLanguage(), descriptor);
    _node = node;
  }

  public static CILOSTAZOLRootNode create(MethodSymbol method) {
    final CILMethodNode node;
    if (method.isInternalCall()) {
      node = CILRuntimeSpecificMethodNode.create(method);
    } else {
      node = CILMethodNode.create(method);
    }
    return new CILOSTAZOLRootNode(node.getFrameDescriptor(), node);
  }

  @Override
  @ExplodeLoop
  public Object execute(VirtualFrame frame) {

    try {
      return _node.execute(frame);
    } catch (ReturnException ex) {
      // TODO: log error
      return ex.getResult();
    }
  }
}
