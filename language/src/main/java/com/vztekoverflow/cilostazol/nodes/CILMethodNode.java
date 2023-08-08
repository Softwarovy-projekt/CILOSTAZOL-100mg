package com.vztekoverflow.cilostazol.nodes;

import static com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions.*;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.HostCompilerDirectives;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.BytecodeOSRNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.staticobject.StaticProperty;
import com.vztekoverflow.cil.parser.bytecode.BytecodeBuffer;
import com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.CLIUSHeapPtr;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.exceptions.NotImplementedException;
import com.vztekoverflow.cilostazol.nodes.nodeized.CALLNode;
import com.vztekoverflow.cilostazol.nodes.nodeized.NEWOBJNode;
import com.vztekoverflow.cilostazol.nodes.nodeized.NodeizedNodeBase;
import com.vztekoverflow.cilostazol.nodes.nodeized.PRINTNode;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticField;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import com.vztekoverflow.cilostazol.runtime.symbols.*;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol.MethodFlags.Flag;
import com.vztekoverflow.cilostazol.staticanalysis.StaticOpCodeAnalyser;
import java.lang.reflect.Array;
import java.util.Arrays;

public class CILMethodNode extends CILNodeBase implements BytecodeOSRNode {
  private final MethodSymbol method;
  private final byte[] cil;
  private final BytecodeBuffer bytecodeBuffer;
  private final FrameDescriptor frameDescriptor;

  @Children private NodeizedNodeBase[] nodes = new NodeizedNodeBase[0];

  private CILMethodNode(MethodSymbol method) {
    this.method = method;
    cil = method.getCIL();
    frameDescriptor =
        CILOSTAZOLFrame.create(
            method.getParameterCountIncludingInstance(),
            method.getLocals().length,
            method.getMaxStack());
    this.bytecodeBuffer = new BytecodeBuffer(cil);
  }

  public static CILMethodNode create(MethodSymbol method) {
    return new CILMethodNode(method);
  }

  public MethodSymbol getMethod() {
    return method;
  }

  public FrameDescriptor getFrameDescriptor() {
    return frameDescriptor;
  }
  // endregion

  // region CILNodeBase
  @Override
  public Object execute(VirtualFrame frame) {
    initializeFrame(frame);
    return execute(frame, 0, CILOSTAZOLFrame.getStartStackOffset(method));
  }

  // region OSR
  @Override
  public Object executeOSR(VirtualFrame osrFrame, int target, Object interpreterState) {
    throw new NotImplementedException();
  }

  @Override
  public Object getOSRMetadata() {
    throw new NotImplementedException();
  }
  // endregion

  @Override
  public void setOSRMetadata(Object osrMetadata) {
    throw new NotImplementedException();
  }

  private void initializeFrame(VirtualFrame frame) {
    // Init arguments
    Object[] args = frame.getArguments();
    TypeSymbol[] argTypes = getMethod().getParameterTypesIncludingInstance();
    int argsOffset = CILOSTAZOLFrame.getStartArgsOffset(getMethod());

    for (int i = 0; i < args.length; i++) {
      CILOSTAZOLFrame.put(frame, args[i], argsOffset + i, argTypes[i]);
    }
  }

