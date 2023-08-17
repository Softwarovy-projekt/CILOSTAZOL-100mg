package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.staticobject.StaticProperty;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol.MethodFlags.Flag;
import com.vztekoverflow.cilostazol.runtime.symbols.ReferenceSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
import java.lang.reflect.Array;
import java.util.Objects;

public final class CILOSTAZOLFrame {
  public static FrameDescriptor create(int params, int locals, int stack) {
    int slotCount = params + locals + stack;
    FrameDescriptor.Builder builder = FrameDescriptor.newBuilder(slotCount);
    builder.addSlots(slotCount, FrameSlotKind.Static);
    /*
    Frame slots using this kind cannot be changed to another kind later on.
    Static frame slots can simultaneously hold one primitive and one object value.
    Static frame slots are intended for situations where the type of variable in frame
    slots is known ahead of time and does not need any type checks.
    Static frame slots are intended for situations where the type of variable in a frame
    slots is known ahead-of-time and does not need any type checks (for example,
    in statically typed languages).
     */
    return builder.build();
  }

  // region stack offsets
  public static int getStartStackOffset(MethodSymbol method) {
    return getStartArgsOffset(method) + method.getParameterCountIncludingInstance();
  }

  public static int getStartArgsOffset(MethodSymbol methodSymbol) {
    return methodSymbol.getLocals().length;
  }

  /** Instantiable methods have `this` as the first argument. */
  public static int isInstantiable(MethodSymbol methodSymbol) {
    return methodSymbol.getMethodFlags().hasFlag(Flag.STATIC) ? 0 : 1;
  }
  // endregion

  // region stack put
  public static void put(VirtualFrame frame, Object obj, int topStack, TypeSymbol typeSymbol) {
    put(frame, obj, topStack, typeSymbol.getStackTypeKind());
  }

  public static void put(
      VirtualFrame frame, Object obj, int topStack, CILOSTAZOLFrame.StackType stackType) {
    switch (stackType) {
      case Int32 -> CILOSTAZOLFrame.putInt32(frame, topStack, (int) obj);
      case Int64 -> CILOSTAZOLFrame.putInt64(frame, topStack, (long) obj);
      case NativeFloat -> CILOSTAZOLFrame.putNativeFloat(frame, topStack, (double) obj);
      case NativeInt -> CILOSTAZOLFrame.putInt32(frame, topStack, (int) obj);
      case ManagedPointer -> CILOSTAZOLFrame.putInt32(frame, topStack, (int) obj);
      case Object -> CILOSTAZOLFrame.putObject(frame, topStack, (StaticObject) obj);
      default -> throw new InterpreterException(
          CILOSTAZOLBundle.message("cilostazol.exception.unknown.type.on.stack", stackType));
    }
  }

  public static void putObject(Frame frame, int slot, StaticObject value) {
    assert slot >= 0;
    assert value != null;
    frame.setObjectStatic(slot, value);
  }

  public static void putInt32(Frame frame, int slot, int value) {
    assert slot >= 0;
    frame.setIntStatic(slot, value);
  }

  public static void putInt64(Frame frame, int slot, long value) {
    assert slot >= 0;
    frame.setLongStatic(slot, value);
  }

  public static void putNativeFloat(Frame frame, int slot, double value) {
    assert slot >= 0;
    frame.setDoubleStatic(slot, value);
  }

  public static void putNativeInt(Frame frame, int slot, int value) {
    putInt32(frame, slot, value);
  }
  // endregion

  // region stack pop
  public static Object pop(VirtualFrame frame, int topStack, TypeSymbol typeSymbol) {
    return pop(frame, topStack, typeSymbol.getStackTypeKind());
  }

  public static Object pop(VirtualFrame frame, int topStack, CILOSTAZOLFrame.StackType stackType) {
    return switch (stackType) {
      case Int32 -> popInt32(frame, topStack);
      case Int64 -> popInt64(frame, topStack);
      case NativeFloat -> popNativeFloat(frame, topStack);
      case NativeInt -> popInt32(frame, topStack);
      case ManagedPointer -> popInt32(frame, topStack);
      case Object -> popObject(frame, topStack);
      default -> throw new InterpreterException(
          CILOSTAZOLBundle.message("cilostazol.exception.unknown.type.on.stack", stackType));
    };
  }

