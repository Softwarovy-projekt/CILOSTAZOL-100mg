package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.nodes.TypeHelpers;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import java.io.OutputStream;
import java.io.PrintStream;
import org.jetbrains.annotations.NotNull;

public class PRINTNode extends NodeizedNodeBase {
  /**
   * There is only support for Console.WriteLine(String); Others such as Console.WriteLine(int),
   * Console.WriteLine(Object...) are not supported.
   */
  private final int argumentTop;

  private final int originalTop;
  private final boolean addNewLine;
  private final TypeSymbol[] parameterTypes;

  public PRINTNode(int top, boolean addNewLine, TypeSymbol[] parameterTypes) {
    // -1 because we mock Console.WriteLine(String) and the argument
    // is a local variable that is last on local stack
    this.argumentTop = top - 1;
    this.originalTop = top;
    this.addNewLine = addNewLine;
    this.parameterTypes = parameterTypes;
  }

  @Override
  public int execute(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(this).getEnv();

    // we always take only the first argument - hence we do not support formatting
    StringBuilder result = getStringFromFrame(frame);
    if (addNewLine) {
      result.append(System.lineSeparator());
    }
    println(env.out(), result.toString());
    // for eating an argument and not returning anything
    return originalTop - 1;
  }

  @NotNull
  @ExplodeLoop
  private StringBuilder getStringFromFrame(VirtualFrame frame) {
    // we always take only the first argument - we do not support formatting
    switch (parameterTypes[0].getStackTypeKind()) {
      case Object -> {
        return handleObjectToString(frame);
      }
      case Int32 -> {
        return new StringBuilder().append(CILOSTAZOLFrame.getLocalInt(frame, argumentTop));
      }
      case Int64 -> {
        return new StringBuilder().append(CILOSTAZOLFrame.getLocalLong(frame, argumentTop));
      }
      case NativeFloat -> {
        return new StringBuilder().append(CILOSTAZOLFrame.getLocalNativeFloat(frame, argumentTop));
      }
      default -> throw new InterpreterException(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.not.supported.Console.Write.argument",
              parameterTypes[0].toString()));
    }
  }

  @NotNull
  private StringBuilder handleObjectToString(VirtualFrame frame) {
    var obj = CILOSTAZOLFrame.getLocalObject(frame, argumentTop);

    // handle Console.WriteLine(String)
    if (obj.getTypeSymbol().equals(SymbolResolver.getString(CILOSTAZOLContext.get(this))))
      return handleStringToString(frame, obj, (NamedTypeSymbol) obj.getTypeSymbol());

    // handle Console.WriteLine(Object)
    var definingType = (NamedTypeSymbol) obj.getTypeSymbol();
    var objectToString =
        SymbolResolver.resolveMethod(
            definingType, "ToString", definingType.getTypeArguments(), new TypeSymbol[0], 0);
    var toString = TypeHelpers.getVirtualMethodOnInstance(objectToString, obj);

    // default object.ToString() implementation
    if (toString.getDefiningType().getNamespace().equals("System")
        && toString.getDefiningType().getName().equals("Object"))
      return new StringBuilder().append(definingType.toString());

    // callToString
    new CALLNode(toString, originalTop).execute(frame);
    // string is put on top of the stack
    return getStringFromFrame(frame);
  }

  @NotNull
  private StringBuilder handleStringToString(
      VirtualFrame frame, StaticObject obj, NamedTypeSymbol namedTypeSymbol) {
    var length = namedTypeSymbol.getInstanceFields(frame, originalTop)[0].getInt(obj);
    var charArray =
        CILOSTAZOLContext.get(null)
            .getArrayProperty()
            .getObject(namedTypeSymbol.getInstanceFields(frame, originalTop)[1].getObject(obj));

    StringBuilder result = new StringBuilder();
    for (int i = 0; i < length; i++) {
      result.append(((char[]) charArray)[i]);
    }
    return result;
  }

  @CompilerDirectives.TruffleBoundary
  private void println(OutputStream out, String value) {
    try (var p = new PrintStream(out)) {
      p.print(value);
    }
  }
}
