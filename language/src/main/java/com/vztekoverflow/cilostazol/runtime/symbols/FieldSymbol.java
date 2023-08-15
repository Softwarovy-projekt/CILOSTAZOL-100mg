package com.vztekoverflow.cilostazol.runtime.symbols;

import com.vztekoverflow.cil.parser.cli.signature.FieldSig;
import com.vztekoverflow.cil.parser.cli.signature.SignatureReader;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIFieldTableRow;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;
import com.vztekoverflow.cilostazol.runtime.objectmodel.SystemType;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;

public final class FieldSymbol extends Symbol {

  private final String name;
  private final TypeSymbol type;
  private final short flags;
  private final short visibilityFlags;
  private final TypeSymbol declaringType;
  public final CLIFieldTableRow tableRow;

  private FieldSymbol(
      String name, TypeSymbol type, short flags, short visibilityFlags, CLIFieldTableRow row) {
    super(ContextProviderImpl.getInstance());
    this.name = name;
    this.type = type;
    this.flags = flags;
    this.visibilityFlags = visibilityFlags;
    this.tableRow = row;
    // TODO:
    this.declaringType = null;
  }

  public String getName() {
    return name;
  }

  public TypeSymbol getType() {
    return type;
  }

  public boolean isStatic() {
    return (flags & 0x0010) != 0;
  }

  public boolean isInitOnly() {
    return (flags & 0x0020) != 0;
  }

  public boolean isLiteral() {
    return (flags & 0x0040) != 0;
  }

  public boolean isNotSerialized() {
    return (flags & 0x0080) != 0;
  }

  public boolean isSpecialName() {
    return (flags & 0x0200) != 0;
  }

  public FieldSymbolVisibility getVisibility() {
    return FieldSymbolVisibility.fromFlags(visibilityFlags);
  }

  public SystemType getSystemType() {
    return type.getSystemType();
  }

  public TypeSymbol getDeclaringType() {
    return declaringType;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof FieldSymbol && equals((FieldSymbol) obj);
  }

  public boolean equals(FieldSymbol other) {
    return getName().equals(other.getName()) && getType().equals(other.getType());
  }

  public static class FieldSymbolFactory {
    public static FieldSymbol create(
        CLIFieldTableRow row, TypeSymbol[] mvars, TypeSymbol[] vars, ModuleSymbol module) {
      final String name = row.getNameHeapPtr().read(module.getDefiningFile().getStringHeap());
      final var signature = row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap());

      final FieldSig fieldSig = FieldSig.parse(new SignatureReader(signature));
      final TypeSymbol type = SymbolResolver.resolveType(fieldSig.getType(), mvars, vars, module);
      short flags = row.getFlags();
      short visibilityFlags = (short) (row.getFlags() & 0x0007);

      return new FieldSymbol(name, type, flags, visibilityFlags, row);
    }

    public static FieldSymbol createWith(FieldSymbol symbol, TypeSymbol type) {
      return new FieldSymbol(
          symbol.name, type, symbol.flags, symbol.visibilityFlags, symbol.tableRow);
    }
  }

  public enum FieldSymbolVisibility {
    CompilerControlled,
    Private,
    FamANDAssem,
    Assembly,
    Family,
    FamORAssem,
    Public;

    public static final int MASK = 0x7;

    public static FieldSymbolVisibility fromFlags(int flags) {
      return FieldSymbolVisibility.values()[flags & MASK];
    }
  }
}