  public static int popInt32(Frame frame, int slot) {
    assert slot >= 0;
    int result = frame.getIntStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    clearPrimitive(frame, slot);
    return result;
  }

  public static long popInt64(Frame frame, int slot) {
    assert slot >= 0;
    long result = frame.getLongStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    clearPrimitive(frame, slot);
    return result;
  }

  public static double popNativeFloat(Frame frame, int slot) {
    assert slot >= 0;
    double result = frame.getDoubleStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    clearPrimitive(frame, slot);
    return result;
  }

  public static int popNativeInt(Frame frame, int slot) {
    assert slot >= 0;
    int result = frame.getIntStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    clearPrimitive(frame, slot);
    return result;
  }

  public static StaticObject popObject(Frame frame, int slot) {
    assert slot >= 0;
    Object result = frame.getObjectStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    frame.clearObjectStatic(slot);
    return (StaticObject) result;
  }

  public static Object popObjectFromPossibleReference(
      VirtualFrame frame, int slot, CILOSTAZOLContext context) {
    StaticObject staticObject = popObject(frame, slot);
    TypeSymbol type = staticObject.getTypeSymbol();
    if (type instanceof ReferenceSymbol) {
      return getObjectFromReferenceBasedOnReferenceType(
          context, (ReferenceSymbol) type, staticObject);
    }

    return staticObject;
  }

  public static Object popObjectFromPossibleReference(
      VirtualFrame frame, TypeSymbol type, int slot, CILOSTAZOLContext context) {
    if (type instanceof ReferenceSymbol) {
      return getObjectFromReference(frame, slot, context);
    }

    return popObject(frame, slot);
  }

  private static void clearPrimitive(Frame frame, int slot) {
    assert slot >= 0;
    frame.clearPrimitiveStatic(slot);
  }
  // endregion

  // region reference fetch

  public static Object getObjectFromReference(
      VirtualFrame frame, int top, CILOSTAZOLContext context) {
    var reference = CILOSTAZOLFrame.popObject(frame, top - 1);
    var referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    return getObjectFromReferenceBasedOnReferenceType(context, referenceType, reference);
  }

