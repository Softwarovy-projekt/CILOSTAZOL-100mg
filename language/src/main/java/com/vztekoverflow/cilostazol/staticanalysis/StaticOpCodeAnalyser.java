package com.vztekoverflow.cilostazol.staticanalysis;

import static com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions.*;
import static com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame.*;
import static com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame.StackType.*;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.bytecode.BytecodeBuffer;
import com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.exceptions.InvalidCLIException;
import com.vztekoverflow.cilostazol.exceptions.NotImplementedException;
import com.vztekoverflow.cilostazol.runtime.objectmodel.SystemType;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import com.vztekoverflow.cilostazol.runtime.symbols.*;
import java.util.Stack;

public class StaticOpCodeAnalyser {

  // region Stack manipulation
  public static OpCodeType[] analyseOpCodes(MethodSymbol method) {
    return getOpcodeTypes(
        method.getOriginalCIL(),
        method.getMaxStack() + method.getParameterCountIncludingInstance(),
        method.getParameterTypesIncludingInstance(),
        method.getLocals(),
        method.getReturnType(),
        method.getModule());
  }

  private static OpCodeType[] getOpcodeTypes(
      byte[] cil,
      int maxStack,
      TypeSymbol[] parameters,
      LocalSymbol[] locals,
      ReturnSymbol returnType,
      ModuleSymbol module) {
    var bytecodeBuffer = new BytecodeBuffer(cil);
    OpCodeType[] types = new OpCodeType[cil.length];
    boolean[] visited = new boolean[cil.length];
    Stack<Integer> visitStack = new Stack<>();
    var stack = new StackType[maxStack];
    var topStack = 0;
    int pc = 0;
    visitStack.push(0);
    while (!visitStack.isEmpty()) {
      pc = visitStack.pop();
      var nextPc = bytecodeBuffer.nextInstruction(pc);
      if (nextPc < cil.length
          && !visited[
              nextPc]) // !visited can be removed as a safety measure to traverse all the opcodes
      visitStack.add(nextPc);

      int curOpcode = bytecodeBuffer.getOpcode(pc);
      if (!visited[pc])
        topStack =
            resolveOpCode(
                parameters,
                locals,
                returnType,
                module,
                bytecodeBuffer,
                types,
                visited,
                visitStack,
                stack,
                topStack,
                pc,
                nextPc,
                curOpcode);

      topStack += BytecodeInstructions.getStackEffect(curOpcode);
      visited[pc] = true;
    }

    return types;
  }

