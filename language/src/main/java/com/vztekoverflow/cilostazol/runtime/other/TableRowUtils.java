package com.vztekoverflow.cilostazol.runtime.other;

import com.vztekoverflow.cil.parser.cli.CLIFile;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIMemberRefTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIMethodDefTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIMethodSpecTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITypeDefTableRow;
import com.vztekoverflow.cilostazol.runtime.symbols.ModuleSymbol;

public class TableRowUtils {
  public static CLITypeDefTableRow getTypeDefRow(ModuleSymbol module, CLITablePtr ptr) {
    return module.getDefiningFile().getTableHeads().getTypeDefTableHead().skip(ptr);
  }

  public static CLIMemberRefTableRow getMemberRefRow(ModuleSymbol module, CLITablePtr ptr) {
    return module.getDefiningFile().getTableHeads().getMemberRefTableHead().skip(ptr);
  }

  public static CLIMethodSpecTableRow getMethodSpecRow(ModuleSymbol module, CLITablePtr ptr) {
    return module.getDefiningFile().getTableHeads().getMethodSpecTableHead().skip(ptr);
  }

  public static CLIMethodDefTableRow getMethodDefRow(ModuleSymbol module, CLITablePtr ptr) {
    return module.getDefiningFile().getTableHeads().getMethodDefTableHead().skip(ptr);
  }

  public static CLIMethodDefTableRow getMethodDefRow(CLIFile file, CLITablePtr ptr) {
    return file.getTableHeads().getMethodDefTableHead().skip(ptr);
  }
}
