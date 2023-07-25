package com.vztekoverflow.cilostazol.nodes;

import static com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions.*;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.HostCompilerDirectives;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.BytecodeOSRNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.vztekoverflow.cil.parser.bytecode.BytecodeBuffer;
import com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.CLIUSHeapPtr;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.exceptions.InvalidCLIException;
import com.vztekoverflow.cilostazol.exceptions.NotImplementedException;
import com.vztekoverflow.cilostazol.nodes.nodeized.*;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.TypeHelpers;
import com.vztekoverflow.cilostazol.runtime.symbols.*;
import java.util.Arrays;

public class CILMethodNode extends CILNodeBase implements BytecodeOSRNode {
  private final MethodSymbol method;
  private final byte[] cil;
  private final BytecodeBuffer bytecodeBuffer;
  private final FrameDescriptor frameDescriptor;
  private final TypeSymbol[] taggedFrame;

  @Children private NodeizedNodeBase[] nodes = new NodeizedNodeBase[0];

  private CILMethodNode(MethodSymbol method, byte[] cilCode) {
    this.method = method;
    cil = cilCode;
    frameDescriptor =
        CILOSTAZOLFrame.create(
            method.getParameters().length, method.getLocals().length, method.getMaxStack());
    this.bytecodeBuffer = new BytecodeBuffer(cil);
    taggedFrame = createTaggedFrame(method.getLocals());
  }

  public static CILMethodNode create(MethodSymbol method, byte[] cilCode) {
    return new CILMethodNode(method, cilCode);
  }

  private TypeSymbol[] createTaggedFrame(LocalSymbol[] localSymbols) {
    var taggedFrame = new TypeSymbol[frameDescriptor.getNumberOfSlots()];
    if (!getMethod().getMethodFlags().hasFlag(MethodSymbol.MethodFlags.Flag.STATIC))
      CILOSTAZOLFrame.putTaggedStack(taggedFrame, 0, getMethod().getDefiningType());

    final int localOffset = CILOSTAZOLFrame.getStartLocalsOffset(getMethod());
    for (int i = 0; i < localSymbols.length; i++) {
      CILOSTAZOLFrame.putTaggedStack(taggedFrame, localOffset + i, localSymbols[i].getType());
    }

    return taggedFrame;
  }

  public MethodSymbol getMethod() {
    return method;
  }

  public FrameDescriptor getFrameDescriptor() {
    return frameDescriptor;
  }

  // region CILNodeBase
  @Override
  public Object execute(VirtualFrame frame) {
    initializeFrame(frame);
    return execute(frame, 0, CILOSTAZOLFrame.getStartStackOffset(method));
  }

  private void initializeFrame(VirtualFrame frame) {
    // Init arguments
    Object[] args = frame.getArguments();

    boolean hasReceiver =
        !getMethod().getMethodFlags().hasFlag(MethodSymbol.MethodFlags.Flag.STATIC);
    int receiverSlot = CILOSTAZOLFrame.getStartLocalsOffset(getMethod());
    if (hasReceiver) {
      throw new NotImplementedException();
    }

    TypeSymbol[] argTypes =
        Arrays.stream(method.getParameters())
            .map(ParameterSymbol::getType)
            .toArray(TypeSymbol[]::new);
    int topStack = CILOSTAZOLFrame.getStartArgsOffset(getMethod());

    for (int i = 0; i < method.getParameters().length; i++) {
      switch (argTypes[i].getStackTypeKind()) {
        case Int:
          CILOSTAZOLFrame.putInt(frame, topStack, (int) args[i + receiverSlot]);
          break;
        case Long:
          CILOSTAZOLFrame.putLong(frame, topStack, (long) args[i + receiverSlot]);
          break;
        case Double:
          CILOSTAZOLFrame.putDouble(frame, topStack, (double) args[i + receiverSlot]);
          break;
        case Object:
          CILOSTAZOLFrame.putObject(frame, topStack, (StaticObject) args[i + receiverSlot]);
          break;
        default:
          throw new NotImplementedException();
      }
      CILOSTAZOLFrame.putTaggedStack(taggedFrame, topStack, argTypes[i + receiverSlot]);
      topStack--;
    }
  }
  // endregion

  // region OSR
  @Override
  public Object executeOSR(VirtualFrame osrFrame, int target, Object interpreterState) {
    throw new NotImplementedException();
  }

