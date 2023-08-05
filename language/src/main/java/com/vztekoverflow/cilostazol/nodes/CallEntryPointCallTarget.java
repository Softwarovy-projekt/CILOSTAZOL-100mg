package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.CallTarget;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import java.lang.reflect.Array;

public final class CallEntryPointCallTarget implements CallTarget {

  private final CallTarget inner;
  private final boolean shouldAddArgs;
  private final StaticObject arg;

  public CallEntryPointCallTarget(CallTarget inner, boolean shouldAddArgs) {
    this.inner = inner;
    this.shouldAddArgs = shouldAddArgs;
    if (shouldAddArgs) {
      var ctx = CILOSTAZOLContext.get(null);
      var arrayOfString = SymbolResolver.resolveArray(SymbolResolver.getString(ctx), 1, ctx);
      arg =
          ctx.getAllocator()
              .createNewReferenceArray(
                  arrayOfString, ctx.getEnv().getApplicationArguments().length);
    } else {
      arg = null;
    }
  }

  public void fillArguments() {
    var ctx = CILOSTAZOLContext.get(null);
    int i = 0;
    for (var appArg : ctx.getEnv().getApplicationArguments()) {
      var javaArr = ctx.getArrayProperty().getObject(arg);
      Array.set(javaArr, i, ctx.getAllocator().createString(appArg));
      i++;
    }
  }

  @Override
  public Object call(Object... arguments) {
    assert arguments.length == 0;
    Object result;
    if (shouldAddArgs) {
      fillArguments();
      result = inner.call(arg);
    } else {
      result = inner.call();
    }

    if (result == null) return 0;

    return result;
  }
}
