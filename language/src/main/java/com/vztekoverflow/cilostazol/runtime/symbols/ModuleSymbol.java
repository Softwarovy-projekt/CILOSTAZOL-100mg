package com.vztekoverflow.cilostazol.runtime.symbols;

import static com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol.IS_TYPE_FORWARDER_FLAG_MASK;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cil.parser.cli.CLIFile;
import com.vztekoverflow.cil.parser.cli.CLIFileUtils;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIFieldTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIMethodDefTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITypeDefTableRow;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;
import com.vztekoverflow.cilostazol.runtime.other.FieldIndex;
import com.vztekoverflow.cilostazol.runtime.other.MethodIndex;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;

public final class ModuleSymbol extends Symbol {
  private final CLIFile definingFile;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private MethodIndex[] methodDefToMethodSymbolCache;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private FieldIndex[] fieldToFieldSymbolCache;

  public ModuleSymbol(CLIFile definingFile) {
    super(ContextProviderImpl.getInstance());
    this.definingFile = definingFile;
    this.methodDefToMethodSymbolCache = null;
    this.fieldToFieldSymbolCache = null;
  }

  public CLIFile getDefiningFile() {
    return definingFile;
  }

  // region Symbol resolving
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

    // search exported types
    for (var row : definingFile.getTableHeads().getExportedTypeTableHead()) {
      var rowName = row.getTypeNameHeapPtr().read(definingFile.getStringHeap());
      var rowNamespace = row.getTypeNamespaceHeapPtr().read(definingFile.getStringHeap());

      if (rowName.equals(name) && rowNamespace.equals(namespace)) {
        return (NamedTypeSymbol) SymbolResolver.resolveType(row, this);
      }
    }

    return null;
  }

  public AssemblyIdentity getLocalTypeDefiningAssembly(String name, String namespace) {
    for (var row : definingFile.getTableHeads().getExportedTypeTableHead()) {
      var rowName = row.getTypeNameHeapPtr().read(definingFile.getStringHeap());
      var rowNamespace = row.getTypeNamespaceHeapPtr().read(definingFile.getStringHeap());

      if (rowName.equals(name)
          && rowNamespace.equals(namespace)
          && row.getImplementationTablePtr().getTableId()
              == CLITableConstants.CLI_TABLE_ASSEMBLY_REF
          && (row.getFlags() & IS_TYPE_FORWARDER_FLAG_MASK) != 0) {

        var assemblyId =
            AssemblyIdentity.fromAssemblyRefRow(
                getDefiningFile().getStringHeap(),
                getDefiningFile()
                    .getTableHeads()
                    .getAssemblyRefTableHead()
                    .skip(row.getImplementationTablePtr()));

        var assembly = getContext().resolveAssembly(assemblyId);
        if (assembly == null) return null;

        return assembly.getLocalTypeDefiningAssembly(name, namespace);
      }
    }

    return getDefiningFile().getAssemblyIdentity();
  }

  public FieldIndex getLocalField(CLIFieldTableRow row) {
    if (fieldToFieldSymbolCache == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      initFieldSymbolCache();
    }

    return fieldToFieldSymbolCache[row.getRowNo()];
  }

  public MethodIndex getLocalMethod(CLIMethodDefTableRow row) {
    if (methodDefToMethodSymbolCache == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      initMethodSymbolCache();
    }

    return methodDefToMethodSymbolCache[row.getRowNo()];
  }

  private void initMethodSymbolCache() {
    methodDefToMethodSymbolCache =
        new MethodIndex
            [definingFile.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_METHOD_DEF)
                + 1];
    for (CLITypeDefTableRow klass : getDefiningFile().getTableHeads().getTypeDefTableHead()) {
      var nameAndNamespace = CLIFileUtils.getNameAndNamespace(getDefiningFile(), klass);
      var methodRange = CLIFileUtils.getMethodRange(getDefiningFile(), klass);
      int startIdx = methodRange.getLeft();
      int endIdx = methodRange.getRight();
      while (startIdx < endIdx) {
        methodDefToMethodSymbolCache[startIdx] =
            new MethodIndex(
                getContext()
                    .resolveType(
                        nameAndNamespace.getLeft(),
                        nameAndNamespace.getRight(),
                        getDefiningFile().getAssemblyIdentity()),
                startIdx - methodRange.getLeft());
        startIdx++;
      }
    }
  }

  private void initFieldSymbolCache() {
    fieldToFieldSymbolCache =
        new FieldIndex
            [definingFile.getTablesHeader().getRowCount(CLITableConstants.CLI_TABLE_FIELD) + 1];
    for (CLITypeDefTableRow klass : getDefiningFile().getTableHeads().getTypeDefTableHead()) {
      var nameAndNamespace = CLIFileUtils.getNameAndNamespace(getDefiningFile(), klass);
      var fieldRange = CLIFileUtils.getFieldRange(getDefiningFile(), klass);
      int startIdx = fieldRange.getLeft();
      int endIdx = fieldRange.getRight();
      while (startIdx < endIdx) {
        fieldToFieldSymbolCache[startIdx] =
            new FieldIndex(
                getContext()
                    .resolveType(
                        nameAndNamespace.getLeft(),
                        nameAndNamespace.getRight(),
                        getDefiningFile().getAssemblyIdentity()),
                startIdx - fieldRange.getLeft());
        startIdx++;
      }
    }
  }
  // endregion

  public static final class ModuleSymbolFactory {
    public static ModuleSymbol create(CLIFile file) {
      return new ModuleSymbol(file);
    }
  }
}
