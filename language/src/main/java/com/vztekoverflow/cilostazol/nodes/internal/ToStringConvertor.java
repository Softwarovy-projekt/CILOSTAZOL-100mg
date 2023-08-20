package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.nodes.TypeHelpers;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ToStringConvertor {
  @NotNull
  public static String booleanToString(VirtualFrame frame) {
    return ((int) frame.getArguments()[0]) != 0 ? "True" : "False";
  }

  @NotNull
  public static String charToString(VirtualFrame frame) {
    return String.valueOf((char) (int) frame.getArguments()[0]);
  }

  public static String primitiveToString(VirtualFrame frame) {
    return frame.getArguments()[0].toString();
  }

  @NotNull
  public static String unsignedIntToString(VirtualFrame frame) {
    return String.valueOf(TypeHelpers.zeroExtend32((int) frame.getArguments()[0]));
  }

  @NotNull
  public static char[] objectToString(VirtualFrame frame) {
    StaticObject object = (StaticObject) frame.getArguments()[0];
    MethodSymbol toString =
        Objects.requireNonNull(
                SymbolResolver.resolveMethod(
                    object.getTypeSymbol(), "ToString", new TypeSymbol[0], new TypeSymbol[0], 0))
            .member;
    StaticObject toStringResult = (StaticObject) toString.getNode().getCallTarget().call(object);
    return handleStringToString(
        frame, toStringResult, (NamedTypeSymbol) toStringResult.getTypeSymbol());
  }

  @NotNull
  public static char[] stringToString(VirtualFrame frame) {
    StaticObject string = (StaticObject) frame.getArguments()[0];
    return handleStringToString(frame, string, (NamedTypeSymbol) string.getTypeSymbol());
  }

  public static char[] handleStringToString(
      VirtualFrame frame, StaticObject object, NamedTypeSymbol type) {
    char[] charArray =
        (char[])
            CILOSTAZOLContext.get(null)
                .getArrayProperty()
                .getObject(type.getInstanceFields(frame, 0)[1].getObject(object));
    return charArray;
  }
}
