package com.vztekoverflow.bacil.parser.cli.tables.generated;

import com.vztekoverflow.bacil.parser.cli.tables.*;
public class CLIAssemblyRefProcessorTableRow extends CLITableRow<CLIAssemblyRefProcessorTableRow> {

	public CLIAssemblyRefProcessorTableRow(CLITables tables, int cursor, int rowIndex) {
		super(tables, cursor, rowIndex);
	}

	public final int getProcessor() {
		int offset = 0;
		return getInt(offset);
	}

	public final CLITablePtr getAssemblyRef() { 
		int offset = 4;
		return new CLITablePtr(CLITableConstants.CLI_TABLE_ASSEMBLY_REF, getShort(offset));
	}

	@Override
	public int getLength() {
		int offset = 6;
		return offset;
	}

	@Override
	public byte getTableId() {
		return CLITableConstants.CLI_TABLE_ASSEMBLY_REF_PROCESSOR;
	}

	@Override
	protected CLIAssemblyRefProcessorTableRow createNew(CLITables tables, int cursor, int rowIndex) {
		return new CLIAssemblyRefProcessorTableRow(tables, cursor, rowIndex);
	}

}
