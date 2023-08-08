package com.vztekoverflow.cilostazol.runtime.symbols;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.RootNode;
import com.vztekoverflow.cil.parser.ByteSequenceBuffer;
import com.vztekoverflow.cil.parser.CILParserException;
import com.vztekoverflow.cil.parser.cli.CLIFile;
import com.vztekoverflow.cil.parser.cli.signature.LocalVarsSig;
import com.vztekoverflow.cil.parser.cli.signature.MethodDefFlags;
import com.vztekoverflow.cil.parser.cli.signature.MethodDefSig;
import com.vztekoverflow.cil.parser.cli.signature.SignatureReader;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIMethodDefTableRow;
import com.vztekoverflow.cil.parser.cli.table.generated.CLIParamTableRow;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.exceptions.TypeSystemException;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLRootNode;
import com.vztekoverflow.cilostazol.runtime.context.ContextProviderImpl;
import com.vztekoverflow.cilostazol.staticanalysis.StaticOpCodeAnalyser;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MethodSymbol extends Symbol {
  protected final String name;
  protected final ModuleSymbol module;
  protected final NamedTypeSymbol definingType;
  // Flags
  protected final MethodDefFlags methodDefFlags;
  protected final MethodFlags methodFlags;
  protected final MethodImplFlags methodImplFlags;
  // Signature
  protected final TypeParameterSymbol[] typeParameters;
  protected final ParameterSymbol[] parameters;
  protected final LocalSymbol[] locals;
  protected final ReturnSymbol returnSymbol;
  protected final ExceptionHandlerSymbol[] exceptionHandlers;
  protected final byte[] cil;
  protected final byte[] originalCil;
  // body
  protected final int maxStack;
  protected final MethodHeaderFlags methodHeaderFlags;
  @CompilerDirectives.CompilationFinal protected RootNode node;

  @CompilerDirectives.CompilationFinal(dimensions = 1)
  private StaticOpCodeAnalyser.OpCodeType[] opCodeTypes = null;

  protected MethodSymbol(
      String name,
      ModuleSymbol module,
      NamedTypeSymbol definingType,
      MethodDefFlags methodDefFlags,
      MethodFlags methodFlags,
      MethodImplFlags methodImplFlags,
      TypeParameterSymbol[] typeParameters,
      ParameterSymbol[] parameters,
      LocalSymbol[] locals,
      ReturnSymbol returnSymbol,
      ExceptionHandlerSymbol[] exceptionHandlers,
      byte[] cil,
      int maxStack,
      MethodHeaderFlags methodHeaderFlags) {
    super(ContextProviderImpl.getInstance());
    this.name = name;
    this.module = module;
    this.definingType = definingType;
    this.methodDefFlags = methodDefFlags;
    this.methodFlags = methodFlags;
    this.methodImplFlags = methodImplFlags;
    this.typeParameters = typeParameters;
    this.parameters = parameters;
    this.locals = locals;
    this.returnSymbol = returnSymbol;
    this.exceptionHandlers = exceptionHandlers;
    this.cil = cil;
    this.originalCil = cil.clone();
    this.maxStack = maxStack;
    this.methodHeaderFlags = methodHeaderFlags;
  }

  // region Getters
  public NamedTypeSymbol getDefiningType() {
    return definingType;
  }

  public String getName() {
    return name;
  }

  public ParameterSymbol[] getParameters() {
    return parameters;
  }

  public int getParameterCountIncludingInstance() {
    return parameters.length + (getMethodFlags().hasFlag(MethodFlags.Flag.STATIC) ? 0 : 1);
  }

  public LocalSymbol[] getLocals() {
    return locals;
  }

  public ReturnSymbol getReturnType() {
    return returnSymbol;
  }

  public MethodDefFlags getMethodDefFlags() {
    return methodDefFlags;
  }

  public MethodFlags getMethodFlags() {
    return methodFlags;
  }

  public MethodImplFlags getMethodImplFlags() {
    return methodImplFlags;
  }

  public MethodHeaderFlags getMethodHeaderFlags() {
    return methodHeaderFlags;
  }

  public ExceptionHandlerSymbol[] getExceptionHandlers() {
    return exceptionHandlers;
  }

  public int getMaxStack() {
    return maxStack;
  }

  public MethodSymbol getDefinition() {
    return this;
  }

  public byte[] getCIL() {
    return cil;
  }

  public byte[] getOriginalCIL() {
    return originalCil;
  }

  public StaticOpCodeAnalyser.OpCodeType[] getOpCodeTypes() {
    if (opCodeTypes == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      opCodeTypes = StaticOpCodeAnalyser.analyseOpCodes(this);
    }
    return opCodeTypes;
  }

  public TypeSymbol[] getParameterTypesIncludingInstance() {
    boolean hasReceiver = !getMethodFlags().hasFlag(MethodFlags.Flag.STATIC);
    TypeSymbol[] paramTypes;

    if (hasReceiver) {
      paramTypes = new TypeSymbol[getParameterCountIncludingInstance()];
      paramTypes[0] = getDefiningType();
      for (int i = 0; i < parameters.length; i++) {
        paramTypes[i + 1] = parameters[i].getType();
      }

    } else {
      paramTypes =
          Arrays.stream(parameters).map(ParameterSymbol::getType).toArray(TypeSymbol[]::new);
    }

    return paramTypes;
  }

  public String toString() {
    return returnSymbol.toString()
        + " "
        + getName()
        + "("
        + Arrays.stream(getTypeParameters())
            .map(TypeParameterSymbol::getName)
            .collect(Collectors.joining())
        + ")";
  }

  public ModuleSymbol getModule() {
    return definingType.getDefiningModule();
  }

  public TypeParameterSymbol[] getTypeParameters() {
    return typeParameters;
  }

  public TypeSymbol[] getTypeArguments() {
    return typeParameters;
  }
  // endregion

  public ConstructedMethodSymbol construct(TypeSymbol[] typeArguments) {
    return ConstructedMethodSymbol.ConstructedMethodSymbolFactory.create(this, this, typeArguments);
  }

  public RootNode getNode() {
    if (node == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      node = CILOSTAZOLRootNode.create(this);
    }

    return node;
  }

  public boolean hasReturnValue() {
    return returnSymbol.getType().getSystemType()
        != com.vztekoverflow.cilostazol.runtime.objectmodel.SystemType.Void;
  }

  public boolean hasArgs() {
    return parameters.length > 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof MethodSymbol other && equals(other);
  }

  public boolean equals(MethodSymbol other) {
    return getName().equals(other.getName())
        && getReturnType().equals(other.getReturnType())
        && getDefiningType().equals(other.getDefiningType())
        && Arrays.equals(getTypeParameters(), other.getTypeParameters());
  }

  public static class MethodSymbolFactory {
    public static MethodSymbol create(CLIMethodDefTableRow mDef, NamedTypeSymbol definingType) {
      final TypeSymbol[] definingTypeTypeParams = definingType.getTypeArguments();
      final CLIFile file = definingType.definingModule.getDefiningFile();
      final MethodDefSig mSignature =
          MethodDefSig.parse(
              new SignatureReader(mDef.getSignatureHeapPtr().read(file.getBlobHeap())));
      final String name = mDef.getNameHeapPtr().read(file.getStringHeap());
      final MethodFlags flags = new MethodFlags(mDef.getFlags());

      // Type parameters parsing
      final TypeParameterSymbol[] typeParameters =
          TypeParameterSymbol.TypeParameterSymbolFactory.create(
              mSignature.getGenParamCount(),
              mDef.getPtr(),
              definingTypeTypeParams,
              definingType.getDefiningModule());

      final int codeSize;
      final short maxStackSize;
      final LocalSymbol[] locals;
      final int ilFlags;
      final MethodHeaderFlags methodHeaderFlags;
      final int headerSize;
      final byte[] cil;
      final ExceptionHandlerSymbol[] handlers;

      int rva = mDef.getRVA();
      if (rva == 0) {
        System.err.println(
            "Warning: Method "
                + definingType.getName()
                + "::"
                + name
                + " has no RVA (likely tagged as extern), skipping.");
      }

      // Method header parsing
      if (rva != 0 && !flags.hasFlag(MethodFlags.Flag.ABSTRACT)) {
        final ByteSequenceBuffer buf = file.getBuffer(rva);
        final byte firstByte = buf.getByte();
        final MethodHeaderFlags pom = new MethodHeaderFlags(firstByte);
        if (pom.hasFlag(MethodHeaderFlags.Flag.CORILMETHOD_TINYFORMAT)) {
          maxStackSize = 8;
          locals = new LocalSymbol[0];
          methodHeaderFlags = new MethodHeaderFlags(firstByte & 0x3);
          headerSize = 1;
          codeSize = (firstByte >> 2) & 0xFF;
        } else if (pom.hasFlag(MethodHeaderFlags.Flag.CORILMETHOD_FATFORMAT)) {
          final short firstWord = (short) (firstByte | (buf.getByte() << 8));
          methodHeaderFlags = new MethodHeaderFlags(firstWord & 0xFFF);
          headerSize = (firstWord >> 12);
          maxStackSize = buf.getShort();
          codeSize = buf.getInt();
          final int localTok = buf.getInt();
          // Locals parsing
          if (localTok == 0) {
            locals = new LocalSymbol[0];
          } else {
            locals =
                LocalSymbol.LocalSymbolFactory.create(
                    LocalVarsSig.read(
                        new SignatureReader(
                            file.getTableHeads()
                                .getStandAloneSigTableHead()
                                .skip(CLITablePtr.fromToken(localTok))
                                .getSignatureHeapPtr()
                                .read(file.getBlobHeap())),
                        definingType.definingModule.getDefiningFile()),
                    typeParameters,
                    definingTypeTypeParams,
                    definingType.getDefiningModule());
          }
          if (headerSize != 3) {
            throw new CILParserException(
                CILOSTAZOLBundle.message("cilostazol.exception.parser.fatHeader.size"));
          }
        } else {
          throw new TypeSystemException(
              CILOSTAZOLBundle.message("cilostazol.exception.parser.method.general"));
        }

        cil = buf.subSequence(codeSize).toByteArray();

        // Exception handlers parsing
        if (methodHeaderFlags.hasFlag(MethodHeaderFlags.Flag.CORILMETHOD_FATFORMAT)
            && methodHeaderFlags.hasFlag(MethodHeaderFlags.Flag.CORILMETHOD_MORESECTS)) {
          buf.setPosition(buf.getPosition() + codeSize);
          buf.align(4);
          handlers =
              ExceptionHandlerSymbol.ExceptionHandlerSymbolFactory.create(
                  buf, typeParameters, definingTypeTypeParams, definingType.getDefiningModule());
        } else {
          handlers = new ExceptionHandlerSymbol[0];
        }
      } else {
        codeSize = 0;
        maxStackSize = 0;
        locals = new LocalSymbol[0];
        ilFlags = 0;
        methodHeaderFlags = null;
        headerSize = 0;
        cil = new byte[0];
        handlers = new ExceptionHandlerSymbol[0];
      }

      // Sort params
      CLIParamTableRow[] params = new CLIParamTableRow[mSignature.getParams().length];
      CLIParamTableRow paramRow =
          file.getTableHeads().getParamTableHead().skip(mDef.getParamListTablePtr());
      for (int i = 0; i < params.length; i++) {
        params[i] = paramRow;
        paramRow = paramRow.next();
      }

      // Return type parsing
      ReturnSymbol returnSymbol =
          ReturnSymbol.ReturnSymbolFactory.create(
              mSignature.getRetType(),
              typeParameters,
              definingTypeTypeParams,
              definingType.getDefiningModule());

      // Parameters parsing
      final ParameterSymbol[] parameters =
          ParameterSymbol.ParameterSymbolFactory.create(
              mSignature.getParams(),
              params,
              typeParameters,
              definingTypeTypeParams,
              definingType.getDefiningModule());

      return new MethodSymbol(
          name,
          definingType.getDefiningModule(),
          definingType,
          mSignature.getMethodDefFlags(),
          flags,
          new MethodImplFlags(mDef.getImplFlags()),
          typeParameters,
          parameters,
          locals,
          returnSymbol,
          handlers,
          cil,
          maxStackSize,
          methodHeaderFlags);
    }
  }

  // region Flags
  public static class MethodHeaderFlags {
    // region Masks
    private static final int F_CORILMETHOD_FORMAT_MASK = 0x3;
    public final int _flags;
    // endregion

    public MethodHeaderFlags(int flags) {
      _flags = flags;
    }

    public boolean hasFlag(MethodHeaderFlags.Flag flag) {
      return switch (flag) {
        case CORILMETHOD_TINYFORMAT, CORILMETHOD_FATFORMAT -> (_flags & F_CORILMETHOD_FORMAT_MASK)
            == flag.code;
        default -> (_flags & flag.code) == flag.code;
      };
    }

    public enum Flag {
      CORILMETHOD_TINYFORMAT(0x2),
      CORILMETHOD_FATFORMAT(0x3),
      CORILMETHOD_INITLOCALS(0x10),
      CORILMETHOD_MORESECTS(0x8);

      public final int code;

      Flag(int code) {
        this.code = code;
      }
    }
  }

  public static class MethodFlags {
    // region Masks
    private static final int F_MEMBER_ACCESS_MASK = 0x0007;
    private static final int F_V_TABLE_LAYOUT_MASK = 0x0100;
    public final int _flags;
    // endregion

    public MethodFlags(int flags) {
      _flags = flags;
    }

    public boolean hasFlag(MethodFlags.Flag flag) {
      switch (flag) {
        case COMPILER_CONTROLLED:
        case PRIVATE:
        case FAM_AND_ASSEM:
        case ASSEM:
        case FAMILY:
        case FAM_OR_ASSEM:
        case PUBLIC:
          return (_flags & F_MEMBER_ACCESS_MASK) == flag.code;
        case REUSE_SLOT:
        case NEW_SLOT:
          return (_flags & F_V_TABLE_LAYOUT_MASK) == flag.code;
        default:
          return (_flags & flag.code) == flag.code;
      }
    }

    public enum Flag {
      COMPILER_CONTROLLED(0x0000),
      PRIVATE(0x0001),
      FAM_AND_ASSEM(0x0002),
      ASSEM(0x0003),
      FAMILY(0x0004),
      FAM_OR_ASSEM(0x0005),
      PUBLIC(0x0006),
      STATIC(0x0010),
      FINAL(0x0020),
      VIRTUAL(0x0040),
      HIDE_BY_SIG(0x0080),
      REUSE_SLOT(0x0000),
      NEW_SLOT(0x0100),
      STRICT(0x0200),
      ABSTRACT(0x0400),
      SPECIAL_NAME(0x0800),
      P_INVOKE_IMPL(0x2000),
      UNMANAGED_EXPORT(0x0008),
      RT_SPECIAL_NAME(0x1000),
      HAS_SECURITY(0x4000),
      REQUIRE_SEC_OBJECT(0x8000);

      public final int code;

      Flag(int code) {
        this.code = code;
      }
    }
  }

  public static class MethodImplFlags {
    // region Masks
    private static final int F_CODE_TYPE_MASK = 0x0003;
    private static final int F_MANAGED_MASK = 0x0004;
    public final int _flags;
    // endregion

    public MethodImplFlags(int flags) {
      _flags = flags;
    }

    public boolean hasFlag(MethodImplFlags.Flag flag) {
      switch (flag) {
        case IL:
        case NATIVE:
        case OPTIL:
        case RUNTIME:
          return (_flags & F_CODE_TYPE_MASK) == flag.code;
        case UNMANAGED:
        case MANAGED:
          return (_flags & F_MANAGED_MASK) == flag.code;
        default:
          return (_flags & flag.code) == flag.code;
      }
    }

    public enum Flag {
      IL(0x0000),
      NATIVE(0x0001),
      OPTIL(0x0002),
      RUNTIME(0x0003),
      UNMANAGED(0x0004),
      MANAGED(0x0000),
      FORWARD_REF(0x0010),
      PRESERVE_SIG(0x0080),
      INTERNAL_CALL(0x1000),
      SYNCHRONIZED(0x0020),
      NO_INLINING(0x0008),
      MAX_METHOD_IMPL_VAL(0xFFFF),
      NO_OPTIMIZATION(0x0040);

      public final int code;

      Flag(int code) {
        this.code = code;
      }
    }
  }
  // endregion
}
