package com.vztekoverflow.cilostazol.nodes;

import static com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions.*;
import static com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants.*;

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
import com.vztekoverflow.cilostazol.nodes.nodeized.NodeizedNodeBase;
import com.vztekoverflow.cilostazol.nodes.nodeized.PRINTNode;
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
    return new CILMethodNode(method); // TODO: not method.createNode()?
  }

  private TypeSymbol[] createTaggedFrame(LocalSymbol[] localSymbols) {
    var taggedFrame = new TypeSymbol[frameDescriptor.getNumberOfSlots()];

    for (int i = 0; i < localSymbols.length; i++) {
      CILOSTAZOLFrame.putTaggedStack(taggedFrame, i, localSymbols[i].getType());
    }

    return taggedFrame;
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

  // region CILNodeBase
  @Override
  public Object execute(VirtualFrame frame) {
    initializeFrame(frame);
    return execute(frame, 0, CILOSTAZOLFrame.getStartStackOffset(method));
  }

  private void initializeFrame(VirtualFrame frame) {
    // Init arguments
    Object[] args = frame.getArguments();

    boolean hasReceiver = !getMethod().getMethodFlags().hasFlag(Flag.STATIC);
    TypeSymbol[] argTypes;

    if (hasReceiver) {
      argTypes = new TypeSymbol[method.getParameterCountIncludingInstance()];
      argTypes[0] = getMethod().getDefiningType();
      for (int i = 0; i < method.getParameters().length; i++) {
        argTypes[i + 1] = method.getParameters()[i].getType();
      }

    } else {
      argTypes =
          Arrays.stream(method.getParameters())
              .map(ParameterSymbol::getType)
              .toArray(TypeSymbol[]::new);
    }

    int argsOffset = CILOSTAZOLFrame.getStartArgsOffset(getMethod());

    for (int i = 0; i < args.length; i++) {
      CILOSTAZOLFrame.put(frame, args[i], argsOffset + i, argTypes[i]);
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
        case LDSFLD:
          loadField(frame, topStack, bytecodeBuffer.getImmToken(pc));
          break;
        case LDFLDA:
        case LDSFLDA:
          // TODO: Requires references to fields
          break;

          // Storing fields
        case STFLD:
        case STSFLD:
          storeField(frame, topStack, bytecodeBuffer.getImmToken(pc));
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
        case BOX:
          box(frame, topStack - 1, bytecodeBuffer.getImmToken(pc));
          break;
        case UNBOX:
          unbox(frame, topStack - 1, bytecodeBuffer.getImmToken(pc));
          break;
        case UNBOX_ANY:
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

  // region Helpers
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

  private Object getJavaArrElem(VirtualFrame frame, int top) {
    var idx = CILOSTAZOLFrame.popInt32(frame, top);
    var arr = CILOSTAZOLFrame.popObject(frame, top - 1);
    return Array.get(getMethod().getContext().getArrayProperty().getObject(arr), idx);
  }

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
        frame, top, getMethod().getContext().getAllocator().createString(value));
  }

  private void loadIndirectByte(VirtualFrame frame, int top) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame =
            (Frame) getMethod().getContext().getStackReferenceFrameProperty().getObject(reference);
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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
        int index =
            getMethod().getContext().getArrayElementReferenceIndexProperty().getInt(reference);
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

  private void storeIndirectNative(VirtualFrame frame, int top) {
    storeIndirectInt(frame, top);
  }

  private void duplicateSlot(VirtualFrame frame, int top) {
    CILOSTAZOLFrame.copyStatic(frame, top, top + 1);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, top + 1, CILOSTAZOLFrame.getTaggedStack(taggedFrame, top));
  }

  private void copyObjectIndirectly(VirtualFrame frame, int sourceIdx, int destIdx) {
    int sourceSlot = CILOSTAZOLFrame.popInt(frame, sourceIdx);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, sourceIdx);
    int descSlot = CILOSTAZOLFrame.popInt(frame, destIdx);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, destIdx);
    CILOSTAZOLFrame.copyStatic(frame, sourceSlot, descSlot);
    CILOSTAZOLFrame.putTaggedStack(
        taggedFrame, descSlot, CILOSTAZOLFrame.getTaggedStack(taggedFrame, sourceSlot));
  }

  private void copyObject(VirtualFrame frame, CLITablePtr typePtr, int sourceSlot, int destSlot) {
    var type = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    CILOSTAZOLFrame.copyStatic(frame, sourceSlot, destSlot);
    CILOSTAZOLFrame.putTaggedStack(taggedFrame, destSlot, type);
  }

  private int getSlotFromReference(VirtualFrame frame, int referenceIdx) {
    var sourceSlot = CILOSTAZOLFrame.popInt(frame, referenceIdx);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, referenceIdx);
    return sourceSlot;
  }

  private void checkIsInstance(VirtualFrame frame, int slot, CLITablePtr typePtr) {
    // TODO: The value can be a Nullable<T>, which is handled differently than T
    var targetType = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    var sourceType = CILOSTAZOLFrame.popTaggedStack(taggedFrame, slot);
    var object = CILOSTAZOLFrame.popObject(frame, slot);
    if (sourceType.isAssignableFrom(targetType)) {
      // Success: put object back on stack with a new type
      CILOSTAZOLFrame.putObject(frame, slot, object);
      CILOSTAZOLFrame.putTaggedStack(taggedFrame, slot, targetType);
    } else {
      // Failure: keep the previous type and put null on the stack
      CILOSTAZOLFrame.putObject(frame, slot, StaticObject.NULL);
      CILOSTAZOLFrame.putTaggedStack(taggedFrame, slot, sourceType);
    }
  }

  private void box(VirtualFrame frame, int slot, CLITablePtr typePtr) {
    var type = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    if (!type.isValueType()) return;

    // TODO: Nullable<T> requires special handling

    // Find a field to store the data in
    // TODO: See how to handle structs with multiple fields
    // TODO: Consider using wrap methods for primitive types like Espresso does
    FieldSymbol matchingField = null;
    for (var field : type.getFields()) {
      if (field.isStatic()) continue;

      var fieldType = field.getType();
      if (fieldType.equals(type)) {
        matchingField = field;
        break;
      }
    }

    if (matchingField == null) {
      throw new InterpreterException("Could not find matching field for value type");
    }

    CILOSTAZOLFrame.popTaggedStack(taggedFrame, slot);
    switch (matchingField.getSystemType()) {
      case Int -> {
        var value = CILOSTAZOLFrame.popInt(frame, slot);
        StaticObject boxedObject = createNewObjectOnStack(frame, type, slot);
        type.getAssignableInstanceField(matchingField).setInt(boxedObject, value);
      }
      case Long -> {
        var value = CILOSTAZOLFrame.popLong(frame, slot);
        StaticObject boxedObject = createNewObjectOnStack(frame, type, slot);
        type.getAssignableInstanceField(matchingField).setLong(boxedObject, value);
      }
      case Float -> {
        var value = CILOSTAZOLFrame.popDouble(frame, slot);
        StaticObject boxedObject = createNewObjectOnStack(frame, type, slot);
        type.getAssignableInstanceField(matchingField).setFloat(boxedObject, (float) value);
      }
      case Double -> {
        var value = CILOSTAZOLFrame.popDouble(frame, slot);
        StaticObject boxedObject = createNewObjectOnStack(frame, type, slot);
        type.getAssignableInstanceField(matchingField).setDouble(boxedObject, value);
      }
      case Short -> {
        var value = CILOSTAZOLFrame.popInt(frame, slot);
        StaticObject boxedObject = createNewObjectOnStack(frame, type, slot);
        type.getAssignableInstanceField(matchingField).setShort(boxedObject, (short) value);
      }
      case Char -> {
        var value = CILOSTAZOLFrame.popInt(frame, slot);
        StaticObject boxedObject = createNewObjectOnStack(frame, type, slot);
        type.getAssignableInstanceField(matchingField).setChar(boxedObject, (char) value);
      }
      case Boolean -> {
        var value = CILOSTAZOLFrame.popInt(frame, slot);
        StaticObject boxedObject = createNewObjectOnStack(frame, type, slot);
        type.getAssignableInstanceField(matchingField).setBoolean(boxedObject, value != 0);
      }
      case Object -> {
        var value = CILOSTAZOLFrame.popObject(frame, slot);
        StaticObject boxedObject = createNewObjectOnStack(frame, type, slot);
        type.getAssignableInstanceField(matchingField).setObject(boxedObject, value);
      }
      default -> throw new InterpreterException("Unsupported field type");
    }
  }

  private void unbox(VirtualFrame frame, int slot, CLITablePtr typePtr) {
    var type = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
  }

  private void initializeObject(VirtualFrame frame, int top, CLITablePtr typePtr) {
    var type = (NamedTypeSymbol) SymbolResolver.resolveType(typePtr, method.getModule());
    int dest = CILOSTAZOLFrame.popInt(frame, top - 1);
    CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);

    if (type.isValueType()) {
      // Initialize value type
      createNewObjectOnStack(frame, type, dest);
      return;
    }

    // Initialize reference type
    CILOSTAZOLFrame.setLocalObject(frame, dest, StaticObject.NULL);
    CILOSTAZOLFrame.setTaggedStack(taggedFrame, dest, type);
  }

  private StaticObject createNewObjectOnStack(VirtualFrame frame, NamedTypeSymbol type, int dest) {
    StaticObject object = type.getContext().getAllocator().createNew(type);
    CILOSTAZOLFrame.setLocalObject(frame, dest, object);
    CILOSTAZOLFrame.setTaggedStack(taggedFrame, dest, type);
    return object;
  }

  private void loadField(VirtualFrame frame, int top, CLITablePtr fieldPtr) {
    var classMember = SymbolResolver.resolveField(fieldPtr, method.getModule());
    StaticField field;
    StaticObject object;
    if (classMember.member.isStatic()) {
      field = classMember.symbol.getAssignableStaticField(classMember.member);
      object = classMember.symbol.getStaticInstance();
      top++;
    } else {
      field = classMember.symbol.getAssignableInstanceField(classMember.member);
      object = CILOSTAZOLFrame.popObjectFromPossibleReference(frame, taggedFrame, method, top - 1);
    }
    switch (field.getKind()) {
      case Boolean -> {
        boolean value = field.getBoolean(object);
        CILOSTAZOLFrame.putInt(frame, top - 1, value ? 1 : 0);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top - 1, SymbolResolver.getBoolean(CILOSTAZOLContext.get(this)));
      }
      case Char -> {
        char value = field.getChar(object);
        CILOSTAZOLFrame.putInt(frame, top - 1, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top - 1, SymbolResolver.getChar(CILOSTAZOLContext.get(this)));
      }
      case Short -> {
        short value = field.getShort(object);
        CILOSTAZOLFrame.putInt(frame, top - 1, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top - 1, SymbolResolver.getInt16(CILOSTAZOLContext.get(this)));
      }
      case Float -> {
        float value = field.getFloat(object);
        CILOSTAZOLFrame.putDouble(frame, top - 1, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top - 1, SymbolResolver.getSingle(CILOSTAZOLContext.get(this)));
      }
      case Double -> {
        double value = field.getDouble(object);
        CILOSTAZOLFrame.putDouble(frame, top - 1, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top - 1, SymbolResolver.getDouble(CILOSTAZOLContext.get(this)));
      }
      case Int -> {
        int value = field.getInt(object);
        CILOSTAZOLFrame.putInt(frame, top - 1, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top - 1, SymbolResolver.getInt32(CILOSTAZOLContext.get(this)));
      }
      case Long -> {
        long value = field.getLong(object);
        CILOSTAZOLFrame.putLong(frame, top - 1, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top - 1, SymbolResolver.getInt64(CILOSTAZOLContext.get(this)));
      }
      default -> {
        StaticObject value = (StaticObject) field.getObject(object);
        CILOSTAZOLFrame.putObject(frame, top - 1, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top - 1, SymbolResolver.getObject(CILOSTAZOLContext.get(this)));
      }
    }
  }

  void storeField(VirtualFrame frame, int top, CLITablePtr fieldPtr) {
    var classMember = SymbolResolver.resolveField(fieldPtr, method.getModule());
    StaticField field;
    StaticObject object;
    if (classMember.member.isStatic()) {
      field = classMember.symbol.getAssignableStaticField(classMember.member);
      object = classMember.symbol.getStaticInstance();
    } else {
      field = classMember.symbol.getAssignableInstanceField(classMember.member);
      object = CILOSTAZOLFrame.popObjectFromPossibleReference(frame, taggedFrame, method, top - 2);
    }
    switch (field.getKind()) {
      case Boolean -> {
        int value = CILOSTAZOLFrame.popInt(frame, top - 1);
        CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);
        field.setBoolean(object, value != 0);
      }
      case Short -> {
        int value = CILOSTAZOLFrame.popInt(frame, top - 1);
        CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);
        field.setShort(object, (short) value);
      }
      case Char -> {
        int value = CILOSTAZOLFrame.popInt(frame, top - 1);
        CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);
        field.setChar(object, (char) value);
      }
      case Float -> {
        double value = CILOSTAZOLFrame.popDouble(frame, top - 1);
        CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);
        field.setFloat(object, (float) value);
      }
      case Double -> {
        double value = CILOSTAZOLFrame.popDouble(frame, top - 1);
        CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);
        field.setDouble(object, value);
      }
      case Int -> {
        int value = CILOSTAZOLFrame.popInt(frame, top - 1);
        CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);
        field.setInt(object, value);
      }
      case Long -> {
        long value = CILOSTAZOLFrame.popLong(frame, top - 1);
        CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);
        field.setLong(object, value);
      }
      default -> {
        StaticObject value = CILOSTAZOLFrame.popObject(frame, top - 1);
        CILOSTAZOLFrame.popTaggedStack(taggedFrame, top - 1);
        field.setObject(object, value);
      }
    }
  }

  private void popStack(int top) {
    // pop taggedFrame
    taggedFrame[top] = null;
  }

  private Object getReturnValue(VirtualFrame frame, int top) {
    if (getMethod().hasReturnValue()) {
      TypeSymbol retType = getMethod().getReturnType().getType();
      return CILOSTAZOLFrame.pop(frame, top, retType);
    }
    // return code 0;
    return 0;
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
    if (opcode == CALL) {
      var method =
          SymbolResolver.resolveMethod(
              token,
              getMethod().getTypeArguments(),
              getMethod().getDefiningType().getTypeArguments(),
              getMethod().getModule());
      node = getCheckedCALLNode(method.member, top);
    } else {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      throw new InterpreterException();

    switch (opcode) {
      case LDSTR -> {
        CLIUSHeapPtr ptr = new CLIUSHeapPtr(token.getRowNo());
        node =
            new LDSTRNode(
                ptr.readString(method.getModule().getDefiningFile().getUSHeap()),
                top,
                SymbolResolver.getString(CILOSTAZOLContext.get(this)));
      }
      case NEWOBJ -> {
        MethodSymbol methodSymbol;
        switch (token.getTableId()) {
          case CLITableConstants.CLI_TABLE_MEMBER_REF -> {
            methodSymbol = getMethodSymbolFromMemberRef(top, token);
          }
          case CLITableConstants.CLI_TABLE_METHOD_DEF -> {
            methodSymbol = getMethodSymbolFromMethodDef(top, token);
          }
          default -> {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new InterpreterException();
          }
        }
        node = new NEWOBJNode(methodSymbol, top);
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
            taggedFrame, top, SymbolResolver.getSByte(CILOSTAZOLContext.get(this)));
        break;
      case CONV_I2:
        CILOSTAZOLFrame.putInt(frame, top, TypeHelpers.signExtend16(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, SymbolResolver.getInt16(CILOSTAZOLContext.get(this)));
        break;
      case CONV_I4:
        CILOSTAZOLFrame.putInt(frame, top, (int) TypeHelpers.truncate32(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, SymbolResolver.getInt32(CILOSTAZOLContext.get(this)));
        break;
      case CONV_I8:
        CILOSTAZOLFrame.putLong(frame, top, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, SymbolResolver.getInt64(CILOSTAZOLContext.get(this)));
        break;
      case CONV_U1:
        CILOSTAZOLFrame.putInt(frame, top, TypeHelpers.zeroExtend8(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, SymbolResolver.getByte(CILOSTAZOLContext.get(this)));
        break;
      case CONV_U2:
        CILOSTAZOLFrame.putInt(frame, top, TypeHelpers.zeroExtend16(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, SymbolResolver.getUInt16(CILOSTAZOLContext.get(this)));
        break;
      case CONV_U4:
        CILOSTAZOLFrame.putLong(frame, top, TypeHelpers.zeroExtend32(value));
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, SymbolResolver.getUInt32(CILOSTAZOLContext.get(this)));
        break;
      case CONV_U8:
        CILOSTAZOLFrame.putLong(frame, top, value);
        CILOSTAZOLFrame.putTaggedStack(
            taggedFrame, top, SymbolResolver.getUInt64(CILOSTAZOLContext.get(this)));
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
    if (frame.isObject(slot)) {
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