  @Override
  public Object getOSRMetadata() {
    throw new NotImplementedException();
  }

  @Override
  public void setOSRMetadata(Object osrMetadata) {
    throw new NotImplementedException();
  }
  // endregion

  @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.MERGE_EXPLODE)
  @HostCompilerDirectives.BytecodeInterpreterSwitch
  private Object execute(VirtualFrame frame, int pc, int topStack) {

    while (true) {
      int curOpcode = bytecodeBuffer.getOpcode(pc);
      int nextpc = bytecodeBuffer.nextInstruction(pc);
      switch (curOpcode) {
        case NOP:
        case POP:
          CILOSTAZOLFrame.popTaggedStack(taggedFrame, topStack - 1);
          break;
        case DUP:
          duplicateSlot(frame, topStack - 1);
          break;

          // Loading on top of the stack
        case LDNULL:
          loadNull(frame, topStack);
          break;
        case LDC_I4_M1:
        case LDC_I4_0:
        case LDC_I4_1:
        case LDC_I4_2:
        case LDC_I4_3:
        case LDC_I4_4:
        case LDC_I4_5:
        case LDC_I4_6:
        case LDC_I4_7:
        case LDC_I4_8:
          loadValueOnTop(frame, topStack, curOpcode - LDC_I4_0);
          break;
        case LDC_I4_S:
          loadValueOnTop(frame, topStack, bytecodeBuffer.getImmByte(pc));
          break;
        case LDC_I4:
          loadValueOnTop(frame, topStack, bytecodeBuffer.getImmInt(pc));
          break;
        case LDC_I8:
          loadValueOnTop(frame, topStack, bytecodeBuffer.getImmLong(pc));
          break;
        case LDC_R4:
          loadValueOnTop(frame, topStack, Float.intBitsToFloat(bytecodeBuffer.getImmInt(pc)));
          break;
        case LDC_R8:
          loadValueOnTop(frame, topStack, Double.longBitsToDouble(bytecodeBuffer.getImmLong(pc)));
          break;
        case LDSTR:
          topStack = nodeizeOpToken(frame, topStack, bytecodeBuffer.getImmToken(pc), pc, curOpcode);
          break;

          // Storing to locals
        case STLOC_0:
        case STLOC_1:
        case STLOC_2:
        case STLOC_3:
          storeValueToLocal(frame, curOpcode - STLOC_0, topStack - 1);
          break;
        case STLOC_S:
          storeValueToLocal(frame, bytecodeBuffer.getImmUByte(pc), topStack - 1);
          break;

          // Loading locals to top
        case LDLOC_0:
        case LDLOC_1:
        case LDLOC_2:
        case LDLOC_3:
          loadLocalToTop(frame, curOpcode - LDLOC_0, topStack);
          break;
        case LDLOC_S:
          loadLocalToTop(frame, bytecodeBuffer.getImmUByte(pc), topStack);
          break;
        case LDLOCA_S:
          loadLocalRefToTop(frame, bytecodeBuffer.getImmUByte(pc), topStack);
          break;

          // Loading args to top
        case LDARG_0:
        case LDARG_1:
        case LDARG_2:
        case LDARG_3:
          loadArgToTop(frame, curOpcode - LDARG_0, topStack);
          break;
        case LDARG_S:
          loadArgToTop(frame, bytecodeBuffer.getImmUByte(pc), topStack);
          break;
        case LDARGA_S:
          loadArgRefToTop(frame, bytecodeBuffer.getImmUByte(pc), topStack);
          break;

          // Loading fields
        case LDFLD:
          topStack = nodeizeOpToken(frame, topStack, bytecodeBuffer.getImmToken(pc), pc, curOpcode);
          break;
        case LDFLDA:
        case LDSFLD:
        case LDSFLDA:
          // TODO
          // topStack = nodeizeOpToken(frame, topStack, bytecodeBuffer.getImmToken(pc), pc,
          // curOpcode);
          break;

          // Storing fields
        case STFLD:
          topStack = nodeizeOpToken(frame, topStack, bytecodeBuffer.getImmToken(pc), pc, curOpcode);
          break;
        case STSFLD:
          // TODO
          break;

          // Object manipulation
        case LDOBJ:
        case STOBJ:
          // TODO
          break;

        case INITOBJ:
        case NEWOBJ:
          topStack = nodeizeOpToken(frame, topStack, bytecodeBuffer.getImmToken(pc), pc, curOpcode);
          break;
        case CPOBJ:
          copyObject(frame, topStack - 2, topStack - 1);
        case ISINST:
          // TODO
          break;

          // Branching
        case BEQ:
        case BGE:
        case BGT:
        case BLE:
        case BLT:
        case BGE_UN:
        case BGT_UN:
        case BLE_UN:
        case BLT_UN:
        case BNE_UN:
          if (binaryCompare(curOpcode, frame, topStack - 2, topStack - 1)) {
            // TODO: OSR support
            pc = nextpc + bytecodeBuffer.getImmInt(pc);
            topStack += BytecodeInstructions.getStackEffect(curOpcode);
            continue;
          }
          break;

          // Branching - short
        case BEQ_S:
        case BGE_S:
        case BGT_S:
        case BLE_S:
        case BLT_S:
        case BGE_UN_S:
        case BGT_UN_S:
        case BLE_UN_S:
        case BLT_UN_S:
        case BNE_UN_S:
          if (binaryCompare(curOpcode, frame, topStack - 2, topStack - 1)) {
            // TODO: OSR support
            pc = nextpc + bytecodeBuffer.getImmByte(pc);
            topStack += BytecodeInstructions.getStackEffect(curOpcode);
            continue;
          }
          break;

        case BR:
          // TODO: OSR support
          pc = nextpc + bytecodeBuffer.getImmInt(pc);
          continue;
        case BR_S:
          // TODO: OSR support
          pc = nextpc + bytecodeBuffer.getImmByte(pc);
          continue;

        case BRTRUE:
        case BRFALSE:
          if (shouldBranch(curOpcode, frame, topStack - 1)) {
            // TODO: OSR support
            pc = nextpc + bytecodeBuffer.getImmInt(pc);
            topStack += BytecodeInstructions.getStackEffect(curOpcode);
            continue;
          }
          break;

        case BRTRUE_S:
        case BRFALSE_S:
          if (shouldBranch(curOpcode, frame, topStack - 1)) {
            // TODO: OSR support
            pc = nextpc + bytecodeBuffer.getImmByte(pc);
            topStack += BytecodeInstructions.getStackEffect(curOpcode);
            continue;
          }
          break;

        case CEQ:
        case CGT:
        case CLT:
        case CGT_UN:
        case CLT_UN:
          binaryCompareAndPutOnTop(curOpcode, frame, topStack - 2, topStack - 1);
          break;

        case BREAK:
          // Inform a debugger that a breakpoint has been reached
          // This does not interest us at the moment
          break;

        case JMP:
          // Exit current method and jump to the specified method
          // TODO - finish after function calls are done
          break;

        case SWITCH:
          // TODO: - find out the how to trigger this instruction from C#
          break;

        case RET:
          return getReturnValue(frame, topStack - 1);

        case CALL:
          System.out.println("CALLING");
          break;

          // Store indirect
        case STIND_I1:
        case STIND_I2:
        case STIND_I4:
        case STIND_I8:
        case STIND_I:
        case STIND_R4:
        case STIND_R8:
        case STIND_REF:
          storeIndirect(frame, topStack - 1);
          break;

          // Load indirect
        case LDIND_I1:
        case LDIND_U1:
        case LDIND_I2:
        case LDIND_U2:
        case LDIND_I4:
        case LDIND_U4:
        case LDIND_I8:
        case LDIND_I:
        case LDIND_R4:
        case LDIND_R8:
        case LDIND_REF:
          loadIndirect(frame, topStack - 1);
          break;

        case CONV_I:
        case CONV_I1:
        case CONV_I2:
        case CONV_I4:
        case CONV_I8:
          convertToInteger(curOpcode, frame, topStack - 1, true);
          break;
        case CONV_U:
        case CONV_U1:
        case CONV_U2:
        case CONV_U4:
        case CONV_U8:
          convertToInteger(curOpcode, frame, topStack - 1, false);
          break;

        case CONV_R4:
        case CONV_R8:
          break;

        case TRUFFLE_NODE:
          topStack = nodes[bytecodeBuffer.getImmInt(pc)].execute(frame, taggedFrame);
          break;

        default:
          System.out.println("Opcode not implemented: " + curOpcode);
          break;
      }

      topStack += BytecodeInstructions.getStackEffect(curOpcode);
      pc = nextpc;
    }
  }

  // region Helpers
  private void loadIndirect(VirtualFrame frame, int top) {
    // TODO: Check types, do we need to distinguish types ?
    var referenceType = (ReferenceSymbol) CILOSTAZOLFrame.getTaggedStack(taggedFrame, top);
    assert referenceType.getStackTypeKind() == CILOSTAZOLFrame.StackType.Int;

    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top);
    int index = CILOSTAZOLFrame.popInt(frame, top);

    if (referenceType instanceof ArgReferenceSymbol)
      index += CILOSTAZOLFrame.getStartArgsOffset(getMethod());
    else if (referenceType instanceof LocalReferenceSymbol)
      index += CILOSTAZOLFrame.getStartLocalsOffset(getMethod());
    CILOSTAZOLFrame.copyStatic(frame, index, top);
    CILOSTAZOLFrame.putTaggedStack(taggedFrame, top, referenceType.getUnderlyingTypeSymbol());
  }

  private void storeIndirect(VirtualFrame frame, int top) {
    // TODO: Check types
    var referenceType = (ReferenceSymbol) CILOSTAZOLFrame.getTaggedStack(taggedFrame, top - 1);
    int index = CILOSTAZOLFrame.getLocalInt(frame, top - 1);
    if (referenceType instanceof ArgReferenceSymbol)
      index += CILOSTAZOLFrame.getStartArgsOffset(getMethod());
    else if (referenceType instanceof LocalReferenceSymbol)
      index += CILOSTAZOLFrame.getStartLocalsOffset(getMethod());
    CILOSTAZOLFrame.copyStatic(frame, top, index);

    CILOSTAZOLFrame.pop(frame, top, CILOSTAZOLFrame.popTaggedStack(taggedFrame, top));
    CILOSTAZOLFrame.pop(frame, top - 1, CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1));
  }

  private void loadValueOnTop(VirtualFrame frame, int top, int value) {
    // We want to tag the stack by types in it
    CILOSTAZOLFrame.putInt(frame, top, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top, getMethod().getContext().getType(CILOSTAZOLContext.CILBuiltInType.Int32));
  }

  private void loadValueOnTop(VirtualFrame frame, int top, long value) {
    // We want to tag the stack by types in it
    CILOSTAZOLFrame.putLong(frame, top, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top, getMethod().getContext().getType(CILOSTAZOLContext.CILBuiltInType.Int64));
  }

  private void loadValueOnTop(VirtualFrame frame, int top, double value) {
    // We want to tag the stack by types in it
    CILOSTAZOLFrame.putDouble(frame, top, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame,
        top,
        getMethod().getContext().getType(CILOSTAZOLContext.CILBuiltInType.Double));
  }

  private void loadValueOnTop(VirtualFrame frame, int top, float value) {
    // We want to tag the stack by types in it
    CILOSTAZOLFrame.putDouble(frame, top, value);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame,
        top,
        getMethod().getContext().getType(CILOSTAZOLContext.CILBuiltInType.Single));
  }

  private void loadLocalToTop(VirtualFrame frame, int localIdx, int top) {
    int localSlot = CILOSTAZOLFrame.getStartLocalsOffset(getMethod()) + localIdx;
    CILOSTAZOLFrame.copyStatic(frame, localSlot, top);
    // Tag the top of the stack
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top, CILOSTAZOLFrame.getTaggedStack(taggedFrame, localSlot));
  }

  private void loadArgToTop(VirtualFrame frame, int argIdx, int top) {
    int argSlot = CILOSTAZOLFrame.getStartArgsOffset(getMethod()) + argIdx;
    CILOSTAZOLFrame.copyStatic(frame, argSlot, top);
    // Tag the top of the stack
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top, CILOSTAZOLFrame.getTaggedStack(taggedFrame, argSlot));
  }

  private void loadLocalRefToTop(VirtualFrame frame, int localIdx, int top) {
    int localSlot = CILOSTAZOLFrame.getStartLocalsOffset(getMethod()) + localIdx;
    CILOSTAZOLFrame.putInt(frame, top, localSlot);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame,
        top,
        new LocalReferenceSymbol(CILOSTAZOLFrame.getTaggedStack(taggedFrame, localSlot)));
  }

  private void loadArgRefToTop(VirtualFrame frame, int argIdx, int top) {
    int argSlot = CILOSTAZOLFrame.getStartArgsOffset(getMethod()) + argIdx;
    CILOSTAZOLFrame.putInt(frame, top, argSlot);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame,
        top,
        new ArgReferenceSymbol(CILOSTAZOLFrame.getTaggedStack(taggedFrame, argSlot)));
  }

  private void loadNull(VirtualFrame frame, int top) {
    // In this situation we don't know the type of null yet -> it will be determined later
    CILOSTAZOLFrame.putObject(frame, top, StaticObject.NULL);
    CILOSTAZOLFrame.putTaggedStack(taggedFrame, top, new NullSymbol());
  }

  private void storeValueToLocal(VirtualFrame frame, int localIdx, int top) {
    // Locals are already typed
    // TODO: type checking
    CILOSTAZOLFrame.copyStatic(frame, top, localIdx);
    // pop taggedFrame
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top);
  }

  private void duplicateSlot(VirtualFrame frame, int top) {
    CILOSTAZOLFrame.copyStatic(frame, top, top + 1);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top + 1, CILOSTAZOLFrame.getTaggedStack(taggedFrame, top));
  }

  private void copyObject(VirtualFrame frame, int sourceIdx, int descIdx) {
    int localsOffset = CILOSTAZOLFrame.getStartLocalsOffset(getMethod());
    int sourceSlot = localsOffset + CILOSTAZOLFrame.popInt(frame, sourceIdx);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, sourceIdx);
    int descSlot = localsOffset + CILOSTAZOLFrame.popInt(frame, descIdx);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, descIdx);
    CILOSTAZOLFrame.copyStatic(frame, sourceSlot, descSlot);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, descSlot, CILOSTAZOLFrame.getTaggedStack(taggedFrame, sourceSlot));
  }

  private void popStack(int top) {
    // pop taggedFrame
    taggedFrame[top] = null;
  }

  private Object getReturnValue(VirtualFrame frame, int top) {
    TypeSymbol retType = getMethod().getReturnType().getType();
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top);
    // TODO: type checking
    switch (retType.getStackTypeKind()) {
      case Int -> {
        return CILOSTAZOLFrame.popInt(frame, top);
      }
      case Long -> {
        return CILOSTAZOLFrame.popLong(frame, top);
      }
      case Double -> {
        return CILOSTAZOLFrame.popDouble(frame, top);
      }
      case Void -> {
        return null;
      }
      case Object -> {
        return CILOSTAZOLFrame.popObject(frame, top);
      }
      default -> {
        throw new InvalidCLIException();
      }
    }
  }
  // endregion

  // region Nodeization
  /**
   * Get a byte[] representing an instruction with the specified opcode and a 32-bit immediate
   * value.
   *
   * @param opcode opcode of the new instruction
   * @param imm 32-bit immediate value of the new instruction
   * @param targetLength the length of the resulting patch, instruction will be padded with NOPs
   * @return The new instruction bytes.
   */
  private byte[] preparePatch(byte opcode, int imm, int targetLength) {
    assert (targetLength >= 5); // Smaller instructions won't fit the 32-bit immediate
    byte[] patch = new byte[targetLength];
    patch[0] = opcode;
    patch[1] = (byte) (imm & 0xFF);
    patch[2] = (byte) ((imm >> 8) & 0xFF);
    patch[3] = (byte) ((imm >> 16) & 0xFF);
    patch[4] = (byte) ((imm >> 24) & 0xFF);
    return patch;
  }

  private int nodeizeOpToken(VirtualFrame frame, int top, CLITablePtr token, int pc, int opcode) {
    CompilerDirectives.transferToInterpreterAndInvalidate();
    final NodeizedNodeBase node;
    switch (opcode) {
      case LDSTR:
        CLIUSHeapPtr ptr = new CLIUSHeapPtr(token.getRowNo());
        node =
            new LDSTRNode(
                ptr.readString(method.getModule().getDefiningFile().getUSHeap()),
                top,
                method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.String));
        break;
      case NEWOBJ:
        // TODO: Requires CALL to be implemented
        node = null;
        break;
      case INITOBJ:
        {
          // TODO: This is not cached and causes issues when working with fields
          var type =
              NamedTypeSymbol.NamedTypeSymbolFactory.create(
                  method
                      .getModule()
                      .getDefiningFile()
                      .getTableHeads()
                      .getTypeDefTableHead()
                      .skip(token),
                  method.getModule());
          node = INITOBJNodeGen.create(type, top);
        }
        break;
      case LDFLD:
        {
          TypeSymbol type;
          if (CILOSTAZOLFrame.getTaggedStack(taggedFrame, top - 1) instanceof ReferenceSymbol) {
            type = CILOSTAZOLFrame.getTaggedStack(taggedFrame, frame.getIntStatic(top - 1));
          } else {
            type = CILOSTAZOLFrame.getTaggedStack(taggedFrame, top - 1);
          }
          assert type instanceof NamedTypeSymbol;
          node = LDFLDNodeGen.create(method, (NamedTypeSymbol) type, token, top);
        }
        break;
      case STFLD:
        {
          TypeSymbol type;
          if (CILOSTAZOLFrame.getTaggedStack(taggedFrame, top - 2) instanceof ReferenceSymbol) {
            type = CILOSTAZOLFrame.getTaggedStack(taggedFrame, frame.getIntStatic(top - 2));
          } else {
            type = CILOSTAZOLFrame.getTaggedStack(taggedFrame, top - 2);
          }
          assert type instanceof NamedTypeSymbol;
          node = STFLDNodeGen.create(method, (NamedTypeSymbol) type, token, top);
        }
        break;
      default:
        CompilerAsserts.neverPartOfCompilation();
        throw new InterpreterException();
    }

    int index = addNode(node);

    byte[] patch =
        preparePatch(
            (byte) TRUFFLE_NODE,
            index,
            com.vztekoverflow.bacil.bytecode.BytecodeInstructions.getLength(opcode));
    bytecodeBuffer.patchBytecode(pc, patch);

    // execute the new node
    return nodes[index].execute(frame, taggedFrame);
  }

  private int addNode(NodeizedNodeBase node) {
    CompilerAsserts.neverPartOfCompilation();
    nodes = Arrays.copyOf(nodes, nodes.length + 1);
    int nodeIndex = nodes.length - 1; // latest empty slot
    nodes[nodeIndex] = insert(node);
    return nodeIndex;
  }
  // endregion

  // region Conversion
  private void convertToInteger(int opcode, VirtualFrame frame, int top, boolean signed) {
    var type = CILOSTAZOLFrame.popTaggedStack(taggedFrame, top);
    long value =
        switch (type.getStackTypeKind()) {
          case Int -> signed
              ? TypeHelpers.signExtend32(CILOSTAZOLFrame.popInt(frame, top))
              : TypeHelpers.zeroExtend32(CILOSTAZOLFrame.popInt(frame, top));
          case Long -> CILOSTAZOLFrame.popLong(frame, top);
          case Double -> (long) CILOSTAZOLFrame.popDouble(frame, top);
          default -> throw new InterpreterException(
              "Invalid type for conversion: " + type.getStackTypeKind());
        };

    switch (opcode) {
      case CONV_I1:
        CILOSTAZOLFrame.putInt(frame, top, TypeHelpers.signExtend8(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.SByte));
        break;
      case CONV_I2:
        CILOSTAZOLFrame.putInt(frame, top, TypeHelpers.signExtend16(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Int16));
        break;
      case CONV_I4:
        CILOSTAZOLFrame.putInt(frame, top, (int) TypeHelpers.truncate32(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Int32));
        break;
      case CONV_I8:
        CILOSTAZOLFrame.putLong(frame, top, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Int64));
        break;
      case CONV_U1:
        CILOSTAZOLFrame.putInt(frame, top, TypeHelpers.zeroExtend8(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.Byte));
        break;
      case CONV_U2:
        CILOSTAZOLFrame.putInt(frame, top, TypeHelpers.zeroExtend16(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.UInt16));
        break;
      case CONV_U4:
        CILOSTAZOLFrame.putLong(frame, top, TypeHelpers.zeroExtend32(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.UInt32));
        break;
      case CONV_U8:
        CILOSTAZOLFrame.putLong(frame, top, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, method.getContext().getType(CILOSTAZOLContext.CILBuiltInType.UInt64));
        break;
      case CONV_U:
      case CONV_I:
        // TODO
        CompilerAsserts.neverPartOfCompilation();
        throw new InterpreterException("CONV_U/I not implemented");
      default:
        CompilerAsserts.neverPartOfCompilation();
        throw new InterpreterException("Invalid opcode for conversion");
    }
  }
  // endregion

  // region Branching
  /**
   * Evaluate whether the branch should be taken for simple (true/false) conditional branch
   * instructions based on a value on the evaluation stack.
   *
   * @return whether to take the branch or not
   */
  private boolean shouldBranch(int opcode, VirtualFrame frame, int slot) {
    boolean value;
    if (taggedFrame[slot].getStackTypeKind() == CILOSTAZOLFrame.StackType.Object) {
      value = CILOSTAZOLFrame.popObject(frame, slot) != StaticObject.NULL;
    } else {
      value = CILOSTAZOLFrame.popInt(frame, slot) != 0;
    }
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, slot);

    if (opcode == BRFALSE || opcode == BRFALSE_S) {
      value = !value;
    }

    return value;
  }

  /**
   * Do a binary comparison of values on the evaluation stack and put the result on the evaluation
   * stack.
   */
  private void binaryCompareAndPutOnTop(int opcode, VirtualFrame frame, int slot1, int slot2) {
    boolean result = binaryCompare(opcode, frame, slot1, slot2);
    loadValueOnTop(frame, slot1, result ? 1 : 0);
  }

  /**
   * Do a binary comparison of values on the evaluation stack and return the result as a boolean.
   *
   * <p>Possible operands: - int32 -> maps to Java int; - int64 -> maps to Java long; - native int
   * -> unsupported; - float (internal representation that can be implementation-dependent) -> maps
   * to Java double; - object reference -> maps to Java Object; - managed pointer -> unsupported
   *
   * <p>Possible combinations: - int32, int32; - int64, int64; - float, float; - object reference,
   * object reference (only for beq[.s], bne.un[.s], ceq)
   *
   * @return the comparison result as a boolean
   */
  private boolean binaryCompare(int opcode, VirtualFrame frame, int slot1, int slot2) {
    assert slot1 < slot2;
    var slot1Type = CILOSTAZOLFrame.popTaggedStack(taggedFrame, slot1).getStackTypeKind();
    var slot2Type = CILOSTAZOLFrame.popTaggedStack(taggedFrame, slot2).getStackTypeKind();

    if (slot1Type == CILOSTAZOLFrame.StackType.Int && slot2Type == CILOSTAZOLFrame.StackType.Int) {
      long op1 = CILOSTAZOLFrame.popInt(frame, slot1);
      long op2 = CILOSTAZOLFrame.popInt(frame, slot2);
      return binaryCompareInt32(opcode, op1, op2);
    }

    if (slot1Type == CILOSTAZOLFrame.StackType.Long
        && slot2Type == CILOSTAZOLFrame.StackType.Long) {
      long op1 = CILOSTAZOLFrame.popLong(frame, slot1);
      long op2 = CILOSTAZOLFrame.popLong(frame, slot2);
      return binaryCompareInt64(opcode, op1, op2);
    }

    if (slot1Type == CILOSTAZOLFrame.StackType.Double
        && slot2Type == CILOSTAZOLFrame.StackType.Double) {
      double op1 = CILOSTAZOLFrame.popDouble(frame, slot1);
      double op2 = CILOSTAZOLFrame.popDouble(frame, slot2);
      return binaryCompareDouble(opcode, op1, op2);
    }

    if (slot1Type == CILOSTAZOLFrame.StackType.Object
        && slot2Type == CILOSTAZOLFrame.StackType.Object) {
      var op1 = CILOSTAZOLFrame.popObject(frame, slot1);
      var op2 = CILOSTAZOLFrame.popObject(frame, slot2);
      return binaryCompareByReference(opcode, frame, op1, op2);
    }

    CompilerDirectives.transferToInterpreterAndInvalidate();
    throw new InterpreterException("Invalid types for comparison: " + slot1Type + " " + slot2Type);
  }

  private boolean binaryCompareByReference(int opcode, VirtualFrame frame, Object op1, Object op2) {
    switch (opcode) {
      case CEQ:
      case BEQ:
      case BEQ_S:
        return op1 == op2;

      case BNE_UN:
      case BNE_UN_S:
        return op1 != op2;
    }

    CompilerDirectives.transferToInterpreterAndInvalidate();
    throw new InterpreterException("Unimplemented opcode for reference comparison: " + opcode);
  }

  private boolean binaryCompareInt32(int opcode, long op1, long op2) {
    switch (opcode) {
      case CGT:
      case BGT:
      case BGT_S:
      case BGE:
      case BGE_S:
      case CLT:
      case BLT:
      case BLT_S:
      case BLE:
      case BLE_S:
      case CEQ:
      case BEQ:
      case BEQ_S:
        op1 = TypeHelpers.signExtend32(op1);
        op2 = TypeHelpers.signExtend32(op2);
        break;
      case CGT_UN:
      case BGT_UN:
      case BGT_UN_S:
      case BGE_UN:
      case BGE_UN_S:
      case CLT_UN:
      case BLT_UN:
      case BLT_UN_S:
      case BLE_UN:
      case BLE_UN_S:
      case BNE_UN:
      case BNE_UN_S:
        op1 = TypeHelpers.zeroExtend32(op1);
        op2 = TypeHelpers.zeroExtend32(op2);
        break;
      default:
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new InterpreterException("Unimplemented opcode for int32 comparison: " + opcode);
    }

    return binaryCompareInt64(opcode, op1, op2);
  }

  private boolean binaryCompareInt64(int opcode, long op1, long op2) {
    boolean result;
    switch (opcode) {
      case CGT:
      case BGT:
      case BGT_S:
        result = op1 > op2;
        break;

      case BGE:
      case BGE_S:
        result = op1 >= op2;
        break;

      case CLT:
      case BLT:
      case BLT_S:
        result = op1 < op2;
        break;
      case BLE:
      case BLE_S:
        result = op1 <= op2;
        break;

      case CEQ:
      case BEQ:
      case BEQ_S:
        result = op1 == op2;
        break;

      case CGT_UN:
      case BGT_UN:
      case BGT_UN_S:
        result = Long.compareUnsigned(op1, op2) > 0;
        break;

      case BGE_UN:
      case BGE_UN_S:
        result = Long.compareUnsigned(op1, op2) >= 0;
        break;

      case CLT_UN:
      case BLT_UN:
      case BLT_UN_S:
        result = Long.compareUnsigned(op1, op2) < 0;
        break;
      case BLE_UN:
      case BLE_UN_S:
        result = Long.compareUnsigned(op1, op2) <= 0;
        break;

      case BNE_UN:
      case BNE_UN_S:
        result = op1 != op2;
        break;
      default:
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new InterpreterException("Unimplemented opcode for int64 comparison: " + opcode);
    }

    return result;
  }

  private boolean binaryCompareDouble(int opcode, double op1, double op2) {
    final boolean isUnordered = Double.isNaN(op1) || Double.isNaN(op2);
    boolean result;
    switch (opcode) {
      case CGT:
      case BGT:
      case BGT_S:
        if (isUnordered) {
          return false;
        }
        result = op1 > op2;
        break;

      case CGT_UN:
      case BGT_UN:
      case BGT_UN_S:
        if (isUnordered) {
          return true;
        }
        result = op1 > op2;
        break;

      case BGE:
      case BGE_S:
        if (isUnordered) {
          return false;
        }
        result = op1 >= op2;
        break;

      case BGE_UN:
      case BGE_UN_S:
        if (isUnordered) {
          return true;
        }
        result = op1 >= op2;
        break;

      case CLT:
      case BLT:
      case BLT_S:
        if (isUnordered) {
          return false;
        }
        result = op1 < op2;
        break;

      case CLT_UN:
      case BLT_UN:
      case BLT_UN_S:
        if (isUnordered) {
          return true;
        }
        result = op1 < op2;
        break;

      case BLE:
      case BLE_S:
        if (isUnordered) {
          return false;
        }
        result = op1 <= op2;
        break;

      case BLE_UN:
      case BLE_UN_S:
        if (isUnordered) {
          return true;
        }
        result = op1 <= op2;
        break;

      case CEQ:
      case BEQ:
      case BEQ_S:
        if (isUnordered) {
          return false;
        }

        result = op1 == op2;
        break;

      case BNE_UN:
      case BNE_UN_S:
        if (isUnordered) {
          return true;
        }
        result = op1 != op2;
        break;
      default:
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new InterpreterException("Unimplemented opcode for double comparison: " + opcode);
    }

    return result;
  }
  // endregion
}
