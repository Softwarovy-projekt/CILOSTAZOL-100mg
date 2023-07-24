package com.vztekoverflow.cilostazol.nodes.nodeized;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import java.io.OutputStream;
import java.io.PrintStream;

public class PRINTNode extends NodeizedNodeBase {
  /**
   * There is only support for Console.WriteLine(String); Others such as Console.WriteLine(int),
   * Console.WriteLine(Object...) are not supported.
   */
  private final int top;

  public PRINTNode(int top) {
    // -1 because we mock Console.WriteLine(String) and the argument is on stack
    this.top = top - 1;
  }

  @Override
  public int execute(VirtualFrame frame, TypeSymbol[] taggedFrame) {
    TruffleLanguage.Env env = CILOSTAZOLContext.get(this).getEnv();
    assert taggedFrame[top].getType()
                instanceof com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol namedType
            && namedType.getName().equals("String")
            && namedType.getNamespace().equals("System")
        : "Only support for Console.WriteLine(String)";
    var namedTypeSymbol = CILOSTAZOLFrame.getLocalObject(frame, top).getTypeSymbol();
    var length =
        namedTypeSymbol.getInstanceFields()[0].getInt(CILOSTAZOLFrame.getLocalObject(frame, top));
    var charArray =
        namedTypeSymbol.getInstanceFields()[1].getObject(
            CILOSTAZOLFrame.getLocalObject(frame, top));
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < length; i++) {
      result.append(((char[]) charArray)[i]);
    }

    print(env.out(), result.toString());
    return top;
  }

  @CompilerDirectives.TruffleBoundary
  private static void print(OutputStream out, String value) {
    try (var p = new PrintStream(out)) {
      p.println(value);
    }
  }
}
