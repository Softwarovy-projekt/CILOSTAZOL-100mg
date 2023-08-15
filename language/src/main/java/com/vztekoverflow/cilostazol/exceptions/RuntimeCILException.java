package com.vztekoverflow.cilostazol.exceptions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;

// Exception for handling .NET exceptions
public final class RuntimeCILException extends RuntimeException {
  final StaticObject exception;

  private RuntimeCILException(StaticObject exception) {
    this.exception = exception;
  }

  public StaticObject getException() {
    return exception;
  }

  public enum Exception {
    Arithmetic("ArithmeticException"),
    DivideByZero("DivideByZeroException"),
    ExecutionEngine("ExecutionEngineException"),
    InvalidAddress("InvalidAddressException"),
    Overflow("OverflowException"),
    Security("SecurityException"),
    StackOverflow("StackOverflowException"),
    TypeLoad("TypeLoadException"),
    IndexOutOfRange("IndexOutOfRangeException"),
    InvalidCast("InvalidCastException"),
    MissingField("MissingFieldException"),
    MissingMethod("MissingMethodException"),
    NullReference("NullReferenceException"),
    OutOfMemory("OutOfMemoryException"),
    ArrayTypeMismatch("ArrayTypeMismatchException");

    public final String className;

    Exception(String name) {
      className = name;
    }
  }

  public static class RuntimeCILExceptionFactory {
    public static RuntimeCILException create(StaticObject exception) {
      return new RuntimeCILException(exception);
    }

    public static RuntimeCILException create(
        Exception ex, CILOSTAZOLContext ctx, VirtualFrame frame, int tp) {
      var type =
          (NamedTypeSymbol)
              SymbolResolver.resolveType(
                  ex.className, "System", AssemblyIdentity.SystemRuntimeLib700(), ctx);
      return new RuntimeCILException(ctx.getAllocator().createNew(type, frame, tp));
    }
  }
}
