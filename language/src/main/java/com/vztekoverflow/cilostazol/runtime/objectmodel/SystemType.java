package com.vztekoverflow.cilostazol.runtime.objectmodel;

import static com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions.*;
import static com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions.INITOBJ;
import static com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants.CLI_TABLE_METHOD_SPEC;
import static com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants.CLI_TABLE_TYPE_REF;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.cil.parser.bytecode.BytecodeBuffer;
import com.vztekoverflow.cil.parser.bytecode.BytecodeInstructions;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cil.parser.cli.signature.MethodDefSig;
import com.vztekoverflow.cil.parser.cli.signature.SignatureReader;
import com.vztekoverflow.cil.parser.cli.table.CLITablePtr;
import com.vztekoverflow.cil.parser.cli.table.generated.CLITableConstants;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.exceptions.NotImplementedException;
import com.vztekoverflow.cilostazol.runtime.other.TableRowUtils;
import com.vztekoverflow.cilostazol.runtime.symbols.*;
import java.util.Arrays;
import java.util.Objects;

public enum SystemType {
  Boolean,
  Char,
  Int,
  Short,
  Float,
  Long,
  Double,
  Void,
  Object,
  /**
   * <b><i>NOT A REAL TYPE</i></b><br>
   * This one is purely for stack sanity purposes Used for clearing the stack after a call
   */
  None;

  public static SystemType getTypeKind(String name, String namespace, AssemblyIdentity assembly) {
    if (AssemblyIdentity.SystemPrivateCoreLib700().equalsVersionAgnostic(assembly)
        && Objects.equals(namespace, "System")) {
      return switch (name) {
        case "Boolean" -> Boolean;
        case "Byte", "SByte", "Char" -> Char;
        case "Int16", "UInt16" -> Short;
        case "Int32", "UInt32" -> Int;
        case "Double" -> Double;
        case "Single" -> Float;
        case "Int64", "UInt64" -> Long;
        case "Void" -> Void;
        default -> Object;
          // Decimal, UIntPtr, IntPtr ??
      };
    }

    return Object;
  }