  private static Object getObjectFromReferenceBasedOnReferenceType(
      CILOSTAZOLContext context, ReferenceSymbol referenceType, StaticObject reference) {
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame = (Frame) context.getStackReferenceFrameProperty().getObject(reference);
        int index = context.getStackReferenceIndexProperty().getInt(reference);
        return CILOSTAZOLFrame.getLocalObject(refFrame, index);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject) context.getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty) context.getFieldReferenceFieldProperty().getObject(reference);
        return refField.getObject(refObj);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject) context.getArrayElementReferenceArrayProperty().getObject(reference);
        int index = context.getArrayElementReferenceIndexProperty().getInt(refArr);
        var javaArr = context.getArrayProperty().getObject(refArr);
        return Array.get(javaArr, index);
      }
    }

    throw new InterpreterException(
        CILOSTAZOLBundle.message("cilostazol.exception.unknown.reference.type"));
  }

  // endregion

  // region stack set
  public static void setLocal(VirtualFrame frame, Object obj, int topStack, TypeSymbol typeSymbol) {
    setLocal(frame, obj, topStack, typeSymbol.getStackTypeKind());
  }

  public static void setLocal(
      VirtualFrame frame, Object obj, int topStack, CILOSTAZOLFrame.StackType stackType) {
    switch (stackType) {
      case Int32 -> CILOSTAZOLFrame.setLocalInt(frame, topStack, (int) obj);
      case Int64 -> CILOSTAZOLFrame.setLocalLong(frame, topStack, (long) obj);
      case NativeFloat -> CILOSTAZOLFrame.setLocalNativeFloat(frame, topStack, (double) obj);
      case NativeInt -> CILOSTAZOLFrame.setLocalInt(frame, topStack, (int) obj);
      case ManagedPointer -> CILOSTAZOLFrame.setLocalInt(frame, topStack, (int) obj);
      case Object -> CILOSTAZOLFrame.setLocalObject(frame, topStack, (StaticObject) obj);
      default -> throw new InterpreterException(
          CILOSTAZOLBundle.message("cilostazol.exception.unknown.type.on.stack", stackType));
    }
  }

  public static void setLocalObject(Frame frame, int localSlot, StaticObject value) {
    assert localSlot >= 0;
    assert value != null;
    frame.setObjectStatic(localSlot, value);
  }

  public static void setLocalInt(Frame frame, int localSlot, int value) {
    assert localSlot >= 0;
    frame.setIntStatic(localSlot, value);
  }

  public static void setLocalLong(Frame frame, int localSlot, long value) {
    assert localSlot >= 0;
    frame.setLongStatic(localSlot, value);
  }

  public static void setLocalNativeFloat(Frame frame, int localSlot, double value) {
    assert localSlot >= 0;
    frame.setDoubleStatic(localSlot, value);
  }
  // endregion

  // region stack get
  public static Object getLocal(VirtualFrame frame, int topStack, TypeSymbol typeSymbol) {
    return getLocal(frame, topStack, typeSymbol.getStackTypeKind());
  }

  public static Object getLocal(
      VirtualFrame frame, int topStack, CILOSTAZOLFrame.StackType stackType) {
    return switch (stackType) {
      case Int32 -> CILOSTAZOLFrame.getLocalInt(frame, topStack);
      case Int64 -> CILOSTAZOLFrame.getLocalLong(frame, topStack);
      case NativeFloat -> CILOSTAZOLFrame.getLocalNativeFloat(frame, topStack);
      case NativeInt -> CILOSTAZOLFrame.getLocalInt(frame, topStack);
      case ManagedPointer -> CILOSTAZOLFrame.getLocalInt(frame, topStack);
      case Object -> CILOSTAZOLFrame.getLocalObject(frame, topStack);
      default -> throw new InterpreterException(
          CILOSTAZOLBundle.message("cilostazol.exception.unknown.type.on.stack", stackType));
    };
  }

  public static int getLocalInt(Frame frame, int localSlot) {
    assert localSlot >= 0;
    return frame.getIntStatic(localSlot);
  }

  public static StaticObject getLocalObject(Frame frame, int localSlot) {
    assert localSlot >= 0;
    Object result = frame.getObjectStatic(localSlot);
    assert result != null;
    return (StaticObject) result;
  }

  public static long getLocalLong(Frame frame, int localSlot) {
    assert localSlot >= 0;
    return frame.getLongStatic(localSlot);
  }

  public static double getLocalNativeFloat(Frame frame, int localSlot) {
    assert localSlot >= 0;
    return frame.getDoubleStatic(localSlot);
  }
  // endregion

  public static void clearEvaluationStack(Frame frame, int top, MethodSymbol method) {
    int evaluationStackStart = getStartStackOffset(method);
    while (evaluationStackStart < top) {
      frame.clear(top);
      top--;
    }
  }

  public static StackType getStackTypeKind(
      String name, String namespace, AssemblyIdentity assembly) {
    if (AssemblyIdentity.isStandardLib(assembly) && Objects.equals(namespace, "System")) {
      switch (name) {
        case "Boolean":
        case "Byte":
        case "SByte":
        case "Char":
        case "Int16":
        case "UInt16":
        case "Int32":
        case "UInt32":
          return StackType.Int32;
        case "Single":
        case "Double":
          return StackType.NativeFloat;
        case "Int64":
        case "UInt64":
          return StackType.Int64;
          // Decimal, UIntPtr, IntPtr ??
      }
    }

    return StackType.Object;
  }

  public static void copyStatic(Frame frame, int sourceSlot, int destSlot) {
    assert sourceSlot >= 0 && destSlot >= 0;
    frame.copyStatic(sourceSlot, destSlot);
  }

  public static void moveValueStatic(Frame frame, int sourceSlot, int destSlot) {
    assert sourceSlot >= 0 && destSlot >= 0;
    frame.copyStatic(sourceSlot, destSlot);
    frame.clearStatic(sourceSlot);
  }

  // region stack types
  public enum StackType {
    Object,
    Int32,
    Int64,
    NativeFloat,
    NativeInt,
    ManagedPointer,
  }
  // endregion
}
