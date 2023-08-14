package com.vztekoverflow.cil.parser.cli.table;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants;

/** Class representing a generic pointer to a row in a CLI Metadata table. */
public class CLITablePtr {
  // A translation table for II.23.2.8 TypeDefOrRefOrSpecEncoded
  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private static final byte[] MAP_ENCODED =
      new byte[] {
        CLITableConstants.CLI_TABLE_TYPE_DEF,
        CLITableConstants.CLI_TABLE_TYPE_REF,
        CLITableConstants.CLI_TABLE_TYPE_SPEC
      };

  private final byte tableId;
  private final int rowNo;
  private final int token;

  /**
   * Create a new table pointer.
   *
   * @param tableId ID of the table this pointer points to
   * @param rowNo the row number this pointer points to
   */
  public CLITablePtr(byte tableId, int rowNo) {
    this.tableId = tableId;
    this.rowNo = rowNo;
    this.token = -1;
  }

  /**
   * Create a new table pointer.
   *
   * @param token the metadata token
   */
  public CLITablePtr(int token) {
    this.tableId = (byte) (token >> 24);
    this.rowNo = token & 0xFFFFFF;
    this.token = token;
  }

  /**
   * Create a table pointer from a metadata token (III.1.9 Metadata tokens).
   *
   * @param token the metadata token
   * @return a table pointer pointer equivalent to the specified token
   */
  public static CLITablePtr fromToken(int token) {
    return new CLITablePtr(token);
  }

  /**
   * Translate a TypeDefOrRefOrSpecEncoded value (as specified in II.23.2.8
   * TypeDefOrRefOrSpecEncoded) to a table pointer.
   *
   * @param typeDefOrRefOrSpecEncoded source value
   * @return a table pointer equivalent to the specified TypeDefOrRefOrSpecEncoded value
   */
  public static CLITablePtr fromTypeDefOrRefOrSpecEncoded(int typeDefOrRefOrSpecEncoded) {
    byte table = MAP_ENCODED[typeDefOrRefOrSpecEncoded & 3];
    int rowNo = typeDefOrRefOrSpecEncoded >> 2;
    return new CLITablePtr(table, rowNo);
  }

  public byte getTableId() {
    return tableId;
  }

  public int getRowNo() {
    return rowNo;
  }

  public boolean isEmpty() {
    return rowNo == 0;
  }

  public int getToken() {
    return token;
  }
}
