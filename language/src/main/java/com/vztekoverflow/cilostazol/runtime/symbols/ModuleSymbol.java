package com.vztekoverflow.cilostazol.runtime.symbols;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.cli.CLIFile;
import com.vztekoverflow.cil.parser.cli.CLIFileUtils;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIFieldTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIMethodDefTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITypeDefTableRow;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;

public final class ModuleSymbol extends Symbol {
  private final CLIFile definingFile;
  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private ClassIndex[] methodDefToMethodSymbolCache;
  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private ClassIndex[] fieldToFieldSymbolCache;

  public ModuleSymbol(CLIFile definingFile) {
    super(ContextProviderImpl.getInstance());
    this.definingFile = definingFile;
    this.methodDefToMethodSymbolCache = null;
    this.fieldToFieldSymbolCache = null;
  }

  public CLIFile getDefiningFile() {
    return definingFile;
  }

  //region Symbol resolving
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

  public ClassIndex getLocalField(CLIFieldTableRow row)
  {
    if (fieldToFieldSymbolCache == null)
    {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      initFieldSymbolCache();
    }

    return fieldToFieldSymbolCache[row.getRowNo()];
  }

  public ClassIndex getLocalMethod(CLIMethodDefTableRow row)
  {
    if (methodDefToMethodSymbolCache == null)
    {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      initMethodSymbolCache();
    }

    return methodDefToMethodSymbolCache[row.getRowNo()];
  }

  private void initMethodSymbolCache()
  {
    methodDefToMethodSymbolCache = new ClassIndex
            [definingFile.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_METHOD_DEF)+1];
    for (CLITypeDefTableRow klass : getDefiningFile().getTableHeads().getTypeDefTableHead()) {
      var nameAndNamespace = CLIFileUtils.getNameAndNamespace(getDefiningFile(), klass);
      var methodRange = CLIFileUtils.getMethodRange(getDefiningFile(), klass);
      int startIdx = methodRange.getLeft();
      int endIdx = methodRange.getRight();
      while (startIdx < endIdx) {
        methodDefToMethodSymbolCache[startIdx] =
                new ClassIndex(
                        getContext()
                                .getType(
                                        nameAndNamespace.getLeft(),
                                        nameAndNamespace.getRight(),
                                        getDefiningFile().getAssemblyIdentity()),
                        startIdx - methodRange.getLeft());
        startIdx++;
      }
    }
  }

  private void initFieldSymbolCache()
  {
    fieldToFieldSymbolCache = new ClassIndex[definingFile.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_FIELD)+1];
    for (CLITypeDefTableRow klass : getDefiningFile().getTableHeads().getTypeDefTableHead()) {
      var nameAndNamespace = CLIFileUtils.getNameAndNamespace(getDefiningFile(), klass);
      var fieldRange = CLIFileUtils.getFieldRange(getDefiningFile(), klass);
      int startIdx = fieldRange.getLeft();
      int endIdx = fieldRange.getRight();
      while (startIdx < endIdx) {
        fieldToFieldSymbolCache[startIdx] =
                new ClassIndex(
                        getContext()
                                .getType(
                                        nameAndNamespace.getLeft(),
                                        nameAndNamespace.getRight(),
                                        getDefiningFile().getAssemblyIdentity()),
                        startIdx - fieldRange.getLeft());
        startIdx++;
      }
    }
  }
  //endregion

  public static final class ModuleSymbolFactory {
    public static ModuleSymbol create(CLIFile file) {
      return new ModuleSymbol(file);
    }
  }

  public static final class ClassIndex {
    NamedTypeSymbol symbol;
    int index;

    public ClassIndex(NamedTypeSymbol symbol, int index) {
      this.symbol = symbol;
      this.index = index;
    }

    public NamedTypeSymbol getSymbol() {
      return symbol;
    }

    public int getIndex() {
      return index;
    }
  }
}
