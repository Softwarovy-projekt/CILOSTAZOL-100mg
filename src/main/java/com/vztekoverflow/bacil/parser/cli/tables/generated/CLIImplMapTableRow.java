package com.vztekoverflow.bacil.parser.cli.tables.generated;

import com.vztekoverflow.bacil.parser.cli.tables.*;
public class CLIImplMapTableRow extends CLITableRow<CLIImplMapTableRow> {

	public CLIImplMapTableRow(CLITables tables, int cursor, int rowIndex) {
		super(tables, cursor, rowIndex);
	}

	public final short getMappingFlags() {
		int offset = 0;
		return getShort(offset);
	}

	private static final byte[] MAP_MEMBER_FORWARDED_TABLES = new byte[] { CLITableConstants.CLI_TABLE_FIELD, CLITableConstants.CLI_TABLE_METHOD_DEF} ;
	public final CLITablePtr getMemberForwarded() { 
		int offset = 2;
		short codedValue = getShort(offset);
		return new CLITablePtr(MAP_MEMBER_FORWARDED_TABLES[codedValue & 1], codedValue >> 1);
	}

	public final CLIStringHeapPtr getImportName() {
		int offset = 4;
		int heapOffset=0;
		if (tables.isStringHeapBig()) { heapOffset = getInt(offset); } else { heapOffset = getShort(offset); }
		return new CLIStringHeapPtr(heapOffset);
	}

	public final CLITablePtr getImportScope() { 
		int offset = 6;
		if (tables.isStringHeapBig()) offset += 2;
		return new CLITablePtr(CLITableConstants.CLI_TABLE_MODULE_REF, getShort(offset));
	}

	@Override
	public int getLength() {
		int offset = 8;
		if (tables.isStringHeapBig()) offset += 2;
		return offset;
	}

	@Override
	public byte getTableId() {
		return CLITableConstants.CLI_TABLE_IMPL_MAP;
	}

	@Override
	protected CLIImplMapTableRow createNew(CLITables tables, int cursor, int rowIndex) {
		return new CLIImplMapTableRow(tables, cursor, rowIndex);
	}

}
