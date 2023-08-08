package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
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

  public PRINTNode(int top) {
    // -1 because we mock Console.WriteLine(String) and the argument
    // is a local variable that is last on local stack
    this.argumentTop = top - 1;
    this.originalTop = top;
  }

  @Override
  public int execute(VirtualFrame frame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(this).getEnv();

    StringBuilder result = getStringFromFrame(frame);
    print(env.out(), result.toString());

    //    var type = taggedFrame[argumentTop];
    //    CILOSTAZOLFrame.pop(frame, originalTop, type);
    // for eating an argument and not returning anything
    return originalTop - 1;
  }

  @NotNull
  @ExplodeLoop
  private StringBuilder getStringFromFrame(VirtualFrame frame) {
    var stringObject = CILOSTAZOLFrame.getLocalObject(frame, argumentTop);
    var namedTypeSymbol = (NamedTypeSymbol) stringObject.getTypeSymbol();
    var length = namedTypeSymbol.getInstanceFields(frame, originalTop)[0].getInt(stringObject);
    var charArray =
        CILOSTAZOLContext.get(null)
            .getArrayProperty()
            .getObject(
                namedTypeSymbol.getInstanceFields(frame, originalTop)[1].getObject(stringObject));

    StringBuilder result = new StringBuilder();
    for (int i = 0; i < length; i++) {
      result.append(((char[]) charArray)[i]);
    }
    return result;
  }

  @CompilerDirectives.TruffleBoundary
  private static void print(OutputStream out, String value) {
    try (var p = new PrintStream(out)) {
      p.println(value);
    }
  }
}