  public static SystemType[] GetSystemTypes(
      byte[] cil,
      int maxStack,
      ParameterSymbol[] parameters,
      LocalSymbol[] locals,
      ReturnSymbol retType,
      ModuleSymbol module) {
    var bytecodeBuffer = new BytecodeBuffer(cil);
    SystemType[] types = new SystemType[cil.length];
    var stack = new SystemType[maxStack];
    var topStack = 0;
    int pc = 0;
    while (pc < cil.length) {
      int curOpcode = bytecodeBuffer.getOpcode(pc);
      int nextpc = bytecodeBuffer.nextInstruction(pc);
      switch (curOpcode) {
          // region Does not affect stack
        case NOP:
        case BREAK:
        case STLOC_0:
        case STLOC_1:
        case STLOC_2:
        case STLOC_3:
        case STARG_S:
        case STLOC_S:
          break;
          // endregion
          // region Args
        case LDARG_0:
        case LDARG_1:
        case LDARG_2:
        case LDARG_3:
          handleArg(parameters, stack, topStack, curOpcode - LDARG_0);
          break;
        case LDARG_S:
          handleArg(parameters, stack, topStack, bytecodeBuffer.getImmUByte(pc));
          break;
        case LDARGA_S:
          push(stack, topStack, Int);
          // endregion
          // region Locals
        case LDLOC_0:
        case LDLOC_1:
        case LDLOC_2:
        case LDLOC_3:
          handleLoc(locals, stack, topStack, curOpcode - LDLOC_0);
          break;
        case LDLOC_S:
          handleLoc(locals, stack, topStack, bytecodeBuffer.getImmUByte(pc));
          break;
        case LDLOCA_S:
          push(stack, topStack, Int);
          break;
          // endregion
        case LDNULL:
          push(stack, pc, Object);
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
          push(stack, topStack, Int);
          break;
        case LDC_I8:
          push(stack, topStack, Long);
          break;
        case LDC_R4:
          push(stack, topStack, Float);
          break;
        case LDC_R8:
          push(stack, topStack, Double);
          break;
        case DUP:
          setTypeByStack(types, stack, topStack, pc);
          push(stack, topStack, stack[topStack - 1]);
          break;
        case POP:
          setTypeByStack(types, stack, topStack, pc);
          clear(stack, topStack);
          break;
        case JMP:
          break;
        case CALL:
          var methodPtr = bytecodeBuffer.getImmToken(pc);
          topStack = handleMethod(methodPtr, stack, topStack, module, false);
          break;
        case CALLI:
          // TODO: after implementing functionality of this opcode
          break;
        case RET:
          // From stack to stack == nothing
          break;
        case BR:
        case BR_S:
          // Nothing
          break;
        case BRFALSE_S:
        case BRTRUE_S:
        case BRFALSE:
          setTypeByStack(types, stack, topStack, pc);
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
          // Since only pairs of the same value are comparable,
          // we can be sure that the top two values are of the same type
          clear(stack, topStack); // clear 2nd operand
          setTypeByStack(types, stack, topStack - 1, pc); // use 1st operand
          break;
          // Same as BRFALSE
          //        case BRNULL: break;
          //        case BRZERO: break;
          //        case BRINST: break;
          //        case BRTRUE: break;
        case SWITCH:
          // is UInt32 by default
          break;
        case LDIND_I1:
        case LDIND_U1:
        case LDIND_I2:
        case LDIND_U2:
        case LDIND_I4:
        case LDIND_U4:
          replace(stack, topStack, Int);
          break;
        case LDIND_I8:
          // Why is this one missing?
          //        case LDIND_U8:
          replace(stack, topStack, Long);
          break;
        case LDIND_I:
          // TODO: what is native int? how do we handle it?
          break;
        case LDIND_R4:
        case LDIND_R8:
          replace(stack, topStack, Float);
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
          clear(stack, topStack);
          clear(stack, topStack - 1);
          break;
        case ADD:
        case SUB:
        case MUL:
        case DIV:
        case DIV_UN:
        case REM:
        case REM_UN:
        case AND:
        case OR:
        case XOR:
          // Since only pairs of the same value are comparable,
          // we can be sure that the top two values are of the same type
          clear(stack, topStack); // clear 2nd operand
          setTypeByStack(types, stack, topStack - 1, pc); // use 1st operand
          break;
        case SHL:
        case SHR:
        case SHR_UN:
          clear(stack, topStack); // clear shift value
          setTypeByStack(types, stack, topStack - 1, pc);
          break;
        case NEG:
        case NOT:
          setTypeByStack(types, stack, topStack, pc);
          break;
        case CONV_I1:
        case CONV_I2:
        case CONV_I4:
        case CONV_U2:
        case CONV_U1:
        case CONV_U4:
          replace(stack, topStack, Int);
          break;
        case CONV_I8:
        case CONV_U8:
          replace(stack, topStack, Long);
          break;
        case CONV_R4:
        case CONV_R8:
          replace(stack, topStack, Float);
          break;
          // TODO: Native int again
        case CONV_I:
          break;
        case CALLVIRT:
          var methodCPtr = bytecodeBuffer.getImmToken(pc);
          topStack = handleMethod(methodCPtr, stack, topStack, module, true);

          break;
        case CPOBJ:
          // copies oboj
          break;
        case LDOBJ:
          var typePtr = bytecodeBuffer.getImmToken(pc);
          var type = module.getContext().getType(typePtr, module);
          replace(stack, topStack, type.getSystemType());
          break;
        case LDSTR:
          push(stack, topStack, Object);
          break;
        case NEWOBJ:
          var methodPtr3 = bytecodeBuffer.getImmToken(pc);
          topStack = handleCtor(methodPtr3, stack, topStack, module);
          break;
        case CASTCLASS:
          // Obj cast to Obj
          break;
        case ISINST:
          // Obj to bool
          replace(stack, topStack, Boolean);
          break;
        case CONV_R_UN:
          replace(stack, topStack, Float);
          break;
        case UNBOX:
          // TODO: pointers ??
          var typePtr2 = bytecodeBuffer.getImmToken(pc);
          var type2 = module.getContext().getType(typePtr2, module);
          setType(types, stack, topStack, pc, type2.getSystemType()); // ?? does this help at all?
          replace(stack, topStack, Int); // TODO: here its supposed to be a pointer
          break;
        case THROW:
          clear(stack, topStack);
          break;
        case LDFLD:
          FieldSymbol fld = getFieldSymbol(module, bytecodeBuffer.getImmToken(pc), pc);
          replace(stack, topStack, fld.getType().getSystemType());
          setType(types, stack, topStack, pc, fld.getType().getSystemType());
          break;
        case LDFLDA:
        case LDSFLDA:
          // TODO: pointers
          break;
        case STFLD:
          clear(stack, topStack);
          clear(stack, topStack - 1);
          break;
        case LDSFLD:
          FieldSymbol fld2 = getFieldSymbol(module, bytecodeBuffer.getImmToken(pc), pc);
          push(stack, topStack, fld2.getType().getSystemType());
          break;
        case STSFLD:
          clear(stack, topStack);
          break;
        case STOBJ:
          clear(stack, topStack);
          clear(stack, topStack - 1);
          break;
        case CONV_OVF_I1_UN:
        case CONV_OVF_I2_UN:
        case CONV_OVF_I4_UN:
        case CONV_OVF_U1_UN:
        case CONV_OVF_U2_UN:
        case CONV_OVF_U4_UN:
          replace(stack, topStack, Int);
          break;
        case CONV_OVF_I8_UN:
        case CONV_OVF_U8_UN:
          replace(stack, topStack, Long);
          break;
        case CONV_OVF_I_UN:
        case CONV_OVF_U_UN:
          // TODO: native int
          break;
        case BOX:
          replace(stack, topStack, Object);
          break;
        case NEWARR:
          replace(stack, topStack, Object);
          break;
        case LDLEN:
          // TODO: native itn (unsigned)
          replace(stack, topStack, Int);
          break;
        case LDELEMA:
          // TODO: address
          break;
        case LDELEM_I1:
        case LDELEM_I2:
        case LDELEM_I4:
        case LDELEM_U1:
        case LDELEM_U2:
        case LDELEM_U4:
          clear(stack, topStack);
          replace(stack, topStack - 1, Int);
          break;
        case LDELEM_I8:
          clear(stack, topStack);
          replace(stack, topStack - 1, Long);
          break;
          // Same as LDELEM_I8
          //        case LDELEM_U8: break;
        case LDELEM_I:
          // TODO: native int
          break;
        case LDELEM_R4:
          clear(stack, topStack);
          replace(stack, topStack - 1, Float);
          break;
        case LDELEM_R8:
          clear(stack, topStack);
          replace(stack, topStack - 1, Double);
          break;
        case LDELEM_REF:
          clear(stack, topStack);
          replace(stack, topStack - 1, Object);
          break;
        case STELEM_I:
        case STELEM_I1:
        case STELEM_I2:
        case STELEM_I4:
        case STELEM_I8:
        case STELEM_R4:
        case STELEM_R8:
        case STELEM_REF:
          clear(stack, topStack);
          clear(stack, topStack - 1);
          clear(stack, topStack - 2);
          break;
        case LDELEM:
          var elemTypePtr = bytecodeBuffer.getImmToken(pc);
          var elemType = module.getContext().getType(elemTypePtr, module);
          setType(types, stack, topStack, pc, elemType.getSystemType());
          clear(stack, topStack);
          replace(stack, topStack - 1, elemType.getSystemType());
          break;
        case STELEM:
          setTypeByStack(types, stack, topStack, pc);
          clear(stack, topStack);
          clear(stack, topStack - 1);
          clear(stack, topStack - 2);
          break;
        case UNBOX_ANY:
          var objPtr = bytecodeBuffer.getImmToken(pc);
          var objType = module.getContext().getType(objPtr, module);
          replace(stack, topStack, objType.getSystemType());
          setType(types, stack, topStack, pc, objType.getSystemType()); // not necessary
          break;
        case CONV_OVF_I1:
        case CONV_OVF_U1:
        case CONV_OVF_I2:
        case CONV_OVF_U2:
        case CONV_OVF_I4:
        case CONV_OVF_U4:
          replace(stack, topStack, Int);
          break;
        case CONV_OVF_I8:
        case CONV_OVF_U8:
          replace(stack, topStack, Long);
          break;
        case REFANYVAL:
          setTypeByStack(types, stack, topStack, pc);
          break;
        case CKFINITE:
          setTypeByStack(types, stack, topStack, pc);
          break;
        case MKREFANY:
          // TODO: pointer
          break;
        case LDTOKEN:
          break;
        case CONV_OVF_I:
        case CONV_OVF_U:
          // TODO: native int
          break;
        case ADD_OVF:
        case ADD_OVF_UN:
        case MUL_OVF:
        case MUL_OVF_UN:
        case SUB_OVF:
        case SUB_OVF_UN:
          // Since only pairs of the same value are comparable,
          // we can be sure that the top two values are of the same type
          clear(stack, topStack); // clear 2nd operand
          setTypeByStack(types, stack, topStack - 1, pc); // use 1st operand
          break;
        case ENDFAULT:
        case LEAVE:
        case LEAVE_S:
          // Nothing
          break;
          // Same as ENDFAULT
          //        case ENDFINALLY: break;
        case STIND_I:
          // TODO: native int
          clear(stack, topStack);
          setTypeByStack(types, stack, topStack - 1, pc);
          clear(stack, topStack - 1);
          break;
        case CONV_U:
          // TODO: native int
          replace(stack, topStack, Int);
          break;
          // Same as CONV_U
          //        case MAX: break;
        case PREFIXED:
          // TODO: what is this?
          break;
        case CEQ:
        case CGT:
        case CGT_UN:
        case CLT:
        case CLT_UN:
          // Since only pairs of the same value are comparable,
          // we can be sure that the top two values are of the same type
          clear(stack, topStack); // clear 2nd operand
          setTypeByStack(types, stack, topStack - 1, pc); // use 1st operand
          replace(stack, topStack - 1, Int);
          break;
        case INITOBJ:
          clear(stack, topStack);
          break;
        default:
          System.out.println("Opcode not implemented: " + curOpcode);
          break;
      }

      topStack += BytecodeInstructions.getStackEffect(curOpcode);
      pc = nextpc;
    }

    return types;
  }