  private static int resolveOpCode(
      TypeSymbol[] parameters,
      LocalSymbol[] locals,
      ReturnSymbol returnType,
      ModuleSymbol module,
      BytecodeBuffer bytecodeBuffer,
      OpCodeType[] types,
      boolean[] visited,
      Stack<Integer> visitStack,
      StackType[] stack,
      int topStack,
      int pc,
      int nextPc,
      int curOpcode) {
    switch (curOpcode) {
      case NOP:
      case BREAK:
        break;

      case STLOC_0:
      case STLOC_1:
      case STLOC_2:
      case STLOC_3:
      case STLOC_S:
        // case STLOC: //we do not track this opcode
      case STARG_S:
        // case STARG: //we do not track this opcode
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        clear(stack, topStack);
        break;

      case LDARG_0:
      case LDARG_1:
      case LDARG_2:
      case LDARG_3:
        handleArg(parameters, stack, topStack, curOpcode - LDARG_0);
        break;

      case LDARG_S:
        // case LDARG: //we do not track this opcode
        handleArg(parameters, stack, topStack, bytecodeBuffer.getImmUByte(pc));
        break;

      case LDARGA_S:
        // case LDARGA: //we do not track this opcode
        push(stack, topStack, StackType.ManagedPointer);
        break;

      case LDLOC_0:
      case LDLOC_1:
      case LDLOC_2:
      case LDLOC_3:
        handleLoc(locals, stack, topStack, curOpcode - LDLOC_0);
        break;

      case LDLOC_S:
        // case LDLOC: //we do not track this opcode
        handleLoc(locals, stack, topStack, bytecodeBuffer.getImmUByte(pc));
        break;

      case LDLOCA_S:
        // case LDLOCA: //we do not track this opcode
        push(stack, topStack, StackType.ManagedPointer);
        break;

      case LDNULL:
        push(stack, topStack, StackType.Object);
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
      case LDC_I4_S:
      case LDC_I4:
        push(stack, topStack, Int32);
        break;
      case LDC_I8:
        push(stack, topStack, Int64);
        break;
      case LDC_R4:
      case LDC_R8:
        push(stack, topStack, StackType.NativeFloat);
        break;
      case DUP:
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        push(stack, topStack, stack[topStack - 1]);
        break;
      case POP:
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        clear(stack, topStack);
        break;
      case JMP:
        break;
      case CALL:
        {
          var methodPtr = bytecodeBuffer.getImmToken(pc);
          var method = SymbolResolver.resolveMethod(methodPtr, module).member;
          topStack = handleMethod(method, stack, topStack);
          break;
        }
      case CALLVIRT:
        {
          var methodPtr = bytecodeBuffer.getImmToken(pc);
          var method = SymbolResolver.resolveMethod(methodPtr, module).member;
          topStack = handleMethod(method, stack, topStack);
          break;
        }
      case CALLI:
        // TODO: after implementing functionality of this opcode
        break;
      case RET:
        {
          if (returnType.getType().getSystemType() != SystemType.Void) {
            clear(stack, topStack);
            topStack--;
          }
          break;
        }
      case BR:
        handleOpCodeJump(bytecodeBuffer, visited, visitStack, pc, nextPc);
        break;
      case BR_S:
        handleOpCodeJumpShort(bytecodeBuffer, visited, visitStack, pc, nextPc);
        break;
      case BRFALSE_S:
      case BRTRUE_S:
        handleOpCodeJumpShort(bytecodeBuffer, visited, visitStack, pc, nextPc);
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        clear(stack, topStack);
        break;
      case BRFALSE:
      case BRTRUE:
        handleOpCodeJump(bytecodeBuffer, visited, visitStack, pc, nextPc);
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        clear(stack, topStack);
        break;
      case BEQ_S:
      case BGE_S:
      case BGT_S:
      case BLE_S:
      case BLT_S:
      case BNE_UN_S:
      case BGE_UN_S:
      case BGT_UN_S:
      case BLE_UN_S:
      case BLT_UN_S:
        handleOpCodeJumpShort(bytecodeBuffer, visited, visitStack, pc, nextPc);
        handleBinaryComparison(types, stack, topStack, pc, curOpcode);
        break;
      case BEQ:
      case BGE:
      case BGT:
      case BLE:
      case BLT:
      case BNE_UN:
      case BGE_UN:
      case BGT_UN:
      case BLE_UN:
      case BLT_UN:
        handleOpCodeJump(bytecodeBuffer, visited, visitStack, pc, nextPc);
        handleBinaryComparison(types, stack, topStack, pc, curOpcode);
        break;
        // Same as BRFALSE
        //        case BRNULL: break;
        //        case BRZERO: break;
        //        case BRINST: break;
        //        case BRTRUE: break;
      case SWITCH:
        {
          var numCases = bytecodeBuffer.getImmUInt(pc);
          for (var i = 0; i < numCases; i++) {
            var caseOffset = pc + 4 + i * 4;
            handleOpCodeJump(bytecodeBuffer, visited, visitStack, caseOffset, nextPc);
          }
          // is UInt32 by default
          break;
        }
      case LDIND_I1:
      case LDIND_U1:
      case LDIND_I2:
      case LDIND_U2:
      case LDIND_I4:
      case LDIND_U4:
        replace(stack, topStack, Int32);
        break;
      case LDIND_I8:
        // case LDIND_U8: //we do not track this opcode
        replace(stack, topStack, Int64);
        break;
      case LDIND_I:
        replace(stack, topStack, NativeInt);
        break;
      case LDIND_R4:
      case LDIND_R8:
        replace(stack, topStack, NativeFloat);
        break;
      case LDIND_REF:
        replace(stack, topStack, Object);
        break;
      case STIND_REF:
      case STIND_I1:
      case STIND_I2:
      case STIND_I4:
      case STIND_I8:
      case STIND_R4:
      case STIND_R8:
      case STIND_I:
        clear(stack, topStack);
        clear(stack, topStack - 1);
        break;
      case ADD:
      case SUB:
      case MUL:
      case DIV:
      case REM:
        handleBinaryNumericOperations(types, stack, topStack, pc, curOpcode);
        break;
      case ADD_OVF:
      case ADD_OVF_UN:
      case MUL_OVF:
      case MUL_OVF_UN:
      case SUB_OVF:
      case SUB_OVF_UN:
        handleOverflowArithmeticOperations(types, stack, topStack, pc, curOpcode);
        break;
      case AND:
      case DIV_UN:
      case REM_UN:
      case OR:
      case XOR:
        handleIntegerOperations(types, stack, topStack, pc, curOpcode);
        break;
      case SHL:
      case SHR:
      case SHR_UN:
        handleShiftOperations(types, stack, topStack, pc, curOpcode);
        break;
      case NEG:
      case NOT:
        handleUnaryNumericOperations(types, stack, topStack, pc, curOpcode);
        break;
      case CONV_I1:
      case CONV_I2:
      case CONV_I4:
      case CONV_U2:
      case CONV_U1:
      case CONV_U4:
      case CONV_OVF_I1:
      case CONV_OVF_U1:
      case CONV_OVF_I2:
      case CONV_OVF_U2:
      case CONV_OVF_I4:
      case CONV_OVF_U4:
      case CONV_OVF_I1_UN:
      case CONV_OVF_I2_UN:
      case CONV_OVF_I4_UN:
      case CONV_OVF_U1_UN:
      case CONV_OVF_U2_UN:
      case CONV_OVF_U4_UN:
        checkRestrictedConversionOperations(stack, topStack, curOpcode);
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        replace(stack, topStack, Int32);
        break;
      case CONV_I8:
      case CONV_U8:
      case CONV_OVF_I8:
      case CONV_OVF_U8:
      case CONV_OVF_I8_UN:
      case CONV_OVF_U8_UN:
        // is not restricted to any type
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        replace(stack, topStack, Int64);
        break;
      case CONV_R4:
      case CONV_R_UN:
      case CONV_R8:
        checkRestrictedConversionOperations(stack, topStack, curOpcode);
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        replace(stack, topStack, NativeFloat);
        break;
      case CONV_I:
      case CONV_U:
      case CONV_OVF_I:
      case CONV_OVF_U:
      case CONV_OVF_I_UN:
      case CONV_OVF_U_UN:
        // is not restricted to any type
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        replace(stack, topStack, NativeInt);
        break;
      case CPOBJ:
        handleCopyObject(types, stack, topStack, pc);
        break;
      case LDOBJ:
        {
          var typePtr = bytecodeBuffer.getImmToken(pc);
          setTypeByStack(types, stack, topStack, pc, curOpcode);
          var type = SymbolResolver.resolveType(typePtr, module);
          replace(stack, topStack, type.getStackTypeKind());
          break;
        }
      case LDSTR:
        push(stack, topStack, Object);
        break;
      case NEWOBJ:
        {
          var ctorPtr = bytecodeBuffer.getImmToken(pc);
          topStack = handleCtor(ctorPtr, stack, topStack, module);
          break;
        }
      case CASTCLASS:
        // Obj cast to Obj
        break;
      case ISINST:
        // Obj to Obj
        break;
      case UNBOX:
        replace(stack, topStack, ManagedPointer);
        break;
      case UNBOX_ANY:
        {
          var objPtr = bytecodeBuffer.getImmToken(pc);
          var objType = SymbolResolver.resolveType(objPtr, module);
          replace(stack, topStack, objType.getStackTypeKind());
          break;
        }
      case BOX:
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        replace(stack, topStack, Object);
        break;
      case THROW:
        clear(stack, topStack);
        break;
      case LDFLD:
        {
          var fieldPtr = bytecodeBuffer.getImmToken(pc);
          var field = SymbolResolver.resolveField(fieldPtr, module).member;
          replace(stack, topStack, field.getType().getStackTypeKind());
          break;
        }
      case LDSFLD:
        {
          var fieldPtr = bytecodeBuffer.getImmToken(pc);
          var field = SymbolResolver.resolveField(fieldPtr, module).member;
          push(stack, topStack, field.getType().getStackTypeKind());
          break;
        }
      case LDFLDA:
        handleLdflda(stack, topStack, pc);
        break;
      case LDSFLDA:
        {
          var fieldPtr = bytecodeBuffer.getImmToken(pc);
          var field = SymbolResolver.resolveField(fieldPtr, module).member;
          handleLdsflda((NamedTypeSymbol) field.getType(), stack, topStack, pc);
          break;
        }
      case STFLD:
        setTypeByStack(types, stack, topStack, pc, curOpcode); // native int or managed pointer
        clear(stack, topStack);
        clear(stack, topStack - 1);
        break;
      case STSFLD:
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        clear(stack, topStack);
        break;
      case STOBJ:
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        clear(stack, topStack);
        clear(stack, topStack - 1);
        break;
      case NEWARR:
        setTypeByStack(types, stack, topStack, pc, curOpcode); // native int or int32 for the size
        replace(stack, topStack, Object);
        break;
      case LDLEN:
        replace(stack, topStack, Int32); // native usnigned int
        break;
      case LDELEMA:
        setTypeByStack(types, stack, topStack, pc, curOpcode); // native int or int32 for the index
        clear(stack, topStack);
        replace(stack, topStack - 1, ManagedPointer);
        break;
      case LDELEM:
        {
          var elemTypePtr = bytecodeBuffer.getImmToken(pc);
          var elemType = SymbolResolver.resolveType(elemTypePtr, module);
          setTypeByStack(types, stack, topStack, pc, curOpcode);
          clear(stack, topStack);
          replace(stack, topStack - 1, elemType.getStackTypeKind());
          break;
        }
      case LDELEM_I1:
      case LDELEM_I2:
      case LDELEM_I4:
      case LDELEM_U1:
      case LDELEM_U2:
      case LDELEM_U4:
        setTypeByStack(types, stack, topStack, pc, curOpcode); // native int or int32 for the index
        clear(stack, topStack);
        replace(stack, topStack - 1, Int32);
        break;
      case LDELEM_I8:
        // case LDELEM_U8: //same opcode as LDELEM_I8
        setTypeByStack(types, stack, topStack, pc, curOpcode); // native int or int32 for the index
        clear(stack, topStack);
        replace(stack, topStack - 1, Int64);
        break;
      case LDELEM_I:
        setTypeByStack(types, stack, topStack, pc, curOpcode); // native int or int32 for the index
        clear(stack, topStack);
        replace(stack, topStack - 1, NativeInt);
        break;
      case LDELEM_R4:
      case LDELEM_R8:
        setTypeByStack(types, stack, topStack, pc, curOpcode); // native int or int32 for the index
        clear(stack, topStack);
        replace(stack, topStack - 1, NativeFloat);
        break;
      case LDELEM_REF:
        setTypeByStack(types, stack, topStack, pc, curOpcode); // native int or int32 for the index
        clear(stack, topStack);
        replace(stack, topStack - 1, Object);
        break;
      case STELEM:
      case STELEM_I1:
      case STELEM_I2:
      case STELEM_I4:
      case STELEM_I8:
      case STELEM_R4:
      case STELEM_R8:
      case STELEM_REF:
      case STELEM_I:
        setTypeByStack(
            types, stack, topStack - 1, pc, curOpcode); // native int or int32 for the index
        clear(stack, topStack);
        clear(stack, topStack - 1);
        clear(stack, topStack - 2);
        break;
      case REFANYVAL:
        {
          var typePtr = bytecodeBuffer.getImmToken(pc);
          var type = SymbolResolver.resolveType(typePtr, module);
          types[pc] = getUnaryOpCodeType(type.getStackTypeKind(), curOpcode);
          replace(stack, topStack, ManagedPointer);
          break;
        }
      case CKFINITE:
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        break;
      case MKREFANY:
        replace(stack, topStack, ManagedPointer); // typed reference is a managed pointer:
        // https://dotnet.github.io/dotNext/features/core/ref.html
        break;
      case ENDFAULT:
        // case ENDFINALLY: same opcode as ENDFAULT
      case LEAVE_S:
        handleOpCodeJumpShort(bytecodeBuffer, visited, visitStack, pc, nextPc);
        break;
      case LEAVE:
        handleOpCodeJump(bytecodeBuffer, visited, visitStack, pc, nextPc);
        // Nothing
        break;
      case CEQ:
      case CGT:
      case CGT_UN:
      case CLT:
      case CLT_UN:
        handleBinaryComparison(types, stack, topStack, pc, curOpcode);
        replace(stack, topStack - 1, Int32); // 1st operand cleared inside handleBinaryComparison
        break;
      case INITOBJ:
        setTypeByStack(types, stack, topStack, pc, curOpcode);
        clear(stack, topStack);
        break;
      case SIZEOF:
        push(stack, topStack, Int32);
        break;
      case TRUFFLE_NODE:
        // TODO
        break;
      default:
        ThrowNotSupportedException();
        break;
    }
    return topStack;
  }

