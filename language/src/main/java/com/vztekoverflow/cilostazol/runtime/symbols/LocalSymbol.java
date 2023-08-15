package com.vztekoverflow.cilostazol.runtime.symbols;

import com.vztekoverflow.cil.parser.cli.signature.LocalVarSig;
import com.vztekoverflow.cil.parser.cli.signature.LocalVarsSig;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;

public final class LocalSymbol extends Symbol {
  private final boolean pinned;
  private final boolean byRef;
  private final TypeSymbol type;

  private LocalSymbol(boolean pinned, boolean byRef, TypeSymbol type) {
    super(ContextProviderImpl.getInstance());
    this.pinned = pinned;
    this.byRef = byRef;
    this.type = type;
  }

  public boolean isPinned() {
    return pinned;
  }

  public boolean isByRef() {
    return byRef;
  }

  public TypeSymbol getType() {
    return type;
  }

  public static final class LocalSymbolFactory {
    public static LocalSymbol create(
        LocalVarSig sig, TypeSymbol[] mvars, TypeSymbol[] vars, ModuleSymbol module) {
      return new LocalSymbol(
          sig.isByRef(),
          sig.isPinned(),
          SymbolResolver.resolveType(sig.getTypeSig(), mvars, vars, module));
    }

    public static LocalSymbol[] create(
        LocalVarsSig sig, TypeSymbol[] mvars, TypeSymbol[] vars, ModuleSymbol module) {
      LocalSymbol[] locals = new LocalSymbol[sig.getVarsCount()];
      for (int i = 0; i < locals.length; i++) {
        locals[i] = create(sig.getVars()[i], mvars, vars, module);
      }
      return locals;
    }

    public static LocalSymbol createWith(LocalSymbol symbol, TypeSymbol type) {
      return new LocalSymbol(symbol.isPinned(), symbol.isByRef(), type);
    }
  }
}