  private static FieldSymbol getFieldSymbol(ModuleSymbol module, CLITablePtr fldPtr, int pc) {
    var typePtr = module.getDefiningFile().getIndicies().getFieldToClassIndex(fldPtr);
    var type = module.getContext().getType(typePtr, module);
    var fld = type.getFields()[fldPtr.getRowNo()];
    return fld;
  }

  /**
   * This function should not be necessary and therefore should be deleted. If we get the type from
   * elsewhere than stack, we can get it during the runtime just as easily.
   */
  private static void setType(
      SystemType[] types, SystemType[] stack, int topStack, int pc, SystemType systemType) {
    types[pc] = systemType;
  }

  private static int handleCtor(
      CLITablePtr token, SystemType[] stack, int topStack, ModuleSymbol module) {
    var method = getMethod(token, stack, topStack, module);
    // ctor does not have return value but puts on stack the object
    topStack = updateStackByMethod(stack, topStack, method, false);
    replace(stack, topStack - method.getParameters().length, Object);
    return topStack;
  }

  /**
   * @param topStack Points to the idx + 1 of a value on the stack we care about
   */
  private static void setTypeByStack(SystemType[] types, SystemType[] stack, int topStack, int pc) {
    types[pc] = stack[topStack - 1];
  }

  private static MethodSymbol getMethod(
      CLITablePtr token, SystemType[] stack, int topStack, ModuleSymbol module) {
    switch (token.getTableId()) {
      case CLITableConstants.CLI_TABLE_MEMBER_REF -> {
        /* Can point to method or field ref
        We can be sure that here we only have method refs */
        var row = TableRowUtils.getMemberRefRow(module, token);
        var name = row.getNameHeapPtr().read(module.getDefiningFile().getStringHeap());
        var klass = row.getKlassTablePtr();
        var signature = row.getSignatureHeapPtr().read(module.getDefiningFile().getBlobHeap());
        var methodSignature = MethodDefSig.parse(new SignatureReader(signature));
        if (klass.getTableId() == CLI_TABLE_TYPE_REF) {
          var containingType = module.getContext().getType(klass, module);
          var method = findMatchingMethod(name, methodSignature, containingType, module);
          return method;
        } else {
          CompilerDirectives.transferToInterpreter();
          throw new InterpreterException();
        }
      }
      case CLITableConstants.CLI_TABLE_METHOD_DEF -> {
        var methodDef = module.getLocalMethod(token);
        return methodDef;
      }
      case CLI_TABLE_METHOD_SPEC -> {
        // TODO: Klepitko
        var row = module.getDefiningFile().getTableHeads().getMethodSpecTableHead().skip(token);
        var instantiation =
            row.getInstantiationHeapPtr().read(module.getDefiningFile().getBlobHeap());
        throw new NotImplementedException();
      }
      default -> {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new InterpreterException();
      }
    }
  }