  private static void handleOpCodeJump(
      BytecodeBuffer bytecodeBuffer,
      boolean[] visited,
      Stack<Integer> visitStack,
      int pc,
      int nextPc) {
    var offset = bytecodeBuffer.getImmInt(pc);
    var target = nextPc + offset;
    if (!visited[target]) visitStack.push(target);
  }

  private static void handleOpCodeJumpShort(
      BytecodeBuffer bytecodeBuffer,
      boolean[] visited,
      Stack<Integer> visitStack,
      int pc,
      int nextPc) {
    var offset = bytecodeBuffer.getImmByte(pc);
    var target = nextPc + offset;
    if (!visited[target]) visitStack.push(target);
  }

  private static void handleLdsflda(
      NamedTypeSymbol field, StackType[] stack, int topStack, int pc) {
    // TODO: handle if field is in unmanaged memory -> then push native int
    push(stack, topStack, ManagedPointer);
  }

  private static void handleLdflda(StackType[] stack, int topStack, int pc) {
    var stackType = stack[topStack - 1];
    switch (stackType) {
      case Object, ManagedPointer -> push(stack, topStack, ManagedPointer);
      case NativeInt -> push(stack, topStack, NativeInt);
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", stackType, pc, LDFLDA));
    }
  }

  private static int handleCtor(
      CLITablePtr ctorPtr, StackType[] stack, int topStack, ModuleSymbol module) {
    var ctor = SymbolResolver.resolveMethod(ctorPtr, module).member;
    // A new argument is added during the constructor call -> topStack + 1
    topStack = updateStackByMethod(stack, topStack + 1, ctor);
    // A new object is added to the stack after the constructor call -> topStack + 1
    topStack++;
    replace(stack, topStack, Object);
    return topStack;
  }

