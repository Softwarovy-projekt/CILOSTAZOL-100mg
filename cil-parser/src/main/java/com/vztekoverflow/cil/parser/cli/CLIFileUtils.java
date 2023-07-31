package com.vztekoverflow.cil.parser.cli;

import com.vztekoverflow.cil.parser.cli.table.generated.CLIMethodDefTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITypeDefTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITypeRefTableRow;
import java.util.ArrayList;
import org.graalvm.collections.Pair;

public class CLIFileUtils {
  public static CLIMethodDefTableRow[] getMethodByName(String name, CLIFile file) {
    ArrayList<CLIMethodDefTableRow> result = new ArrayList<>();

    for (CLIMethodDefTableRow row : file.getTableHeads().getMethodDefTableHead()) {
      if (row.getNameHeapPtr().read(file.getStringHeap()).equals(name)) result.add(row);
    }

    return result.toArray(new CLIMethodDefTableRow[result.size()]);
  }

  public static Pair<Integer, Integer> getMethodRange(CLIFile file, CLITypeDefTableRow row) {
    final var methodTablePtr = row.getMethodListTablePtr();
    final boolean isLastType =
        row.getRowNo() == file.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_TYPE_DEF);
    final int lastIdx =
        isLastType
            ? file.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_METHOD_DEF)
            : row.skip(1).getMethodListTablePtr().getRowNo();

    return Pair.create(methodTablePtr.getRowNo(), lastIdx);
  }

  public static Pair<Integer, Integer> getFieldRange(CLIFile file, CLITypeDefTableRow row) {
    final var fieldTablePtr = row.getFieldListTablePtr();
    final boolean isLastType =
        row.getRowNo() == file.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_TYPE_DEF);
    final int lastIdx =
        isLastType
            ? file.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_FIELD) + 1
            : row.skip(1).getFieldListTablePtr().getRowNo();

    return Pair.create(fieldTablePtr.getRowNo(), lastIdx);
  }

  public static Pair<String, String> getNameAndNamespace(CLIFile file, CLITypeDefTableRow row) {
    final var name = row.getTypeNameHeapPtr().read(file.getStringHeap());
    final var namespace = row.getTypeNamespaceHeapPtr().read(file.getStringHeap());
    return Pair.create(name, namespace);
  }

  public static Pair<String, String> getNameAndNamespace(CLIFile file, CLITypeRefTableRow row) {
    final var name = row.getTypeNameHeapPtr().read(file.getStringHeap());
    final var namespace = row.getTypeNamespaceHeapPtr().read(file.getStringHeap());
    return Pair.create(name, namespace);
  }
}