  private static int handleMethod(
      CLITablePtr token, SystemType[] stack, int topStack, ModuleSymbol module, boolean isVirtual) {
    var method = getMethod(token, stack, topStack, module);
    return updateStackByMethod(stack, topStack, method, isVirtual);
  }

  private static int updateStackByMethod(
      SystemType[] stack, int topStack, MethodSymbol method, boolean isVirtual) {
    // remove args from stack
    // put ret type to stack
    for (int i = 0; i < method.getParameters().length; i++) {
      clear(stack, topStack - i);
    }
    if (isVirtual) clear(stack, topStack - method.getParameters().length);

    if (method.hasReturnValue())
      replace(
          stack,
          topStack - method.getParameters().length - (isVirtual ? 1 : 0),
          method.getReturnType().getType().getSystemType());

    return topStack - method.getParameters().length + (method.hasReturnValue() ? 1 : 0);
  }

  // TODO: Klepitko this is duplciate ... ditto mehtod above
  private static MethodSymbol findMatchingMethod(
      String name,
      MethodDefSig methodSignature,
      NamedTypeSymbol definingType,
      ModuleSymbol module) {
    var parameterTypes =
        Arrays.stream(methodSignature.getParams())
            .map(
                x ->
                    TypeSymbol.TypeSymbolFactory.create(
                        x.getTypeSig(), new TypeSymbol[0], new TypeSymbol[0], module))
            .toArray(TypeSymbol[]::new);
    var returnType =
        TypeSymbol.TypeSymbolFactory.create(
            methodSignature.getRetType().getTypeSig(),
            new TypeSymbol[0],
            new TypeSymbol[0],
            module);
    var matchingMethods =
        Arrays.stream(definingType.getMethods())
            .filter(
                type ->
                    type.getName().equals(name)
                        && type.getReturnType().getType().equals(returnType)
                        && Arrays.equals(
                            Arrays.stream(type.getParameters())
                                .map(ParameterSymbol::getType)
                                .toArray(),
                            parameterTypes))
            .toArray(MethodSymbol[]::new);

    // There must be a unique method
    assert matchingMethods.length == 1;

    return matchingMethods[0];
  }

  private static void handleLoc(LocalSymbol[] locals, SystemType[] stack, int topStack, int idx) {
    var locType = locals[idx].getType().getSystemType();
    push(stack, topStack, locType);
  }

  private static void handleArg(
      ParameterSymbol[] parameters, SystemType[] stack, int topStack, int idx) {
    var argType = parameters[idx].getType().getSystemType();
    push(stack, topStack, argType);
  }

  private static void clear(SystemType[] stack, int topStack) {
    stack[topStack - 1] = None;
  }

  private static void replace(SystemType[] stack, int topStack, SystemType argSystemType) {
    stack[topStack - 1] = argSystemType;
  }

  private static void push(SystemType[] stack, int topStack, SystemType argSystemType) {
    stack[topStack] = argSystemType;
  }
}