  private static int handleMethod(MethodSymbol method, StackType[] stack, int topStack) {
    return updateStackByMethod(stack, topStack, method);
  }

  private static int updateStackByMethod(StackType[] stack, int topStack, MethodSymbol method) {
    // remove args from stack
    // put ret type to stack
    for (int i = 0; i < method.getParameterCountIncludingInstance(); i++) {
      clear(stack, topStack - i);
    }

    if (method.hasReturnValue())
      replace(
          stack,
          topStack - method.getParameterCountIncludingInstance(),
          method.getReturnType().getType().getStackTypeKind());

    return topStack
        - method.getParameterCountIncludingInstance()
        + (method.hasReturnValue() ? 1 : 0);
  }

  /**
   * @param topStack Points to the idx + 1 of a value on the stack we care about
   */
  private static void setTypeByStack(
      OpCodeType[] types, StackType[] stack, int topStack, int pc, int opCode) {
    types[pc] = getUnaryOpCodeType(stack[topStack - 1], opCode);
  }

  /**
   * @ApiNote: Partition III, Table 2: Binary Numeric Operations
   */
  private static void handleBinaryNumericOperations(
      OpCodeType[] types, StackType[] stack, int topStack, int pc, int opCode) {
    var right = stack[topStack - 1];
    var left = stack[topStack - 2];

    // clear right operand
    clear(stack, topStack);

    var opCodeType = getBinaryOpCodeType(left, right, opCode);

    // replace left operand with resulting type
    switch (opCodeType) {
      case Int32 -> replace(stack, topStack - 1, Int32);
      case Int32_NativeInt -> replace(stack, topStack - 1, StackType.NativeInt);
      case Int32_ManagedPointer -> replace(stack, topStack - 1, StackType.ManagedPointer);
      case Int64 -> replace(stack, topStack - 1, Int64);
      case NativeInt_Int32 -> replace(stack, topStack - 1, StackType.NativeInt);
      case NativeInt -> replace(stack, topStack - 1, StackType.NativeInt);
      case NativeInt_ManagedPointer -> replace(stack, topStack - 1, StackType.ManagedPointer);
      case NativeFloat -> replace(stack, topStack - 1, StackType.NativeFloat);
      case ManagedPointer_Int32 -> replace(stack, topStack - 1, StackType.ManagedPointer);
      case ManagedPointer_NativeInt -> replace(stack, topStack - 1, StackType.ManagedPointer);
      case ManagedPointer -> replace(stack, topStack - 1, StackType.NativeInt);
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));
    }

    // set op code type
    types[pc] = opCodeType;
  }

  /**
   * @ApiNote: Partition III, 4.4 cpobj
   */
  private static void handleCopyObject(
      OpCodeType[] types, StackType[] stack, int topStack, int pc) {
    var src = stack[topStack - 1];
    var dest = stack[topStack - 2];
    var opCodeType = getBinaryOpCodeType(src, dest, CPOBJ);

    // clear right operand
    clear(stack, topStack);
    // clear left operand
    clear(stack, topStack - 1);

    switch (opCodeType) {
      case ManagedPointer -> types[pc] = OpCodeType.ManagedPointer;
      case NativeInt -> types[pc] = OpCodeType.NativeInt;
        // specification does not explicitly say if mixed is allowed
      case ManagedPointer_NativeInt -> types[pc] = OpCodeType.ManagedPointer_NativeInt;
      case NativeInt_ManagedPointer -> types[pc] = OpCodeType.NativeInt_ManagedPointer;
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), CPOBJ));
    }
  }

  /**
   * @ApiNote: Partition III, Table 3: Unary Numeric Operations
   */
  private static void handleUnaryNumericOperations(
      OpCodeType[] types, StackType[] stack, int topStack, int pc, int opCode) {
    var right = stack[topStack - 1];
    // replace right operand with resulting type
    switch (right) {
      case Int32 -> {
        types[pc] = OpCodeType.Int32;
        replace(stack, topStack, Int32);
      }
      case Int64 -> {
        types[pc] = OpCodeType.Int64;
        replace(stack, topStack, Int64);
      }
      case NativeInt -> {
        types[pc] = OpCodeType.NativeInt;
        replace(stack, topStack, StackType.NativeInt);
      }
      case NativeFloat -> {
        types[pc] = OpCodeType.NativeFloat;
        replace(stack, topStack, StackType.NativeInt);
      }
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", right.name(), opCode));
    }
  }

  /**
   * @ApiNote: Partition III, Table 4: Binary Comparison or Branch Operations
   */
  private static void handleBinaryComparison(
      OpCodeType[] types, StackType[] stack, int topStack, int pc, int opCode) {
    var right = stack[topStack - 1];
    var left = stack[topStack - 2];

    // clear right operand
    clear(stack, topStack);

    var opCodeType = getBinaryOpCodeType(left, right, opCode);

    // Check restriction on some operands
    boolean isRestrictedOpCodeType =
        opCodeType == OpCodeType.NativeInt_ManagedPointer
            || opCodeType == OpCodeType.ManagedPointer_NativeInt
            || opCodeType == OpCodeType.Object;
    boolean isAllowedOpCode =
        opCode == BEQ
            || opCode == BEQ_S
            || opCode == BNE_UN
            || opCode == BNE_UN_S
            || opCode == CEQ
            || opCode == CGT_UN;
    if (isRestrictedOpCodeType && !isAllowedOpCode)
      ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));

    // check if op code is allowed on operand types
    switch (opCodeType) {
      case Int32,
          Int32_NativeInt,
          Int64,
          NativeInt_Int32,
          NativeInt,
          NativeInt_ManagedPointer,
          NativeFloat,
          ManagedPointer_NativeInt,
          ManagedPointer,
          Object -> {
        /*Do nothing*/
      }
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));
    }
    // clear left operand
    clear(stack, topStack - 1);

    // set op code type
    types[pc] = opCodeType;
  }

  /**
   * @ApiNote: Partition III, Table 5: Integer Operations
   */
  private static void handleIntegerOperations(
      OpCodeType[] types, StackType[] stack, int topStack, int pc, int opCode) {
    var right = stack[topStack - 1];
    var left = stack[topStack - 2];

    // clear right operand
    clear(stack, topStack);

    var opCodeType = getBinaryOpCodeType(left, right, opCode);

    // check if op code is allowed on operand types and replace left operand with resulting type
    switch (opCodeType) {
      case Int32 -> replace(stack, topStack - 1, Int32);
      case Int32_NativeInt -> replace(stack, topStack - 1, StackType.NativeInt);
      case Int64 -> replace(stack, topStack - 1, Int64);
      case NativeInt_Int32 -> replace(stack, topStack - 1, StackType.NativeInt);
      case NativeInt -> replace(stack, topStack - 1, StackType.NativeInt);
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));
    }

    types[pc] = opCodeType;
  }

  /**
   * @ApiNote: Partition III, Table 6: Shift Operations
   */
  private static void handleShiftOperations(
      OpCodeType[] types, StackType[] stack, int topStack, int pc, int opCode) {
    var toBeShifted = stack[topStack - 1];
    var shiftBy = stack[topStack - 2];

    var opCodeType = getBinaryOpCodeType(shiftBy, toBeShifted, opCode);

    // clear right operand
    clear(stack, topStack);

    // check if op code is allowed on operand types and replace left operand with resulting type
    switch (opCodeType) {
      case Int32 -> replace(stack, topStack - 1, Int32);
      case Int32_NativeInt -> replace(stack, topStack - 1, Int32);
      case Int64_Int32 -> replace(stack, topStack - 1, Int64);
      case Int64_NativeInt -> replace(stack, topStack - 1, Int64);
      case NativeInt_Int32 -> replace(stack, topStack - 1, StackType.NativeInt);
      case NativeInt -> replace(stack, topStack - 1, StackType.NativeInt);
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));
    }

    types[pc] = opCodeType;
  }

  /**
   * @ApiNote: Partition III, Table 7: Overflow Arithmetic Operations
   */
  private static void handleOverflowArithmeticOperations(
      OpCodeType[] types, StackType[] stack, int topStack, int pc, int opCode) {
    var right = stack[topStack - 1];
    var left = stack[topStack - 2];

    var opCodeType = getBinaryOpCodeType(left, right, opCode);

    // clear right operand
    clear(stack, topStack);

    // Check restriction on some operands
    boolean isAddRestricted =
        opCodeType == OpCodeType.Int32_ManagedPointer
            || opCodeType == OpCodeType.NativeInt_ManagedPointer;
    boolean isSubRestricted = opCodeType == OpCodeType.ManagedPointer;
    boolean isAddSubRestricted =
        opCodeType == OpCodeType.ManagedPointer_Int32
            || opCodeType == OpCodeType.ManagedPointer_NativeInt;
    if (isAddRestricted && opCode != ADD_OVF_UN)
      ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));
    if (isSubRestricted && opCode != SUB_OVF_UN)
      ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));
    if (isAddSubRestricted && (opCode != ADD_OVF_UN && opCode != SUB_OVF_UN))
      ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));

    // check if op code is allowed on operand types and replace left operand with resulting type
    switch (opCodeType) {
      case Int32 -> replace(stack, topStack - 1, Int32);
      case Int32_NativeInt -> replace(stack, topStack - 1, StackType.NativeInt);
      case Int32_ManagedPointer -> replace(stack, topStack - 1, StackType.ManagedPointer);
      case Int64 -> replace(stack, topStack - 1, Int64);
      case NativeInt_Int32 -> replace(stack, topStack - 1, StackType.NativeInt);
      case NativeInt -> replace(stack, topStack - 1, StackType.NativeInt);
      case NativeInt_ManagedPointer -> replace(stack, topStack - 1, StackType.ManagedPointer);
      case ManagedPointer_Int32 -> replace(stack, topStack - 1, StackType.ManagedPointer);
      case ManagedPointer_NativeInt -> replace(stack, topStack - 1, StackType.ManagedPointer);
      case ManagedPointer -> replace(stack, topStack - 1, StackType.NativeInt);
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", opCodeType.name(), opCode));
    }

    types[pc] = opCodeType;
  }

  /**
   * @ApiNote: Partition III, Table 8: Conversion Operations
   */
  private static void checkRestrictedConversionOperations(
      StackType[] stack, int topStack, int opCode) {
    var operand = stack[topStack - 1];

    if (operand == StackType.ManagedPointer || operand == StackType.Object)
      ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", operand.name(), opCode));
  }

  @CompilerDirectives.TruffleBoundary
  private static OpCodeType ThrowInvalidCLI(String message) {
    throw new InvalidCLIException(message);
  }

  @CompilerDirectives.TruffleBoundary
  private static void ThrowNotSupportedException() {
    throw new NotImplementedException();
  }

  // region type comparison helpers
  private static OpCodeType getUnaryOpCodeType(StackType operand, int opCode) {
    return switch (operand) {
      case Int32 -> OpCodeType.Int32;
      case Int64 -> OpCodeType.Int64;
      case NativeInt -> OpCodeType.NativeInt;
      case NativeFloat -> OpCodeType.NativeFloat;
      case Object -> OpCodeType.Object;
      case ManagedPointer -> OpCodeType.ManagedPointer;
      default -> ThrowInvalidCLI(
          CILOSTAZOLBundle.message(
              "cilostazol.exception.invalid.type.on.stack", operand.name(), opCode));
    };
  }

  private static OpCodeType getBinaryOpCodeType(StackType left, StackType right, int opCode) {
    if (int32_int32(left, right)) {
      return OpCodeType.Int32;
    }
    if (int32_nativeInt(left, right)) {
      return OpCodeType.Int32_NativeInt;
    }
    if (int32_managedPointer(left, right)) {
      return OpCodeType.Int32_ManagedPointer;
    }
    if (int64_int64(left, right)) {
      return OpCodeType.Int64;
    }
    if (nativeInt_int32(left, right)) {
      return OpCodeType.NativeInt_Int32;
    }
    if (nativeInt_nativeInt(left, right)) {
      return OpCodeType.NativeInt;
    }
    if (nativeInt_managedPointer(left, right)) {
      return OpCodeType.NativeInt_ManagedPointer;
    }
    if (nativeFloat_nativeFloat(left, right)) {
      return OpCodeType.NativeFloat;
    }
    if (managedPointer_int32(left, right)) {
      return OpCodeType.ManagedPointer_Int32;
    }
    if (managedPointer_nativeInt(left, right)) {
      return OpCodeType.ManagedPointer_NativeInt;
    }
    if (managedPointer_managedPointer(left, right)) {
      return OpCodeType.ManagedPointer;
    }
    if (object_object(left, right)) {
      return OpCodeType.Object;
    }
    // shift operations
    if (int64_int32(left, right)) {
      return OpCodeType.Int64_Int32;
    }
    if (int64_nativeInt(left, right)) {
      return OpCodeType.Int64_NativeInt;
    }

    return ThrowInvalidCLI(
        CILOSTAZOLBundle.message(
            "cilostazol.exception.illegal.stack.types.for.binary.opcode",
            left.name(),
            right.name(),
            opCode));
  }

  private static boolean managedPointer_managedPointer(StackType left, StackType right) {
    return left == StackType.ManagedPointer && right == StackType.ManagedPointer;
  }

  private static boolean int32_int32(StackType left, StackType right) {
    return left == Int32 && right == Int32;
  }

  private static boolean int32_nativeInt(StackType left, StackType right) {
    return left == Int32 && right == StackType.NativeInt;
  }

  private static boolean int32_managedPointer(StackType left, StackType right) {
    return left == Int32 && right == StackType.ManagedPointer;
  }

  private static boolean int64_int32(StackType left, StackType right) {
    return left == Int64 && right == Int32;
  }

  private static boolean int64_int64(StackType left, StackType right) {
    return left == Int64 && right == Int64;
  }

  private static boolean int64_nativeInt(StackType left, StackType right) {
    return left == Int64 && right == StackType.NativeInt;
  }

  private static boolean nativeInt_int32(StackType left, StackType right) {
    return left == StackType.NativeInt && right == Int32;
  }

  private static boolean nativeInt_nativeInt(StackType left, StackType right) {
    return left == StackType.NativeInt && right == StackType.NativeInt;
  }

  private static boolean nativeInt_managedPointer(StackType left, StackType right) {
    return left == StackType.NativeInt && right == StackType.ManagedPointer;
  }

  private static boolean managedPointer_int32(StackType left, StackType right) {
    return left == StackType.ManagedPointer && right == Int32;
  }

  private static boolean managedPointer_nativeInt(StackType left, StackType right) {
    return left == StackType.ManagedPointer && right == StackType.NativeInt;
  }

  private static boolean nativeFloat_nativeFloat(StackType left, StackType right) {
    return left == StackType.NativeFloat && right == StackType.NativeFloat;
  }

  private static boolean object_object(StackType left, StackType right) {
    return left == StackType.Object && right == StackType.Object;
  }

  private static void handleLoc(LocalSymbol[] locals, StackType[] stack, int topStack, int idx) {
    var locType = locals[idx].getType().getStackTypeKind();
    push(stack, topStack, locType);
  }

  // endregion

  private static void handleArg(TypeSymbol[] parameters, StackType[] stack, int topStack, int idx) {
    var argType = parameters[idx].getStackTypeKind();
    push(stack, topStack, argType);
  }

  private static void clear(StackType[] stack, int topStack) {
    stack[topStack - 1] = null;
  }

  private static void replace(StackType[] stack, int topStack, StackType stackType) {
    stack[topStack - 1] = stackType;
  }

  private static void push(StackType[] stack, int topStack, StackType stackType) {
    stack[topStack] = stackType;
  }

  public enum OpCodeType {
    Int32,
    Int64,
    Object,
    ManagedPointer,
    NativeInt,
    NativeFloat,
    Int32_NativeInt,
    Int32_ManagedPointer,
    NativeInt_Int32,
    NativeInt_ManagedPointer,
    ManagedPointer_Int32,
    ManagedPointer_NativeInt,
    Int64_Int32,
    Int64_NativeInt,
  }
  // endregion
}
