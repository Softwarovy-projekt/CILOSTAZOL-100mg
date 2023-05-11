package com.vztekoverflow.cil.parser.cli.table.generated;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.cli.table.*;
public class CLIMethodImplTableRow extends CLITableRow<CLIMethodImplTableRow> {

	public CLIMethodImplTableRow(CLITables tables, int cursor, int rowIndex) {
		super(tables, cursor, rowIndex);
	}

	@CompilerDirectives.CompilationFinal(dimensions = 1)
	private static final byte[] MAP_KLASS_TABLES = new byte[] {CLITableConstants.CLI_TABLE_TYPE_DEF};
	public final CLITablePtr getKlassTablePtr() { 
		int offset = 0;
		final int rowNo;
		if (areSmallEnough(MAP_KLASS_TABLES)) {rowNo = getShort(offset);} else {rowNo = getInt(offset);}
		return new CLITablePtr(CLITableConstants.CLI_TABLE_TYPE_DEF, rowNo);
	}

	@CompilerDirectives.CompilationFinal(dimensions = 1)
	private static final byte[] MAP_METHOD_BODY_TABLES = new byte[] { CLITableConstants.CLI_TABLE_METHOD_DEF, CLITableConstants.CLI_TABLE_MEMBER_REF} ;
	public final CLITablePtr getMethodBodyTablePtr() { 
		int offset = 2;
		if (!areSmallEnough(MAP_KLASS_TABLES)) offset += 2;
		int codedValue;
		if (areSmallEnough(MAP_METHOD_BODY_TABLES)) {codedValue = getShort(offset);} else {codedValue = getInt(offset);}
		return new CLITablePtr(MAP_METHOD_BODY_TABLES[codedValue & 1], codedValue >> 1);
	}

	@CompilerDirectives.CompilationFinal(dimensions = 1)
	private static final byte[] MAP_METHOD_DECLARATION_TABLES = new byte[] { CLITableConstants.CLI_TABLE_METHOD_DEF, CLITableConstants.CLI_TABLE_MEMBER_REF} ;
	public final CLITablePtr getMethodDeclarationTablePtr() { 
		int offset = 4;
		if (!areSmallEnough(MAP_KLASS_TABLES)) offset += 2;
		if (!areSmallEnough(MAP_METHOD_BODY_TABLES)) offset += 2;
		int codedValue;
		if (areSmallEnough(MAP_METHOD_DECLARATION_TABLES)) {codedValue = getShort(offset);} else {codedValue = getInt(offset);}
		return new CLITablePtr(MAP_METHOD_DECLARATION_TABLES[codedValue & 1], codedValue >> 1);
	}

	@Override
	public int getLength() {
		int offset = 6;
		if (!areSmallEnough(MAP_KLASS_TABLES)) offset += 2;
		if (!areSmallEnough(MAP_METHOD_BODY_TABLES)) offset += 2;
		if (!areSmallEnough(MAP_METHOD_DECLARATION_TABLES)) offset += 2;
		return offset;
	}

	@Override
	public byte getTableId() {
		return CLITableConstants.CLI_TABLE_METHOD_IMPL;
	}

	@Override
	protected CLIMethodImplTableRow createNew(CLITables tables, int cursor, int rowIndex) {
		return new CLIMethodImplTableRow(tables, cursor, rowIndex);
	}

}