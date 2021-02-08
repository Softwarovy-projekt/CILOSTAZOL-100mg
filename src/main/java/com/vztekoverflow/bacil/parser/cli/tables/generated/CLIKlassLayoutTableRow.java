package com.vztekoverflow.bacil.parser.cli.tables.generated;

import com.vztekoverflow.bacil.parser.cli.tables.CLITablePtr;
import com.vztekoverflow.bacil.parser.cli.tables.CLITableRow;
import com.vztekoverflow.bacil.parser.cli.tables.CLITables;
public class CLIKlassLayoutTableRow extends CLITableRow<CLIKlassLayoutTableRow> {

	public CLIKlassLayoutTableRow(CLITables tables, int cursor, int rowIndex) {
		super(tables, cursor, rowIndex);
	}

	public final short getPackingSize() {
		int offset = 0;
		return getShort(offset);
	}

	public final int getKlassSize() {
		int offset = 2;
		return getInt(offset);
	}

	public final CLITablePtr getParent() { 
		int offset = 6;
		final int rowNo;
		if (areSmallEnough(CLITableConstants.CLI_TABLE_TYPE_DEF)) {rowNo = getShort(offset);} else {rowNo = getInt(offset);}
		return new CLITablePtr(CLITableConstants.CLI_TABLE_TYPE_DEF, rowNo);
	}

	@Override
	public int getLength() {
		int offset = 8;
		if (!areSmallEnough(CLITableConstants.CLI_TABLE_TYPE_DEF)) offset += 2;
		return offset;
	}

	@Override
	public byte getTableId() {
		return CLITableConstants.CLI_TABLE_KLASS_LAYOUT;
	}

	@Override
	protected CLIKlassLayoutTableRow createNew(CLITables tables, int cursor, int rowIndex) {
		return new CLIKlassLayoutTableRow(tables, cursor, rowIndex);
	}

}
