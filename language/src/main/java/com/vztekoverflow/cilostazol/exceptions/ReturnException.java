package com.vztekoverflow.cilostazol.exceptions;

import com.oracle.truffle.api.nodes.ControlFlowException;

public class ReturnException extends ControlFlowException {
  private final Object result;

  public ReturnException(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }
}
