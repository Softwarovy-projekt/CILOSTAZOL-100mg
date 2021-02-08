package com.vztekoverflow.bacil.parser.cli.tables.generated;

import com.vztekoverflow.bacil.parser.cli.tables.CLIBlobHeapPtr;
import com.vztekoverflow.bacil.parser.cli.tables.CLITablePtr;
import com.vztekoverflow.bacil.parser.cli.tables.CLITableRow;
import com.vztekoverflow.bacil.parser.cli.tables.CLITables;
public class CLIDeclSecurityTableRow extends CLITableRow<CLIDeclSecurityTableRow> {

	public CLIDeclSecurityTableRow(CLITables tables, int cursor, int rowIndex) {
		super(tables, cursor, rowIndex);
	}

	public final short getAction() {
		int offset = 0;
		return getShort(offset);
	}

	private static final byte[] MAP_PARENT_TABLES = new byte[] { CLITableConstants.CLI_TABLE_TYPE_DEF, CLITableConstants.CLI_TABLE_METHOD_DEF, CLITableConstants.CLI_TABLE_ASSEMBLY} ;
	public final CLITablePtr getParent() { 
		int offset = 2;
		int codedValue;
		if (areSmallEnough(CLITableConstants.CLI_TABLE_TYPE_DEF, CLITableConstants.CLI_TABLE_METHOD_DEF, CLITableConstants.CLI_TABLE_ASSEMBLY)) {codedValue = getShort(offset);} else {codedValue = getInt(offset);}
		return new CLITablePtr(MAP_PARENT_TABLES[codedValue & 3], codedValue >> 2);
	}

	public final CLIBlobHeapPtr getPermissionSet() {
		int offset = 4;
		if (!areSmallEnough(CLITableConstants.CLI_TABLE_TYPE_DEF, CLITableConstants.CLI_TABLE_METHOD_DEF, CLITableConstants.CLI_TABLE_ASSEMBLY)) offset += 2;
		int heapOffset=0;
		if (tables.isBlobHeapBig()) { heapOffset = getInt(offset); } else { heapOffset = getUShort(offset); }
		return new CLIBlobHeapPtr(heapOffset);
	}

	@Override
	public int getLength() {
		int offset = 6;
		if (tables.isBlobHeapBig()) offset += 2;
		if (!areSmallEnough(CLITableConstants.CLI_TABLE_TYPE_DEF, CLITableConstants.CLI_TABLE_METHOD_DEF, CLITableConstants.CLI_TABLE_ASSEMBLY)) offset += 2;
		return offset;
	}

	@Override
	public byte getTableId() {
		return CLITableConstants.CLI_TABLE_DECL_SECURITY;
	}

	@Override
	protected CLIDeclSecurityTableRow createNew(CLITables tables, int cursor, int rowIndex) {
		return new CLIDeclSecurityTableRow(tables, cursor, rowIndex);
	}

}
