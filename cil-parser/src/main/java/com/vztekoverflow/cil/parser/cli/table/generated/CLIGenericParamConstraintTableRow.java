package com.vztekoverflow.cil.parser.cli.table.generated;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.cli.table.*;

public class CLIGenericParamConstraintTableRow
    extends CLITableRow<CLIGenericParamConstraintTableRow> {

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private static final byte[] MAP_OWNER_TABLES =
      new byte[] {CLITableConstants.CLI_TABLE_GENERIC_PARAM};

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private static final byte[] MAP_CONSTRAINT_TABLES =
      new byte[] {
        CLITableConstants.CLI_TABLE_TYPE_DEF,
        CLITableConstants.CLI_TABLE_TYPE_REF,
        CLITableConstants.CLI_TABLE_TYPE_SPEC
      };

  public CLIGenericParamConstraintTableRow(CLITables tables, int cursor, int rowIndex) {
    super(tables, cursor, rowIndex);
  }

  public final CLITablePtr getOwnerTablePtr() {
    int offset = 0;
    final int rowNo;
    if (areSmallEnough(MAP_OWNER_TABLES)) {
      rowNo = getShort(offset) & 0xFFFF;
    } else {
      rowNo = getInt(offset);
    }
    return new CLITablePtr(CLITableConstants.CLI_TABLE_GENERIC_PARAM, rowNo);
  }

  public final CLITablePtr getConstraintTablePtr() {
    int offset = 2;
    if (!areSmallEnough(MAP_OWNER_TABLES)) offset += 2;
    int codedValue;
    var isSmall = areSmallEnough(MAP_CONSTRAINT_TABLES);
    if (isSmall) {
      codedValue = getShort(offset) & 0xFFFF;
    } else {
      codedValue = getInt(offset);
    }
    if ((isSmall && (codedValue & 0xffff) == 0xffff)
        || (!isSmall && (codedValue & 0xffffffff) == 0xffffffff)) return null;
    return new CLITablePtr(
        MAP_CONSTRAINT_TABLES[codedValue & 3],
        (isSmall ? (0x0000ffff & codedValue) : codedValue) >>> 2);
  }

  @Override
  public int getLength() {
    int offset = 4;
    if (!areSmallEnough(MAP_OWNER_TABLES)) offset += 2;
    if (!areSmallEnough(MAP_CONSTRAINT_TABLES)) offset += 2;
    return offset;
  }

  @Override
  public byte getTableId() {
    return CLITableConstants.CLI_TABLE_GENERIC_PARAM_CONSTRAINT;
  }

  @Override
  protected CLIGenericParamConstraintTableRow createNew(
      CLITables tables, int cursor, int rowIndex) {
    return new CLIGenericParamConstraintTableRow(tables, cursor, rowIndex);
  }
}
