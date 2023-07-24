package com.vztekoverflow.cilostazol.nodes;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.vztekoverflow.cilostazol.CILOSTAZOLBundle;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol.MethodFlags.Flag;
import com.vztekoverflow.cilostazol.runtime.symbols.TypeSymbol;
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
    return getStartArgsOffset(method) + method.getParameters().length;
  }

  public static int getStartArgsOffset(MethodSymbol methodSymbol) {
    return isInstantiable(methodSymbol) + methodSymbol.getLocals().length;
  }

  /** Instantiable methods have `this` as the first argument. */
  public static int isInstantiable(MethodSymbol methodSymbol) {
    return methodSymbol.getMethodFlags().hasFlag(Flag.STATIC) ? 0 : 1;
  }
  // endregion

  // region stack put
  public static void put(
      VirtualFrame frame, Object obj, CILOSTAZOLFrame.StackType stackType, int topStack) {
    switch (stackType) {
      case Int -> CILOSTAZOLFrame.putInt(frame, topStack, (int) obj);
      case Long -> CILOSTAZOLFrame.putLong(frame, topStack, (long) obj);
      case Double -> CILOSTAZOLFrame.putDouble(frame, topStack, (double) obj);
      case Object -> CILOSTAZOLFrame.putObject(frame, topStack, (StaticObject) obj);
      case Void -> throw new InterpreterException(
          CILOSTAZOLBundle.message("cilostazol.exception.voidStackType"));
    }
  }

  public static void putObject(Frame frame, int slot, StaticObject value) {
    assert slot >= 0;
    assert value != null;
    frame.setObjectStatic(slot, value);
  }

  public static void putInt(Frame frame, int slot, int value) {
    assert slot >= 0;
    frame.setIntStatic(slot, value);
  }

  public static void putLong(Frame frame, int slot, long value) {
    assert slot >= 0;
    frame.setLongStatic(slot, value);
  }

  public static void putDouble(Frame frame, int slot, double value) {
    assert slot >= 0;
    frame.setDoubleStatic(slot, value);
  }
  // endregion

  // region stack pop
  public static Object pop(VirtualFrame frame, CILOSTAZOLFrame.StackType stackType, int topStack) {
    return switch (stackType) {
      case Int -> CILOSTAZOLFrame.popInt(frame, topStack);
      case Long -> CILOSTAZOLFrame.popLong(frame, topStack);
      case Double -> CILOSTAZOLFrame.popDouble(frame, topStack);
      case Object -> CILOSTAZOLFrame.popObject(frame, topStack);
      case Void -> throw new InterpreterException(
          CILOSTAZOLBundle.message("cilostazol.exception.voidStackType"));
    };
  }

  public static int popInt(Frame frame, int slot) {
    assert slot >= 0;
    int result = frame.getIntStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    clearPrimitive(frame, slot);
    return result;
  }

  public static long popLong(Frame frame, int slot) {
    assert slot >= 0;
    long result = frame.getLongStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    clearPrimitive(frame, slot);
    return result;
  }

  public static double popDouble(Frame frame, int slot) {
    assert slot >= 0;
    double result = frame.getDoubleStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    clearPrimitive(frame, slot);
    return result;
  }

  public static StaticObject popObject(Frame frame, int slot) {
    assert slot >= 0;
    Object result = frame.getObjectStatic(slot);
    // Avoid keeping track of popped slots in FrameStates.
    clearPrimitive(frame, slot);
    return (StaticObject) result;
  }

  public static void pop(Frame frame, int slot, TypeSymbol type) {
    switch (type.getStackTypeKind()) {
      case Object -> {
        popObject(frame, slot);
      }
      case Int -> {
        popInt(frame, slot);
      }
      case Long -> {
        popLong(frame, slot);
      }
      case Double -> {
        popDouble(frame, slot);
      }
      case Void -> {
        throw new InterpreterException();
      }
    }
  }

  private static void clearPrimitive(Frame frame, int slot) {
    assert slot >= 0;
    frame.clearPrimitiveStatic(slot);
  }
  // endregion

  // region stack set
  public static void setLocal(
      VirtualFrame frame, Object obj, CILOSTAZOLFrame.StackType stackType, int topStack) {
    switch (stackType) {
      case Int -> CILOSTAZOLFrame.setLocalInt(frame, topStack, (int) obj);
      case Long -> CILOSTAZOLFrame.setLocalLong(frame, topStack, (long) obj);
      case Double -> CILOSTAZOLFrame.setLocalDouble(frame, topStack, (double) obj);
      case Object -> CILOSTAZOLFrame.setLocalObject(frame, topStack, (StaticObject) obj);
      case Void -> throw new InterpreterException(
          CILOSTAZOLBundle.message("cilostazol.exception.voidLocalType"));
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

  public static void setLocalDouble(Frame frame, int localSlot, double value) {
    assert localSlot >= 0;
    frame.setDoubleStatic(localSlot, value);
  }
  // endregion

  // region stack get
  public static Object getLocal(
      VirtualFrame frame, CILOSTAZOLFrame.StackType stackType, int topStack) {
    return switch (stackType) {
      case Int -> CILOSTAZOLFrame.getLocalInt(frame, topStack);
      case Long -> CILOSTAZOLFrame.getLocalLong(frame, topStack);
      case Double -> CILOSTAZOLFrame.getLocalDouble(frame, topStack);
      case Object -> CILOSTAZOLFrame.getLocalObject(frame, topStack);
      case Void -> throw new InterpreterException(
          CILOSTAZOLBundle.message("cilostazol.exception.voidLocalType"));
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

  public static double getLocalDouble(Frame frame, int localSlot) {
    assert localSlot >= 0;
    return frame.getDoubleStatic(localSlot);
  }
  // endregion

  // region stack types
  public enum StackType {
    Object,
    Int,
    Long,
    Double,
    Void,
  }

  // TODO: It should rely on Assembly as well...
  public static StackType getStackTypeKind(String name, String namespace) {
    if (Objects.equals(namespace, "System")) {
      switch (name) {
        case "Boolean":
        case "Byte":
        case "SByte":
        case "Char":
        case "Int16":
        case "UInt16":
        case "Int32":
        case "UInt32":
          return StackType.Int;
        case "Single":
        case "Double":
          return StackType.Double;
        case "Int64":
        case "UInt64":
          return StackType.Long;
          // Decimal, UIntPtr, IntPtr ??
      }
    }

    return StackType.Object;
  }
  // endregion

  public static void copyStatic(Frame frame, int sourceSlot, int destSlot) {
    assert sourceSlot >= 0 && destSlot >= 0;
    frame.copyStatic(sourceSlot, destSlot);
  }

  // region TaggedFrame
  public static TypeSymbol popTaggedStack(TypeSymbol[] taggedFrame, int top) {
    final var result = taggedFrame[top];
    taggedFrame[top] = null;
    return result;
  }

  public static void putTaggedStack(TypeSymbol[] taggedFrame, int top, TypeSymbol type) {
    assert taggedFrame[top] == null;
    taggedFrame[top] = type;
  }

  public static TypeSymbol getTaggedStack(TypeSymbol[] taggedFrame, int top) {
    assert taggedFrame[top] != null;
    return taggedFrame[top];
  }

  // endregion
}
