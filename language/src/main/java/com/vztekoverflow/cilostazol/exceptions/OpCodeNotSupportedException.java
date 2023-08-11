package com.vztekoverflow.cilostazol.exceptions;

import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;

public class OpCodeNotSupportedException extends CILOSTAZOLException {
  public OpCodeNotSupportedException(int opCode) {
    super(CILOSTAZOLBundle.message("cilostazol.exception.not.supported.OpCode", opCode));
  }
}
