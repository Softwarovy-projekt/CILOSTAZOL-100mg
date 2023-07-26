package com.vztekoverflow.cilostazol.runtime.symbols;

import com.vztekoverflow.cil.parser.cli.CLIFile;
import com.vztekoverflow.cil.parser.cli.CLIFileUtils;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;

public final class ModuleSymbol extends Symbol {
  private final CLIFile definingFile;
  private final MethodSymbol[] methodCache;

  public ModuleSymbol(CLIFile definingFile) {
    super(ContextProviderImpl.getInstance());
    this.definingFile = definingFile;
    this.methodCache =
        new MethodSymbol
            [definingFile.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_METHOD_DEF)];
  }

  public CLIFile getDefiningFile() {
    return definingFile;
  }

  /**
   * @return the type with the given name and namespace, or null if not found in this module.
   * @apiNote If found, the type is cached in the context.
   */
  public NamedTypeSymbol getLocalType(String name, String namespace) {
    for (var row : definingFile.getTableHeads().getTypeDefTableHead()) {
      var rowName = row.getTypeNameHeapPtr().read(definingFile.getStringHeap());
      var rowNamespace = row.getTypeNamespaceHeapPtr().read(definingFile.getStringHeap());

      if (rowName.equals(name) && rowNamespace.equals(namespace)) {
        return NamedTypeSymbol.NamedTypeSymbolFactory.create(row, this);
      }
    }

    return null;
  }

  /**
   * @return the method with the given ptr, or null if not found in this module.
   * @apiNote If found, the method is cached in the ModuleSymbol.
   */
  public MethodSymbol getLocalMethod(CLITablePtr ptr) {
    if (methodCache[ptr.getRowNo()] == null) {
      var classRow =
          definingFile
              .getTableHeads()
              .getTypeDefTableHead()
              .skip(
                  new CLITablePtr(CLITableConstants.CLI_TABLE_TYPE_DEF, getDefiningFile().getIndicies().getMethodToClassIndex(ptr).getRowNo()));
      var nameAndNamespace = CLIFileUtils.getNameAndNamespace(definingFile, classRow);
      methodCache[ptr.getRowNo()] =
          MethodSymbol.MethodSymbolFactory.create(
              definingFile
                  .getTableHeads()
                  .getMethodDefTableHead()
                  .skip(new CLITablePtr(CLITableConstants.CLI_TABLE_METHOD_DEF, ptr.getRowNo())),
              getContext()
                  .getType(
                      nameAndNamespace.getLeft(),
                      nameAndNamespace.getRight(),
                      definingFile.getAssemblyIdentity()));
    }

    return methodCache[ptr.getRowNo()];
  }

  public static final class ModuleSymbolFactory {
    public static ModuleSymbol create(CLIFile file) {
      return new ModuleSymbol(file);
    }
  }
}
