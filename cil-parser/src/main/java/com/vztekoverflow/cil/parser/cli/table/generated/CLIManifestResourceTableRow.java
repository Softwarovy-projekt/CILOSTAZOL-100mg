package com.vztekoverflow.cil.parser.cli.table.generated;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.cli.table.*;

public class CLIManifestResourceTableRow extends CLITableRow<CLIManifestResourceTableRow> {

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private static final byte[] MAP_IMPLEMENTATION_TABLES =
      new byte[] {
        CLITableConstants.CLI_TABLE_FILE,
        CLITableConstants.CLI_TABLE_ASSEMBLY_REF,
        CLITableConstants.CLI_TABLE_EXPORTED_TYPE
      };

  public CLIManifestResourceTableRow(CLITables tables, int cursor, int rowIndex) {
    super(tables, cursor, rowIndex);
  }

  public final int getOffset() {
    int offset = 0;
    return getInt(offset);
  }

  public final int getFlags() {
    int offset = 4;
    return getInt(offset);
  }

  public final CLIStringHeapPtr getNameHeapPtr() {
    int offset = 8;
    int heapOffset = 0;
    if (tables.isStringHeapBig()) {
      heapOffset = getInt(offset);
    } else {
      heapOffset = getUShort(offset);
    }
    return new CLIStringHeapPtr(heapOffset);
  }

  public final CLITablePtr getImplementationTablePtr() {
    int offset = 10;
    if (tables.isStringHeapBig()) offset += 2;
    int codedValue;
    var isSmall = areSmallEnough(MAP_IMPLEMENTATION_TABLES);
    if (isSmall) {
      codedValue = getShort(offset) & 0xFFFF;
    } else {
      codedValue = getInt(offset);
    }
    if ((isSmall && (codedValue & 0xffff) == 0xffff)
        || (!isSmall && (codedValue & 0xffffffff) == 0xffffffff)) return null;
    return new CLITablePtr(
        MAP_IMPLEMENTATION_TABLES[codedValue & 3],
        (isSmall ? (0x0000ffff & codedValue) : codedValue) >>> 2);
  }

  @Override
  public int getLength() {
    int offset = 12;
    if (tables.isStringHeapBig()) offset += 2;
    if (!areSmallEnough(MAP_IMPLEMENTATION_TABLES)) offset += 2;
    return offset;
  }

  @Override
  public byte getTableId() {
    return CLITableConstants.CLI_TABLE_MANIFEST_RESOURCE;
  }

  @Override
  protected CLIManifestResourceTableRow createNew(CLITables tables, int cursor, int rowIndex) {
    return new CLIManifestResourceTableRow(tables, cursor, rowIndex);
  }
}