  @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.MERGE_EXPLODE)
  @HostCompilerDirectives.BytecodeInterpreterSwitch
  private Object execute(VirtualFrame frame, int pc, int topStack) {

    while (true) {
      int curOpcode = bytecodeBuffer.getOpcode(pc);
      int nextpc = bytecodeBuffer.nextInstruction(pc);
      switch (curOpcode) {
        case NOP:
          break;
        case POP:
          pop(frame, topStack, getMethod().getOpCodeTypes()[pc]);
          break;
        case DUP:
          duplicateSlot(frame, topStack - 1);
          break;

          // Loading on top of the stack
        case LDNULL:
          CILOSTAZOLFrame.putObject(frame, topStack, StaticObject.NULL);
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
          CILOSTAZOLFrame.putInt32(frame, topStack, curOpcode - LDC_I4_0);
          break;
        case LDC_I4_S:
          CILOSTAZOLFrame.putInt32(frame, topStack, bytecodeBuffer.getImmByte(pc));
          break;
        case LDC_I4:
          CILOSTAZOLFrame.putInt32(frame, topStack, bytecodeBuffer.getImmInt(pc));
          break;
        case LDC_I8:
          CILOSTAZOLFrame.putInt64(frame, topStack, bytecodeBuffer.getImmLong(pc));
          break;
        case LDC_R4:
          CILOSTAZOLFrame.putNativeFloat(
              frame, topStack, Float.intBitsToFloat(bytecodeBuffer.getImmInt(pc)));
          break;
        case LDC_R8:
          CILOSTAZOLFrame.putNativeFloat(
              frame, topStack, Double.longBitsToDouble(bytecodeBuffer.getImmLong(pc)));
          break;
        case LDSTR:
          loadString(frame, topStack, bytecodeBuffer.getImmToken(pc));
          break;

          // Storing to locals
        case STLOC_0:
        case STLOC_1:
        case STLOC_2:
        case STLOC_3:
          CILOSTAZOLFrame.copyStatic(
              frame,
              topStack - 1,
              curOpcode - STLOC_0 + CILOSTAZOLFrame.isInstantiable(getMethod()));
          break;
        case STLOC_S:
          CILOSTAZOLFrame.copyStatic(
              frame,
              topStack - 1,
              bytecodeBuffer.getImmUByte(pc) + CILOSTAZOLFrame.isInstantiable(getMethod()));
          break;

          // Loading locals to top
        case LDLOC_0:
        case LDLOC_1:
        case LDLOC_2:
        case LDLOC_3:
          CILOSTAZOLFrame.copyStatic(
              frame, curOpcode - LDLOC_0 + CILOSTAZOLFrame.isInstantiable(getMethod()), topStack);
          break;
        case LDLOC_S:
          CILOSTAZOLFrame.copyStatic(
              frame,
              bytecodeBuffer.getImmUByte(pc) + CILOSTAZOLFrame.isInstantiable(getMethod()),
              topStack);
          break;
        case LDLOCA_S:
          CILOSTAZOLFrame.putObject(
              frame,
              topStack,
              getMethod()
                  .getContext()
                  .getAllocator()
                  .createStackReference(
                      SymbolResolver.resolveReference(
                          ReferenceSymbol.ReferenceType.Local, getMethod().getContext()),
                      frame,
                      bytecodeBuffer.getImmUByte(pc)
                          + CILOSTAZOLFrame.isInstantiable(getMethod())));
          break;

          // Loading args to top
        case LDARG_0:
        case LDARG_1:
        case LDARG_2:
        case LDARG_3:
          CILOSTAZOLFrame.copyStatic(
              frame,
              CILOSTAZOLFrame.getStartArgsOffset(getMethod()) + curOpcode - LDARG_0,
              topStack);
          break;
        case LDARG_S:
          CILOSTAZOLFrame.copyStatic(
              frame,
              CILOSTAZOLFrame.getStartArgsOffset(getMethod()) + bytecodeBuffer.getImmUByte(pc),
              topStack);
          break;
        case LDARGA_S:
          CILOSTAZOLFrame.putObject(
              frame,
              topStack,
              getMethod()
                  .getContext()
                  .getAllocator()
                  .createStackReference(
                      SymbolResolver.resolveReference(
                          ReferenceSymbol.ReferenceType.Argument, getMethod().getContext()),
                      frame,
                      bytecodeBuffer.getImmUByte(pc)
                          + CILOSTAZOLFrame.getStartArgsOffset(getMethod())));
          break;

          // Loading fields
        case LDFLD:
          loadInstanceField(
              frame, topStack, bytecodeBuffer.getImmToken(pc), getMethod().getOpCodeTypes()[pc]);
          break;
        case LDSFLD:
          loadStaticField(frame, topStack, bytecodeBuffer.getImmToken(pc));
          break;
        case LDFLDA:
          loadFieldInstanceFieldRef(frame, topStack, bytecodeBuffer.getImmToken(pc));
          break;
        case LDSFLDA:
          loadFieldStaticFieldRef(frame, topStack, bytecodeBuffer.getImmToken(pc));
          break;

          // Storing fields
        case STFLD:
          storeInstanceField(frame, topStack, bytecodeBuffer.getImmToken(pc));
          break;
        case STSFLD:
          storeStaticField(frame, topStack, bytecodeBuffer.getImmToken(pc));
          break;

          // Object manipulation
        case LDOBJ:
          copyObject(
              frame,
              bytecodeBuffer.getImmToken(pc),
              getSlotFromReference(frame, topStack - 1),
              topStack - 1);
          break;
        case STOBJ:
          copyObject(
              frame,
              bytecodeBuffer.getImmToken(pc),
              topStack - 1,
              getSlotFromReference(frame, topStack - 2));
          break;

        case INITOBJ:
          initializeObject(frame, topStack, bytecodeBuffer.getImmToken(pc));
          break;
        case NEWOBJ:
          topStack = nodeizeOpToken(frame, topStack, bytecodeBuffer.getImmToken(pc), pc, curOpcode);
          break;
        case CPOBJ:
          copyObjectIndirectly(frame, topStack - 2, topStack - 1);
          break;
        case ISINST:
          checkIsInstance(frame, topStack - 1, bytecodeBuffer.getImmToken(pc));
          break;
        case CASTCLASS:
          castClass(frame, topStack - 1, bytecodeBuffer.getImmToken(pc));
          break;
        case BOX:
          box(frame, topStack - 1, bytecodeBuffer.getImmToken(pc));
          break;
        case UNBOX:
          unbox(frame, topStack - 1, bytecodeBuffer.getImmToken(pc));
          break;
        case UNBOX_ANY:
          unboxAny(frame, topStack - 1, bytecodeBuffer.getImmToken(pc));
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
          if (binaryCompare(
              curOpcode, frame, topStack - 2, topStack - 1, getMethod().getOpCodeTypes()[pc])) {
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
          if (binaryCompare(
              curOpcode, frame, topStack - 2, topStack - 1, getMethod().getOpCodeTypes()[pc])) {
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
          if (shouldBranch(curOpcode, frame, topStack - 1, getMethod().getOpCodeTypes()[pc])) {
            // TODO: OSR support
            pc = nextpc + bytecodeBuffer.getImmInt(pc);
            topStack += BytecodeInstructions.getStackEffect(curOpcode);
            continue;
          }
          break;

        case BRTRUE_S:
        case BRFALSE_S:
          if (shouldBranch(curOpcode, frame, topStack - 1, getMethod().getOpCodeTypes()[pc])) {
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
          binaryCompareAndPutOnTop(
              curOpcode, frame, topStack - 2, topStack - 1, getMethod().getOpCodeTypes()[pc]);
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
          var methodToken = bytecodeBuffer.getImmToken(pc);
          topStack = nodeizeOpToken(frame, topStack, methodToken, pc, CALL);
          break;

          // Store indirect
        case STIND_I1:
          storeIndirectByte(frame, topStack);
          break;
        case STIND_I2:
          storeIndirectShort(frame, topStack);
          break;
        case STIND_I4:
          storeIndirectInt(frame, topStack);
          break;
        case STIND_I8:
          storeIndirectLong(frame, topStack);
          break;
        case STIND_I:
          storeIndirectNative(frame, topStack);
          break;
        case STIND_R4:
          storeIndirectFloat(frame, topStack);
          break;
        case STIND_R8:
          storeIndirectDouble(frame, topStack);
          break;
        case STIND_REF:
          storeIndirectRef(frame, topStack);
          break;

          // Load indirect
        case LDIND_I1:
        case LDIND_U1:
          loadIndirectByte(frame, topStack);
          break;
        case LDIND_I2:
        case LDIND_U2:
          loadIndirectShort(frame, topStack);
          break;
        case LDIND_I4:
        case LDIND_U4:
          loadIndirectInt(frame, topStack);
          break;
        case LDIND_I8:
          loadIndirectLong(frame, topStack);
          break;
        case LDIND_I:
          loadIndirectNative(frame, topStack);
          break;
        case LDIND_R4:
          loadIndirectFloat(frame, topStack);
          break;
        case LDIND_R8:
          loadIndirectDouble(frame, topStack);
          break;
        case LDIND_REF:
          loadIndirectRef(frame, topStack);
          break;

          // array
        case NEWARR:
          createArray(frame, bytecodeBuffer.getImmToken(pc), topStack - 1);
          break;
        case LDLEN:
          getArrayLength(frame, topStack - 1);
          break;

        case LDELEM:
          CILOSTAZOLFrame.putObject(
              frame,
              topStack - 2,
              (StaticObject) ((StaticObject) getJavaArrElem(frame, topStack - 1)).clone());
          break;
        case LDELEM_REF:
          CILOSTAZOLFrame.putObject(
              frame, topStack - 2, (StaticObject) getJavaArrElem(frame, topStack - 1));
          break;
        case LDELEM_I, LDELEM_U4, LDELEM_I4:
          CILOSTAZOLFrame.putInt32(frame, topStack - 2, (int) getJavaArrElem(frame, topStack - 1));
          break;
        case LDELEM_I1, LDELEM_U1:
          CILOSTAZOLFrame.putInt32(frame, topStack - 2, (byte) getJavaArrElem(frame, topStack - 1));
          break;
        case LDELEM_I2, LDELEM_U2:
          CILOSTAZOLFrame.putInt32(
              frame, topStack - 2, (short) getJavaArrElem(frame, topStack - 1));
          break;
        case LDELEM_I8:
          CILOSTAZOLFrame.putInt64(frame, topStack - 2, (long) getJavaArrElem(frame, topStack - 1));
          break;
        case LDELEM_R4:
          CILOSTAZOLFrame.putNativeFloat(
              frame, topStack - 2, (float) getJavaArrElem(frame, topStack - 1));
          break;
        case LDELEM_R8:
          CILOSTAZOLFrame.putNativeFloat(
              frame, topStack - 2, (double) getJavaArrElem(frame, topStack - 1));
          break;
        case LDELEMA:
          CILOSTAZOLFrame.putObject(
              frame,
              topStack - 2,
              getMethod()
                  .getContext()
                  .getAllocator()
                  .createArrayElementReference(
                      SymbolResolver.resolveReference(
                          ReferenceSymbol.ReferenceType.ArrayElement, getMethod().getContext()),
                      CILOSTAZOLFrame.popObject(frame, topStack - 2),
                      CILOSTAZOLFrame.popInt32(frame, topStack - 1)));
          break;

        case STELEM:
          Array.set(
              getMethod()
                  .getContext()
                  .getArrayProperty()
                  .getObject(CILOSTAZOLFrame.popObject(frame, topStack - 3)),
              CILOSTAZOLFrame.popInt32(frame, topStack - 2),
              CILOSTAZOLFrame.popObject(frame, topStack - 1).clone());
          break;
        case STELEM_REF:
          Array.set(
              getMethod()
                  .getContext()
                  .getArrayProperty()
                  .getObject(CILOSTAZOLFrame.popObject(frame, topStack - 3)),
              CILOSTAZOLFrame.popInt32(frame, topStack - 2),
              CILOSTAZOLFrame.popObject(frame, topStack - 1));
          break;
        case STELEM_I, STELEM_I4:
          Array.set(
              getMethod()
                  .getContext()
                  .getArrayProperty()
                  .getObject(CILOSTAZOLFrame.popObject(frame, topStack - 3)),
              CILOSTAZOLFrame.popInt32(frame, topStack - 2),
              CILOSTAZOLFrame.popInt32(frame, topStack - 1));
          break;
        case STELEM_I1:
          Array.set(
              getMethod()
                  .getContext()
                  .getArrayProperty()
                  .getObject(CILOSTAZOLFrame.popObject(frame, topStack - 3)),
              CILOSTAZOLFrame.popInt32(frame, topStack - 2),
              (byte) CILOSTAZOLFrame.popInt32(frame, topStack - 1));
          break;
        case STELEM_I8:
          Array.set(
              getMethod()
                  .getContext()
                  .getArrayProperty()
                  .getObject(CILOSTAZOLFrame.popObject(frame, topStack - 3)),
              CILOSTAZOLFrame.popInt32(frame, topStack - 2),
              CILOSTAZOLFrame.popInt64(frame, topStack - 1));
          break;
        case STELEM_R4:
          Array.set(
              getMethod()
                  .getContext()
                  .getArrayProperty()
                  .getObject(CILOSTAZOLFrame.popObject(frame, topStack - 3)),
              CILOSTAZOLFrame.popInt32(frame, topStack - 2),
              (float) CILOSTAZOLFrame.popNativeFloat(frame, topStack - 1));
          break;
        case STELEM_R8:
          Array.set(
              getMethod()
                  .getContext()
                  .getArrayProperty()
                  .getObject(CILOSTAZOLFrame.popObject(frame, topStack - 3)),
              CILOSTAZOLFrame.popInt32(frame, topStack - 2),
              CILOSTAZOLFrame.popNativeFloat(frame, topStack - 1));
          break;

          // Conversion
        case CONV_I:
        case CONV_I1:
        case CONV_I2:
        case CONV_I4:
        case CONV_I8:
          convertFromSignedToInteger(
              curOpcode,
              frame,
              topStack - 1,
              getIntegerValueForConversion(
                  frame, topStack - 1, getMethod().getOpCodeTypes()[pc], true));
          break;
        case CONV_U:
        case CONV_U1:
        case CONV_U2:
        case CONV_U4:
        case CONV_U8:
          convertFromSignedToInteger(
              curOpcode,
              frame,
              topStack - 1,
              getIntegerValueForConversion(
                  frame, topStack - 1, getMethod().getOpCodeTypes()[pc], false));
          break;

        case CONV_R4:
        case CONV_R8:
        case CONV_R_UN:
          convertToFloat(curOpcode, frame, topStack - 1, getMethod().getOpCodeTypes()[pc]);
          break;

        case CONV_OVF_I1:
        case CONV_OVF_I2:
        case CONV_OVF_I4:
        case CONV_OVF_I8:
        case CONV_OVF_I:
          convertFromSignedToIntegerAndCheckOverflow(
              curOpcode,
              frame,
              topStack - 1,
              getIntegerValueForConversion(
                  frame, topStack - 1, getMethod().getOpCodeTypes()[pc], true));
          break;

        case CONV_OVF_U1:
        case CONV_OVF_U2:
        case CONV_OVF_U4:
        case CONV_OVF_U8:
        case CONV_OVF_U:
        case CONV_OVF_I1_UN:
        case CONV_OVF_I2_UN:
        case CONV_OVF_I4_UN:
        case CONV_OVF_I8_UN:
        case CONV_OVF_I_UN:
        case CONV_OVF_U1_UN:
        case CONV_OVF_U2_UN:
        case CONV_OVF_U4_UN:
        case CONV_OVF_U8_UN:
        case CONV_OVF_U_UN:
          convertFromSignedToIntegerAndCheckOverflow(
              curOpcode,
              frame,
              topStack - 1,
              getIntegerValueForConversion(
                  frame, topStack - 1, getMethod().getOpCodeTypes()[pc], false));
          break;

          //  arithmetics
        case ADD:
        case DIV:
        case MUL:
        case REM:
        case SUB:
          doNumericBinary(
              frame, topStack, curOpcode, getMethod().getOpCodeTypes()[pc], false, false);
          break;

        case OR:
        case AND:
        case XOR:
          doIntegerBinary(frame, topStack, curOpcode, getMethod().getOpCodeTypes()[pc]);
          break;

        case NEG:
          doNeg(frame, topStack, getMethod().getOpCodeTypes()[pc]);
          break;
        case NOT:
          doNot(frame, topStack, getMethod().getOpCodeTypes()[pc]);
          break;

        case SHL:
        case SHR:
        case SHR_UN:
          doShiftBinary(frame, topStack, curOpcode, getMethod().getOpCodeTypes()[pc]);
          break;

        case ADD_OVF:
        case MUL_OVF:
        case SUB_OVF:
          doNumericBinary(
              frame, topStack, curOpcode, getMethod().getOpCodeTypes()[pc], true, false);
          break;
        case ADD_OVF_UN:
        case SUB_OVF_UN:
        case MUL_OVF_UN:
          doNumericBinary(frame, topStack, curOpcode, getMethod().getOpCodeTypes()[pc], true, true);
          break;

        case DIV_UN:
        case REM_UN:
          doNumericBinary(
              frame, topStack, curOpcode, getMethod().getOpCodeTypes()[pc], false, true);
          break;

        case TRUFFLE_NODE:
          topStack = nodes[bytecodeBuffer.getImmInt(pc)].execute(frame);
          break;

        default:
          System.out.println("Opcode not implemented: " + curOpcode);
          break;
      }

      topStack += BytecodeInstructions.getStackEffect(curOpcode);
      pc = nextpc;
    }
  }

  // region arithmetics
  private int doNumericBinary(int op1, int op2, int opcode, boolean ovfCheck, boolean unsigned) {
    return switch (opcode) {
      case ADD:
        if (ovfCheck) yield Math.addExact(op1, op2);
        else yield op1 + op2;
      case SUB:
        yield op1 - op2;
      case MUL:
        if (ovfCheck) yield Math.multiplyExact(op1, op2);
        else yield op1 * op2;
      case DIV:
        if (unsigned) yield Integer.divideUnsigned(op1, op2);
        else yield op1 / op2;
      case REM:
        if (unsigned) yield Integer.remainderUnsigned(op1, op2);
        else yield op1 % op2;
      default:
        throw new InterpreterException();
    };
  }

  private long doNumericBinary(long op1, long op2, int opcode, boolean ovfCheck, boolean unsigned) {
    return switch (opcode) {
      case ADD:
        if (ovfCheck) yield Math.addExact(op1, op2);
        else yield op1 + op2;
      case SUB:
        yield op1 - op2;
      case MUL:
        if (ovfCheck) yield Math.multiplyExact(op1, op2);
        else yield op1 * op2;
      case DIV:
        if (unsigned) yield Long.divideUnsigned(op1, op2);
        else yield op1 / op2;
      case REM:
        if (unsigned) yield Long.divideUnsigned(op1, op2);
        else yield op1 % op2;
      default:
        throw new InterpreterException();
    };
  }

  private double doNumericBinary(double op1, double op2, int opcode, boolean ovfCheck) {
    return switch (opcode) {
      case ADD:
        var result = op1 + op2;
        if (ovfCheck && Double.isInfinite(result)) throw new ArithmeticException();
        yield result;
      case DIV:
        yield op1 / op2;
      case MUL:
        result = op1 * op2;
        if (ovfCheck && Double.isInfinite(result)) throw new ArithmeticException();
        yield result;
      case REM:
        yield op1 % op2;
      case SUB:
        yield op1 - op2;
      default:
        throw new InterpreterException();
    };
  }

  private int doIntegerBinary(int op1, int op2, int opcode) {
    return switch (opcode) {
      case AND:
        yield op1 & op2;
      case OR:
        yield op1 | op2;
      case XOR:
        yield op1 ^ op2;
      default:
        throw new InterpreterException();
    };
  }

  private long doIntegerBinary(long op1, long op2, int opcode) {
    return switch (opcode) {
      case AND:
        yield op1 & op2;
      case OR:
        yield op1 | op2;
      case XOR:
        yield op1 ^ op2;
      default:
        throw new InterpreterException();
    };
  }

  private void doIntegerBinary(
      VirtualFrame frame, int top, int opcode, StaticOpCodeAnalyser.OpCodeType type) {
    switch (type) {
      case Int32 -> {
        final var op1 = CILOSTAZOLFrame.popInt32(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt32(frame, top - 2);
        CILOSTAZOLFrame.putInt32(frame, top - 2, doIntegerBinary(op1, op2, opcode));
      }
      case Int64 -> {
        final var op1 = CILOSTAZOLFrame.popInt64(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt64(frame, top - 2);
        CILOSTAZOLFrame.putInt64(frame, top - 2, doIntegerBinary(op1, op2, opcode));
      }
      case NativeInt -> {
        final var op1 = CILOSTAZOLFrame.popNativeInt(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popNativeInt(frame, top - 2);
        CILOSTAZOLFrame.putNativeInt(frame, top - 2, doIntegerBinary(op1, op2, opcode));
      }
      case Int32_NativeInt -> {
        final var op1 = CILOSTAZOLFrame.popInt32(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popNativeInt(frame, top - 2);
        CILOSTAZOLFrame.putNativeInt(frame, top - 2, doIntegerBinary(op1, op2, opcode));
      }
      case NativeInt_Int32 -> {
        final var op1 = CILOSTAZOLFrame.popNativeInt(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt32(frame, top - 2);
        CILOSTAZOLFrame.putNativeInt(frame, top - 2, doIntegerBinary(op1, op1, opcode));
      }
      default -> throw new InterpreterException();
    }
  }

  private void doNumericBinary(
      VirtualFrame frame,
      int top,
      int opcode,
      StaticOpCodeAnalyser.OpCodeType type,
      boolean ovfCheck,
      boolean unsigned) {
    switch (type) {
      case Int32 -> {
        final var op1 = CILOSTAZOLFrame.popInt32(frame, top - 2);
        final var op2 = CILOSTAZOLFrame.popInt32(frame, top - 1);
        CILOSTAZOLFrame.putInt32(
            frame, top - 2, doNumericBinary(op1, op2, opcode, ovfCheck, unsigned));
      }
      case Int64 -> {
        final var op1 = CILOSTAZOLFrame.popInt64(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt64(frame, top - 2);
        CILOSTAZOLFrame.putInt64(
            frame, top - 2, doNumericBinary(op1, op2, opcode, ovfCheck, unsigned));
      }
      case NativeInt -> {
        final var op1 = CILOSTAZOLFrame.popNativeInt(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popNativeInt(frame, top - 2);
        CILOSTAZOLFrame.putNativeInt(
            frame, top - 2, doNumericBinary(op1, op2, opcode, ovfCheck, unsigned));
      }
      case NativeFloat -> {
        if (unsigned) throw new InterpreterException();
        final var op1 = CILOSTAZOLFrame.popNativeFloat(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popNativeFloat(frame, top - 2);
        CILOSTAZOLFrame.putNativeFloat(frame, top - 2, doNumericBinary(op1, op2, opcode, ovfCheck));
      }
      case Int32_NativeInt -> {
        final var op1 = CILOSTAZOLFrame.popInt32(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popNativeInt(frame, top - 2);
        CILOSTAZOLFrame.putNativeInt(
            frame, top - 2, doNumericBinary(op1, op2, opcode, ovfCheck, unsigned));
      }
      case NativeInt_Int32 -> {
        final var op1 = CILOSTAZOLFrame.popNativeInt(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt32(frame, top - 2);
        CILOSTAZOLFrame.putNativeInt(
            frame, top - 2, doNumericBinary(op1, op2, opcode, ovfCheck, unsigned));
      }
      default -> throw new InterpreterException();
    }
  }

  private int doShiftBinary(int value, int amount, int opcode) {
    return switch (opcode) {
      case SHL -> value << amount;
      case SHR -> value >> amount;
      case SHR_UN -> value >>> amount;
      default -> throw new InterpreterException();
    };
  }

  private long doShiftBinary(long value, int amount, int opcode) {
    return switch (opcode) {
      case SHL -> value << amount;
      case SHR -> value >> amount;
      case SHR_UN -> value >>> amount;
      default -> throw new InterpreterException();
    };
  }

  private void doShiftBinary(
      VirtualFrame frame, int top, int opcode, StaticOpCodeAnalyser.OpCodeType type) {
    switch (type) {
      case Int32 -> {
        final var op1 = CILOSTAZOLFrame.popInt32(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt32(frame, top - 2);
        CILOSTAZOLFrame.putInt32(frame, top - 2, doShiftBinary(op2, op1, opcode));
      }
      case NativeInt -> {
        final var op1 = CILOSTAZOLFrame.popInt32(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt64(frame, top - 2);
        CILOSTAZOLFrame.putInt64(frame, top - 2, doShiftBinary(op2, op1, opcode));
      }
      case Int64_Int32 -> {
        final var op1 = CILOSTAZOLFrame.popInt32(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popNativeInt(frame, top - 2);
        CILOSTAZOLFrame.putNativeInt(frame, top - 2, doShiftBinary(op2, op1, opcode));
      }
      case NativeInt_Int32 -> {
        final var op1 = CILOSTAZOLFrame.popNativeInt(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt32(frame, top - 2);
        CILOSTAZOLFrame.putInt32(frame, top - 2, doShiftBinary(op2, op1, opcode));
      }
      case Int32_NativeInt -> {
        final var op1 = CILOSTAZOLFrame.popNativeInt(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popInt64(frame, top - 2);
        CILOSTAZOLFrame.putInt64(frame, top - 2, doShiftBinary(op2, op1, opcode));
      }
      case Int64_NativeInt -> {
        final var op1 = CILOSTAZOLFrame.popNativeInt(frame, top - 1);
        final var op2 = CILOSTAZOLFrame.popNativeInt(frame, top - 2);
        CILOSTAZOLFrame.putNativeInt(frame, top - 2, doShiftBinary(op2, op1, opcode));
      }

      default -> throw new InterpreterException();
    }
  }

  private void doNot(VirtualFrame frame, int top, StaticOpCodeAnalyser.OpCodeType type) {
    switch (type) {
      case Int32 -> CILOSTAZOLFrame.putInt32(
          frame, top - 1, ~CILOSTAZOLFrame.popInt32(frame, top - 1));
      case Int64 -> CILOSTAZOLFrame.putInt64(
          frame, top - 1, ~CILOSTAZOLFrame.popInt64(frame, top - 1));
      case NativeInt -> CILOSTAZOLFrame.putNativeInt(
          frame, top - 1, ~CILOSTAZOLFrame.popNativeInt(frame, top - 1));
      default -> throw new InterpreterException();
    }
  }
  // endregion

  private void doNeg(VirtualFrame frame, int top, StaticOpCodeAnalyser.OpCodeType type) {
    switch (type) {
      case Int32 -> CILOSTAZOLFrame.putInt32(
          frame, top - 1, CILOSTAZOLFrame.popInt32(frame, top - 1));
      case Int64 -> CILOSTAZOLFrame.putInt64(
          frame, top - 1, CILOSTAZOLFrame.popInt64(frame, top - 1));
      case NativeInt -> CILOSTAZOLFrame.putNativeInt(
          frame, top - 1, CILOSTAZOLFrame.popNativeInt(frame, top - 1));
      default -> throw new InterpreterException();
    }
  }

  // region array
  private void createArray(VirtualFrame frame, CLITablePtr token, int top) {
    TypeSymbol elemType =
        SymbolResolver.resolveType(
            token,
            getMethod().getTypeArguments(),
            getMethod().getDefiningType().getTypeArguments(),
            getMethod().getModule());
    int num = CILOSTAZOLFrame.popInt32(frame, top);
    var arrayType = SymbolResolver.resolveArray(elemType, 1, getMethod().getContext());
    var object = getMethod().getContext().getArrayShape().getFactory().create(arrayType);
    Object value =
        switch (elemType.getSystemType()) {
          case Boolean -> new boolean[num];
          case Char -> new char[num];
          case Int -> new int[num];
          case Byte -> new byte[num];
          case Short -> new short[num];
          case Float -> new float[num];
          case Long -> new long[num];
          case Double -> new double[num];
          case Void -> throw new InterpreterException();
          case Object -> {
            var res = new StaticObject[num];
            Arrays.fill(res, StaticObject.NULL);
            yield res;
          }
        };
    getMethod().getContext().getArrayProperty().setObject(object, value);
    CILOSTAZOLFrame.putObject(frame, top, object);
  }

  private void getArrayLength(VirtualFrame frame, int top) {
    StaticObject arr = CILOSTAZOLFrame.popObject(frame, top);
    Object javaArr = getMethod().getContext().getArrayProperty().getObject(arr);
    CILOSTAZOLFrame.putInt32(frame, top, Array.getLength(javaArr));
  }
  // endregion

  private Object getJavaArrElem(VirtualFrame frame, int top) {
    var idx = CILOSTAZOLFrame.popInt32(frame, top);
    var arr = CILOSTAZOLFrame.popObject(frame, top - 1);
    return Array.get(getMethod().getContext().getArrayProperty().getObject(arr), idx);
  }

  // region indirect
  private void loadIndirectByte(VirtualFrame frame, int top) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putInt32(frame, top - 1, CILOSTAZOLFrame.getLocalInt(refFrame, index));
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        CILOSTAZOLFrame.putInt32(frame, top - 1, refField.getByte(refObj));
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        CILOSTAZOLFrame.putInt32(frame, top - 1, (byte) Array.get(javaArr, index));
      }
    }
  }

  private void loadIndirectShort(VirtualFrame frame, int top) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putInt32(frame, top - 1, CILOSTAZOLFrame.getLocalInt(refFrame, index));
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        CILOSTAZOLFrame.putInt32(frame, top - 1, refField.getShort(refObj));
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        CILOSTAZOLFrame.putInt32(frame, top - 1, (short) Array.get(javaArr, index));
      }
    }
  }

  private void loadIndirectInt(VirtualFrame frame, int top) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putInt32(frame, top - 1, CILOSTAZOLFrame.getLocalInt(refFrame, index));
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        CILOSTAZOLFrame.putInt32(frame, top - 1, refField.getInt(refObj));
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        CILOSTAZOLFrame.putInt32(frame, top - 1, (int) Array.get(javaArr, index));
      }
    }
  }

  private void loadIndirectLong(VirtualFrame frame, int top) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putInt64(frame, top - 1, CILOSTAZOLFrame.getLocalLong(frame, index));
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        CILOSTAZOLFrame.putInt64(frame, top - 1, refField.getLong(refObj));
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        CILOSTAZOLFrame.putInt64(frame, top - 1, (long) Array.get(javaArr, index));
      }
    }
  }

  private void loadIndirectFloat(VirtualFrame frame, int top) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putNativeFloat(
            frame, top - 1, CILOSTAZOLFrame.getLocalNativeFloat(refFrame, index));
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        CILOSTAZOLFrame.putNativeFloat(frame, top - 1, refField.getFloat(refObj));
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        CILOSTAZOLFrame.putNativeFloat(frame, top - 1, (float) Array.get(javaArr, index));
      }
    }
  }

  private void loadIndirectDouble(VirtualFrame frame, int top) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putNativeFloat(
            frame, top - 1, CILOSTAZOLFrame.getLocalNativeFloat(refFrame, index));
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        CILOSTAZOLFrame.putNativeFloat(frame, top - 1, refField.getDouble(refObj));
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        CILOSTAZOLFrame.putNativeFloat(frame, top - 1, (double) Array.get(javaArr, index));
      }
    }
  }

  private void loadIndirectRef(VirtualFrame frame, int top) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putObject(frame, top - 1, CILOSTAZOLFrame.getLocalObject(refFrame, index));
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        CILOSTAZOLFrame.putObject(frame, top - 1, (StaticObject) refField.getObject(refObj));
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        CILOSTAZOLFrame.putObject(frame, top - 1, (StaticObject) Array.get(javaArr, index));
      }
    }
  }

  private void loadIndirectNative(VirtualFrame frame, int top) {
    loadIndirectInt(frame, top);
  }

  private void storeIndirectByte(VirtualFrame frame, int top) {
    var value = (byte) CILOSTAZOLFrame.popInt32(frame, top - 1);
    var reference = CILOSTAZOLFrame.popObject(frame, top - 2);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putInt32(refFrame, index, value);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        refField.setByte(refObj, value);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        Array.setByte(javaArr, index, value);
      }
    }
  }

  private void storeIndirectShort(VirtualFrame frame, int top) {
    var value = (short) CILOSTAZOLFrame.popInt32(frame, top - 1);
    var reference = CILOSTAZOLFrame.popObject(frame, top - 2);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putInt32(refFrame, index, value);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        refField.setShort(refObj, value);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        Array.setShort(javaArr, index, value);
      }
    }
  }

  private void storeIndirectInt(VirtualFrame frame, int top) {
    var value = CILOSTAZOLFrame.popInt32(frame, top - 1);
    var reference = CILOSTAZOLFrame.popObject(frame, top - 2);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putInt32(refFrame, index, value);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        refField.setInt(refObj, value);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        Array.setInt(javaArr, index, value);
      }
    }
  }

  private void storeIndirectLong(VirtualFrame frame, int top) {
    var value = CILOSTAZOLFrame.popInt64(frame, top - 1);
    var reference = CILOSTAZOLFrame.popObject(frame, top - 2);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putInt64(refFrame, index, value);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        refField.setLong(refObj, value);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        Array.setLong(javaArr, index, value);
      }
    }
  }

  private void storeIndirectFloat(VirtualFrame frame, int top) {
    var value = (float) CILOSTAZOLFrame.popNativeFloat(frame, top - 1);
    var reference = CILOSTAZOLFrame.popObject(frame, top - 2);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putNativeFloat(refFrame, index, value);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        refField.setFloat(refObj, value);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        Array.setFloat(javaArr, index, value);
      }
    }
  }

  private void storeIndirectDouble(VirtualFrame frame, int top) {
    var value = CILOSTAZOLFrame.popNativeFloat(frame, top - 1);
    var reference = CILOSTAZOLFrame.popObject(frame, top - 2);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putNativeFloat(refFrame, index, value);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        refField.setDouble(refObj, value);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        Array.setDouble(javaArr, index, value);
      }
    }
  }

  private void storeIndirectRef(VirtualFrame frame, int top) {
    var value = CILOSTAZOLFrame.popObject(frame, top - 1);
    var reference = CILOSTAZOLFrame.popObject(frame, top - 2);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index = getMethod().getContext().getStackReferenceIndexProperty().getInt(reference);
        CILOSTAZOLFrame.putObject(refFrame, index, value);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject)
                getMethod().getContext().getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty)
                getMethod().getContext().getFieldReferenceFieldProperty().getObject(reference);
        refField.setObject(refObj, value);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject)
                getMethod()
                    .getContext()
                    .getArrayElementReferenceArrayProperty()
                    .getObject(reference);
        int index = getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = getMethod().getContext().getArrayProperty().getObject(refArr);
        Array.set(javaArr, index, value);
      }
    }
  }
  // endregion

  private void storeIndirectNative(VirtualFrame frame, int top) {
    storeIndirectInt(frame, top);
  }

  // region other helpers
  private void pop(VirtualFrame frame, int top, StaticOpCodeAnalyser.OpCodeType type) {
    switch (type) {
      case Int32 -> CILOSTAZOLFrame.popInt32(frame, top - 1);
      case Int64 -> CILOSTAZOLFrame.popInt64(frame, top - 1);
      case Object -> CILOSTAZOLFrame.popObject(frame, top - 1);
      case ManagedPointer -> CILOSTAZOLFrame.popObject(frame, top - 1);
      case NativeInt -> CILOSTAZOLFrame.popNativeInt(frame, top - 1);
      case NativeFloat -> CILOSTAZOLFrame.popNativeFloat(frame, top - 1);
      default -> throw new InterpreterException();
    }
  }

  private void loadString(VirtualFrame frame, int top, CLITablePtr token) {
    CLIUSHeapPtr ptr = new CLIUSHeapPtr(token.getRowNo());
    String value = ptr.readString(method.getModule().getDefiningFile().getUSHeap());
    CILOSTAZOLFrame.putObject(
        frame, top, getMethod().getContext().getAllocator().createString(value, frame, top));
  }

  private void duplicateSlot(VirtualFrame frame, int top) {
    CILOSTAZOLFrame.copyStatic(frame, top, top + 1);
  }
  // endregion

  private Object getReturnValue(VirtualFrame frame, int top) {
    if (getMethod().hasReturnValue()) {
      TypeSymbol retType = getMethod().getReturnType().getType();
      return CILOSTAZOLFrame.pop(frame, top, retType);
    }
    // return code 0;
    return 0;
  }

  // region object
  private void copyObject(VirtualFrame frame, CLITablePtr typePtr, int sourceSlot, int destSlot) {
    var type = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    CILOSTAZOLFrame.copyStatic(frame, sourceSlot, destSlot);
  }

  private void copyObjectIndirectly(VirtualFrame frame, int sourceIdx, int destIdx) {
    int sourceSlot = CILOSTAZOLFrame.popInt32(frame, sourceIdx);
    int descSlot = CILOSTAZOLFrame.popInt32(frame, destIdx);
    CILOSTAZOLFrame.copyStatic(frame, sourceSlot, descSlot);
  }

  private int getSlotFromReference(VirtualFrame frame, int referenceIdx) {
    return CILOSTAZOLFrame.popInt32(frame, referenceIdx);
  }

  private void checkIsInstance(VirtualFrame frame, int slot, CLITablePtr typePtr) {
    // TODO: The value can be a Nullable<T>, which is handled differently than T
    var targetType = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    var object = CILOSTAZOLFrame.popObject(frame, slot);
    var sourceType = object.getTypeSymbol();
    if (sourceType.isAssignableFrom(targetType)) {
      // Success: put object back on stack with a new type
      CILOSTAZOLFrame.putObject(frame, slot, object);
      // TODO: Decide how to denote the change in type in the static analysis
    } else {
      // Failure: keep the previous type and put null on the stack
      CILOSTAZOLFrame.putObject(frame, slot, StaticObject.NULL);
    }
  }

  private void castClass(VirtualFrame frame, int slot, CLITablePtr typePtr) {
    StaticObject object = CILOSTAZOLFrame.popObject(frame, slot);
    if (object == StaticObject.NULL) {
      CILOSTAZOLFrame.putObject(frame, slot, StaticObject.NULL);
      return;
    }

    // TODO: The value can be a Nullable<T>, which is handled differently than T
    var targetType = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    var sourceType = object.getTypeSymbol();
    if (sourceType.isAssignableFrom(targetType)) {
      // Success: put object back on stack with a new type
      CILOSTAZOLFrame.putObject(frame, slot, object);
    } else {
      // Failure: throw InvalidCastException
      // TODO: Throw a proper exception
      throw new InterpreterException("System.InvalidCastException");
    }
  }

  private void box(VirtualFrame frame, int slot, CLITablePtr typePtr) {
    var type = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    if (!type.isValueType()) return;

    // TODO: Nullable<T> requires special handling
    StaticObject object = getMethod().getContext().getAllocator().box(type, frame, slot);
    CILOSTAZOLFrame.putObject(frame, slot, object);
  }

  private void unbox(VirtualFrame frame, int slot, CLITablePtr typePtr) {
    var type = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    if (!type.isValueType()) return;

    StaticObject valueReference =
        getMethod().getContext().getAllocator().unboxToReference(type, frame, method, slot);
    CILOSTAZOLFrame.putObject(frame, slot, valueReference);
  }

  private void unboxAny(VirtualFrame frame, int slot, CLITablePtr typePtr) {
    var targetType = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    var object = CILOSTAZOLFrame.popObject(frame, slot);
    var sourceType = (NamedTypeSymbol) object.getTypeSymbol();
    if (!sourceType.isValueType()) {
      // Reference types are handled like castclass
      if (object == StaticObject.NULL) {
        CILOSTAZOLFrame.putObject(frame, slot, StaticObject.NULL);
        return;
      }

      // TODO: The value can be a Nullable<T>, which is handled differently than T
      if (sourceType.isAssignableFrom(targetType)) {
        CILOSTAZOLFrame.putObject(frame, slot, object);
      } else {
        // TODO: Throw a proper exception
        throw new InterpreterException("System.InvalidCastException");
      }
    }

    switch (targetType.getSystemType()) {
      case Boolean -> {
        boolean value =
            sourceType
                .getAssignableInstanceField(sourceType.getFields()[0], frame, slot + 1)
                .getBoolean(object);
        CILOSTAZOLFrame.putInt32(frame, slot, value ? 1 : 0);
      }
      case Char -> {
        char value =
            sourceType
                .getAssignableInstanceField(sourceType.getFields()[0], frame, slot + 1)
                .getChar(object);
        CILOSTAZOLFrame.putInt32(frame, slot, value);
      }
      case Byte -> {
        byte value =
            sourceType
                .getAssignableInstanceField(sourceType.getFields()[0], frame, slot + 1)
                .getByte(object);
        CILOSTAZOLFrame.putInt32(frame, slot, value);
      }
      case Int -> {
        int value =
            sourceType
                .getAssignableInstanceField(sourceType.getFields()[0], frame, slot + 1)
                .getInt(object);
        CILOSTAZOLFrame.putInt32(frame, slot, value);
      }
      case Short -> {
        short value =
            sourceType
                .getAssignableInstanceField(sourceType.getFields()[0], frame, slot + 1)
                .getShort(object);
        CILOSTAZOLFrame.putInt32(frame, slot, value);
      }
      case Float -> {
        float value =
            sourceType
                .getAssignableInstanceField(sourceType.getFields()[0], frame, slot + 1)
                .getFloat(object);
        CILOSTAZOLFrame.putNativeFloat(frame, slot, value);
      }
      case Long -> {
        long value =
            sourceType
                .getAssignableInstanceField(sourceType.getFields()[0], frame, slot + 1)
                .getLong(object);
        CILOSTAZOLFrame.putInt64(frame, slot, value);
      }
      case Double -> {
        double value =
            sourceType
                .getAssignableInstanceField(sourceType.getFields()[0], frame, slot + 1)
                .getDouble(object);
        CILOSTAZOLFrame.putNativeFloat(frame, slot, value);
      }
      case Void -> throw new InterpreterException("Cannot unbox void");
      case Object -> // Unboxing a struct -> we don't need to change any values
      CILOSTAZOLFrame.putObject(frame, slot, object);
    }
  }

  private void createNewObjectOnStack(
      VirtualFrame frame, NamedTypeSymbol type, int dest, int topStack) {
    StaticObject object = type.getContext().getAllocator().createNew(type, frame, topStack);
    CILOSTAZOLFrame.setLocalObject(frame, dest, object);
  }

  private void initializeObject(VirtualFrame frame, int top, CLITablePtr typePtr) {
    var type = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    var destReference = CILOSTAZOLFrame.popObject(frame, top - 1);
    assert ((ReferenceSymbol) destReference.getTypeSymbol()).getReferenceType()
        == ReferenceSymbol.ReferenceType.Local;
    int dest = getMethod().getContext().getStackReferenceIndexProperty().getInt(destReference);

    if (type.isValueType()) {
      // Initialize value type
      createNewObjectOnStack(frame, type, dest, top);
      return;
    }

    // Initialize reference type
    CILOSTAZOLFrame.setLocalObject(frame, dest, StaticObject.NULL);
  }

  private void loadFieldInstanceFieldRef(VirtualFrame frame, int top, CLITablePtr fieldPtr) {
    final var classMember = SymbolResolver.resolveField(fieldPtr, method.getModule());
    final var field = classMember.symbol.getAssignableInstanceField(classMember.member, frame, top);
    final var object =
        (StaticObject)
            CILOSTAZOLFrame.popObjectFromPossibleReference(
                frame, top - 1, getMethod().getContext());
    CILOSTAZOLFrame.putObject(
        frame,
        top - 1,
        getMethod()
            .getContext()
            .getAllocator()
            .createFieldReference(
                SymbolResolver.resolveReference(
                    ReferenceSymbol.ReferenceType.Field, getMethod().getContext()),
                object,
                field));
  }

  private void loadFieldStaticFieldRef(VirtualFrame frame, int top, CLITablePtr fieldPtr) {
    final var classMember = SymbolResolver.resolveField(fieldPtr, method.getModule());
    final var field = classMember.symbol.getAssignableStaticField(classMember.member, frame, top);
    final var object = classMember.symbol.getStaticInstance(frame, top);
    CILOSTAZOLFrame.putObject(
        frame,
        top,
        getMethod()
            .getContext()
            .getAllocator()
            .createFieldReference(
                SymbolResolver.resolveReference(
                    ReferenceSymbol.ReferenceType.Field, getMethod().getContext()),
                object,
                field));
  }

  private void loadInstanceField(
      VirtualFrame frame, int top, CLITablePtr fieldPtr, StaticOpCodeAnalyser.OpCodeType type) {
    var classMember = SymbolResolver.resolveField(fieldPtr, method.getModule());
    StaticField field =
        classMember.symbol.getAssignableInstanceField(classMember.member, frame, top);
    StaticObject object =
        (StaticObject)
            CILOSTAZOLFrame.popObjectFromPossibleReference(
                frame, top - 1, getMethod().getContext());
    loadValueFromField(frame, top, field, object);
  }

  private void loadStaticField(VirtualFrame frame, int top, CLITablePtr fieldPtr) {
    var classMember = SymbolResolver.resolveField(fieldPtr, method.getModule());
    StaticField field = classMember.symbol.getAssignableStaticField(classMember.member, frame, top);
    StaticObject object = classMember.symbol.getStaticInstance(frame, top);
    loadValueFromField(frame, top + 1, field, object);
  }

  private void loadValueFromField(
      VirtualFrame frame, int top, StaticField field, StaticObject object) {
    switch (field.getKind()) {
      case Boolean -> {
        boolean value = field.getBoolean(object);
        CILOSTAZOLFrame.putInt32(frame, top - 1, value ? 1 : 0);
      }
      case Char -> {
        char value = field.getChar(object);
        CILOSTAZOLFrame.putInt32(frame, top - 1, value);
      }
      case Short -> {
        short value = field.getShort(object);
        CILOSTAZOLFrame.putInt32(frame, top - 1, value);
      }
      case Float -> {
        float value = field.getFloat(object);
        CILOSTAZOLFrame.putNativeFloat(frame, top - 1, value);
      }
      case Double -> {
        double value = field.getDouble(object);
        CILOSTAZOLFrame.putNativeFloat(frame, top - 1, value);
      }
      case Int -> {
        int value = field.getInt(object);
        CILOSTAZOLFrame.putInt32(frame, top - 1, value);
      }
      case Long -> {
        long value = field.getLong(object);
        CILOSTAZOLFrame.putInt64(frame, top - 1, value);
      }
      default -> {
        StaticObject value = (StaticObject) field.getObject(object);
        CILOSTAZOLFrame.putObject(frame, top - 1, value);
      }
    }
  }

  void storeInstanceField(VirtualFrame frame, int top, CLITablePtr fieldPtr) {
    var classMember = SymbolResolver.resolveField(fieldPtr, method.getModule());
    StaticField field =
        classMember.symbol.getAssignableInstanceField(classMember.member, frame, top);
    StaticObject object =
        (StaticObject)
            CILOSTAZOLFrame.popObjectFromPossibleReference(
                frame, top - 2, getMethod().getContext());
    assignValueToField(frame, top, field, object);
  }

  private void storeStaticField(VirtualFrame frame, int top, CLITablePtr fieldPtr) {
    var classMember = SymbolResolver.resolveField(fieldPtr, method.getModule());
    StaticField field = classMember.symbol.getAssignableStaticField(classMember.member, frame, top);
    StaticObject object = classMember.symbol.getStaticInstance(frame, top);
    assignValueToField(frame, top, field, object);
  }

  private void assignValueToField(
      VirtualFrame frame, int top, StaticField field, StaticObject object) {
    switch (field.getKind()) {
      case Boolean -> {
        int value = CILOSTAZOLFrame.popInt32(frame, top - 1);
        field.setBoolean(object, value != 0);
      }
      case Short -> {
        int value = CILOSTAZOLFrame.popInt32(frame, top - 1);
        field.setShort(object, (short) value);
      }
      case Char -> {
        int value = CILOSTAZOLFrame.popInt32(frame, top - 1);
        field.setChar(object, (char) value);
      }
      case Float -> {
        double value = CILOSTAZOLFrame.popNativeFloat(frame, top - 1);
        field.setFloat(object, (float) value);
      }
      case Double -> {
        double value = CILOSTAZOLFrame.popNativeFloat(frame, top - 1);
        field.setDouble(object, value);
      }
      case Int -> {
        int value = CILOSTAZOLFrame.popInt32(frame, top - 1);
        field.setInt(object, value);
      }
      case Long -> {
        long value = CILOSTAZOLFrame.popInt64(frame, top - 1);
        field.setLong(object, value);
      }
      default -> {
        StaticObject value = CILOSTAZOLFrame.popObject(frame, top - 1);
        field.setObject(object, value);
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
      case NEWOBJ -> {
        var method =
            SymbolResolver.resolveMethod(
                token,
                getMethod().getTypeArguments(),
                getMethod().getDefiningType().getTypeArguments(),
                getMethod().getModule());
        node = new NEWOBJNode(method.member, top);
      }
      case CALL -> {
        var method =
            SymbolResolver.resolveMethod(
                token,
                getMethod().getTypeArguments(),
                getMethod().getDefiningType().getTypeArguments(),
                getMethod().getModule());
        node = getCheckedCALLNode(method.member, top);
      }
      default -> {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new InterpreterException();
      }
    }

    int index = addNode(node);

    byte[] patch =
        preparePatch(
            (byte) TRUFFLE_NODE,
            index,
            com.vztekoverflow.bacil.bytecode.BytecodeInstructions.getLength(opcode));
    bytecodeBuffer.patchBytecode(pc, patch);

    // execute the new node
    return nodes[index].execute(frame);
  }

  private NodeizedNodeBase getCheckedCALLNode(MethodSymbol method, int top) {
    if (method.getMethodFlags().hasFlag(Flag.UNMANAGED_EXPORT)) {
      // Either native support must be supported or some workaround must be implemented
      throw new NotImplementedException();
    }
    if (method.getName().equals("WriteLine")
        && method.getDefiningType().getName().equals("Console")
        && method.getDefiningType().getNamespace().equals("System")) {
      return new PRINTNode(top);
    }

    return new CALLNode(method, top);
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
  private void convertFromSignedToInteger(int opcode, VirtualFrame frame, int top, long value) {
    switch (opcode) {
      case CONV_I1 -> CILOSTAZOLFrame.putInt32(frame, top, TypeHelpers.signExtend8(value));
      case CONV_I2 -> CILOSTAZOLFrame.putInt32(frame, top, TypeHelpers.signExtend16(value));
      case CONV_I4 -> CILOSTAZOLFrame.putInt32(frame, top, (int) TypeHelpers.truncate32(value));
      case CONV_I8, CONV_U8 -> CILOSTAZOLFrame.putInt64(frame, top, value);
      case CONV_U1 -> CILOSTAZOLFrame.putInt32(frame, top, TypeHelpers.zeroExtend8(value));
      case CONV_U2 -> CILOSTAZOLFrame.putInt32(frame, top, TypeHelpers.zeroExtend16(value));
      case CONV_U4 -> CILOSTAZOLFrame.putInt32(
          frame, top, (int) TypeHelpers.zeroExtend32(TypeHelpers.truncate32(value)));
      case CONV_I, CONV_U -> CILOSTAZOLFrame.putNativeInt(
          frame, top, (int) TypeHelpers.truncate32(value));
      default -> {
        CompilerAsserts.neverPartOfCompilation();
        throw new InterpreterException("Invalid opcode for conversion");
      }
    }
  }

  private void convertFromSignedToIntegerAndCheckOverflow(
      int opcode, VirtualFrame frame, int top, long value) {
    switch (opcode) {
      case CONV_OVF_I1, CONV_OVF_I1_UN -> CILOSTAZOLFrame.putInt32(
          frame, top, TypeHelpers.signExtend8Exact(value));
      case CONV_OVF_I2, CONV_OVF_I2_UN -> CILOSTAZOLFrame.putInt32(
          frame, top, TypeHelpers.signExtend16Exact(value));
      case CONV_OVF_I4, CONV_OVF_I4_UN -> CILOSTAZOLFrame.putInt32(
          frame, top, (int) TypeHelpers.truncate32Exact(value));
      case CONV_OVF_I8, CONV_OVF_I8_UN, CONV_OVF_U8, CONV_OVF_U8_UN -> CILOSTAZOLFrame.putInt64(
          frame, top, value);
      case CONV_OVF_U1, CONV_OVF_U1_UN -> CILOSTAZOLFrame.putInt32(
          frame, top, TypeHelpers.zeroExtend8Exact(value));
      case CONV_OVF_U2, CONV_OVF_U2_UN -> CILOSTAZOLFrame.putInt32(
          frame, top, TypeHelpers.zeroExtend16Exact(value));
      case CONV_OVF_U4, CONV_OVF_U4_UN -> CILOSTAZOLFrame.putInt32(
          frame, top, (int) TypeHelpers.zeroExtend32Exact(TypeHelpers.truncate32Exact(value)));
      case CONV_OVF_I, CONV_OVF_I_UN -> CILOSTAZOLFrame.putNativeInt(
          frame, top, (int) TypeHelpers.truncate32Exact(value));
      case CONV_OVF_U, CONV_OVF_U_UN -> CILOSTAZOLFrame.putNativeInt(
          frame, top, (int) TypeHelpers.zeroExtend32Exact(TypeHelpers.truncate32Exact(value)));
      default -> {
        CompilerAsserts.neverPartOfCompilation();
        throw new InterpreterException("Invalid opcode for conversion");
      }
    }
  }

  private long getIntegerValueForConversion(
      VirtualFrame frame, int top, StaticOpCodeAnalyser.OpCodeType type, boolean signed) {
    return switch (type) {
      case Int32 -> signed
          ? TypeHelpers.signExtend32(CILOSTAZOLFrame.popInt32(frame, top))
          : TypeHelpers.zeroExtend32(CILOSTAZOLFrame.popInt32(frame, top));
      case NativeInt -> signed
          ? TypeHelpers.signExtend32(CILOSTAZOLFrame.popNativeInt(frame, top))
          : TypeHelpers.zeroExtend32(CILOSTAZOLFrame.popNativeInt(frame, top));
      case Int64 -> CILOSTAZOLFrame.popInt64(frame, top);
      case NativeFloat -> (long) CILOSTAZOLFrame.popNativeFloat(frame, top);
      default -> throw new InterpreterException("Invalid type for conversion: " + type);
    };
  }

  private void convertToFloat(
      int opcode, VirtualFrame frame, int top, StaticOpCodeAnalyser.OpCodeType type) {
    double value =
        switch (type) {
          case Int32 -> CILOSTAZOLFrame.popInt32(frame, top);
          case Int64 -> CILOSTAZOLFrame.popInt64(frame, top);
          case NativeInt -> CILOSTAZOLFrame.popNativeInt(frame, top);
          case NativeFloat -> CILOSTAZOLFrame.popNativeFloat(frame, top);
          default -> throw new InterpreterException("Invalid type for conversion: " + type);
        };

    switch (opcode) {
      case CONV_R4 -> CILOSTAZOLFrame.putNativeFloat(frame, top, (float) value);
      case CONV_R8 -> CILOSTAZOLFrame.putNativeFloat(frame, top, value);
      default -> {
        CompilerAsserts.neverPartOfCompilation();
        throw new InterpreterException("Invalid opcode for conversion");
      }
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
  private boolean shouldBranch(
      int opcode, VirtualFrame frame, int slot, StaticOpCodeAnalyser.OpCodeType type) {
    boolean value;
    if (type == StaticOpCodeAnalyser.OpCodeType.Object) {
      value = CILOSTAZOLFrame.popObject(frame, slot) != StaticObject.NULL;
    } else {
      value = CILOSTAZOLFrame.popInt32(frame, slot) != 0;
    }

    if (opcode == BRFALSE || opcode == BRFALSE_S) {
      value = !value;
    }

    return value;
  }

  /**
   * Do a binary comparison of values on the evaluation stack and put the result on the evaluation
   * stack.
   */
  private void binaryCompareAndPutOnTop(
      int opcode, VirtualFrame frame, int slot1, int slot2, StaticOpCodeAnalyser.OpCodeType type) {
    boolean result = binaryCompare(opcode, frame, slot1, slot2, type);
    CILOSTAZOLFrame.putInt32(frame, slot1, result ? 1 : 0);
  }

  /**
   * Do a binary comparison of values on the evaluation stack and return the result as a boolean.
   * @return the comparison result as a boolean
   */
  private boolean binaryCompare(
      int opcode, VirtualFrame frame, int slot1, int slot2, StaticOpCodeAnalyser.OpCodeType type) {
    assert slot1 < slot2;

    if (type == StaticOpCodeAnalyser.OpCodeType.Int32
        || type == StaticOpCodeAnalyser.OpCodeType.NativeInt) {
      long op1 = CILOSTAZOLFrame.popInt32(frame, slot1);
      long op2 = CILOSTAZOLFrame.popInt32(frame, slot2);
      return binaryCompareInt32(opcode, op1, op2);
    }

    if (type == StaticOpCodeAnalyser.OpCodeType.Int64) {
      long op1 = CILOSTAZOLFrame.popInt64(frame, slot1);
      long op2 = CILOSTAZOLFrame.popInt64(frame, slot2);
      return binaryCompareInt64(opcode, op1, op2);
    }

    if (type == StaticOpCodeAnalyser.OpCodeType.NativeFloat) {
      double op1 = CILOSTAZOLFrame.popNativeFloat(frame, slot1);
      double op2 = CILOSTAZOLFrame.popNativeFloat(frame, slot2);
      return binaryCompareDouble(opcode, op1, op2);
    }

    if (type == StaticOpCodeAnalyser.OpCodeType.Object) {
      var op1 = CILOSTAZOLFrame.popObject(frame, slot1);
      var op2 = CILOSTAZOLFrame.popObject(frame, slot2);
      return binaryCompareByReference(opcode, frame, op1, op2);
    }

    CompilerDirectives.transferToInterpreterAndInvalidate();
    throw new InterpreterException("Invalid types for comparison: " + type.name());
  }

  private boolean binaryCompareByReference(int opcode, VirtualFrame frame, Object op1, Object op2) {
    switch (opcode) {
      case CEQ:
      case BEQ:
      case BEQ_S:
        return op1 == op2;

      case BNE_UN:
      case BNE_UN_S:
        /*
        cgt.un is allowed and verifiable on ObjectRefs (O). This is commonly used when comparing an
        ObjectRef with null (there is no "compare-not-equal" instruction, which would otherwise be a more
        obvious solution)
         */
      case CGT_UN:
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
