package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.staticobject.StaticProperty;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.nodes.CILOSTAZOLFrame;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.objectmodel.StaticObject;
import com.vztekoverflow.cilostazol.runtime.symbols.ReferenceSymbol;
import java.lang.reflect.Array;

public final class IndirectLoader {
  public static int loadByte(StaticObject reference, CILOSTAZOLContext context) {
    ReferenceSymbol referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame = (Frame) context.getStackReferenceFrameProperty().getObject(reference);
        int index = context.getStackReferenceIndexProperty().getInt(reference);
        return CILOSTAZOLFrame.getLocalInt(refFrame, index);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject) context.getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty) context.getFieldReferenceFieldProperty().getObject(reference);
        return refField.getByte(refObj);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject) context.getArrayElementReferenceArrayProperty().getObject(reference);
        int index = context.getArrayElementReferenceIndexProperty().getInt(refArr);
        Object javaArr = context.getArrayProperty().getObject(refArr);
        return (byte) Array.get(javaArr, index);
      }
    }

    throw new InterpreterException("Invalid reference type");
  }

  public static int loadShort(StaticObject reference, CILOSTAZOLContext context) {
    ReferenceSymbol referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame = (Frame) context.getStackReferenceFrameProperty().getObject(reference);
        int index = context.getStackReferenceIndexProperty().getInt(reference);
        return CILOSTAZOLFrame.getLocalInt(refFrame, index);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject) context.getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty) context.getFieldReferenceFieldProperty().getObject(reference);
        return refField.getShort(refObj);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject) context.getArrayElementReferenceArrayProperty().getObject(reference);
        int index = context.getArrayElementReferenceIndexProperty().getInt(refArr);
        Object javaArr = context.getArrayProperty().getObject(refArr);
        return (short) Array.get(javaArr, index);
      }
    }

    throw new InterpreterException("Invalid reference type");
  }

  public static int loadInt32(StaticObject reference, CILOSTAZOLContext context) {
    ReferenceSymbol referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame = (Frame) context.getStackReferenceFrameProperty().getObject(reference);
        int index = context.getStackReferenceIndexProperty().getInt(reference);
        return CILOSTAZOLFrame.getLocalInt(refFrame, index);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject) context.getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty) context.getFieldReferenceFieldProperty().getObject(reference);
        return refField.getInt(refObj);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject) context.getArrayElementReferenceArrayProperty().getObject(reference);
        int index = context.getArrayElementReferenceIndexProperty().getInt(refArr);
        Object javaArr = context.getArrayProperty().getObject(refArr);
        return (int) Array.get(javaArr, index);
      }
    }

    throw new InterpreterException("Invalid reference type");
  }

  public static long loadInt64(StaticObject reference, CILOSTAZOLContext context) {
    ReferenceSymbol referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame = (Frame) context.getStackReferenceFrameProperty().getObject(reference);
        int index = context.getStackReferenceIndexProperty().getInt(reference);
        return CILOSTAZOLFrame.getLocalLong(refFrame, index);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject) context.getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty) context.getFieldReferenceFieldProperty().getObject(reference);
        return refField.getLong(refObj);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject) context.getArrayElementReferenceArrayProperty().getObject(reference);
        int index = context.getArrayElementReferenceIndexProperty().getInt(refArr);
        Object javaArr = context.getArrayProperty().getObject(refArr);
        return (long) Array.get(javaArr, index);
      }
    }

    throw new InterpreterException("Invalid reference type");
  }

  public static double loadFloat(StaticObject reference, CILOSTAZOLContext context) {
    ReferenceSymbol referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame = (Frame) context.getStackReferenceFrameProperty().getObject(reference);
        int index = context.getStackReferenceIndexProperty().getInt(reference);
        return CILOSTAZOLFrame.getLocalNativeFloat(refFrame, index);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject) context.getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty) context.getFieldReferenceFieldProperty().getObject(reference);
        return refField.getFloat(refObj);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject) context.getArrayElementReferenceArrayProperty().getObject(reference);
        int index = context.getArrayElementReferenceIndexProperty().getInt(refArr);
        Object javaArr = context.getArrayProperty().getObject(refArr);
        return (float) Array.get(javaArr, index);
      }
    }

    throw new InterpreterException("Invalid reference type");
  }

  public static double loadDouble(StaticObject reference, CILOSTAZOLContext context) {
    ReferenceSymbol referenceType = (ReferenceSymbol) reference.getTypeSymbol();
    switch (referenceType.getReferenceType()) {
      case Local, Argument -> {
        Frame refFrame = (Frame) context.getStackReferenceFrameProperty().getObject(reference);
        int index = context.getStackReferenceIndexProperty().getInt(reference);
        return CILOSTAZOLFrame.getLocalNativeFloat(refFrame, index);
      }
      case Field -> {
        StaticObject refObj =
            (StaticObject) context.getFieldReferenceObjectProperty().getObject(reference);
        StaticProperty refField =
            (StaticProperty) context.getFieldReferenceFieldProperty().getObject(reference);
        return refField.getDouble(refObj);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject) context.getArrayElementReferenceArrayProperty().getObject(reference);
        int index = context.getArrayElementReferenceIndexProperty().getInt(refArr);
        Object javaArr = context.getArrayProperty().getObject(refArr);
        return (double) Array.get(javaArr, index);
      }
    }

    throw new InterpreterException("Invalid reference type");
  }

  public static StaticObject loadRef(StaticObject reference, CILOSTAZOLContext context) {
    ReferenceSymbol referenceType = (ReferenceSymbol) reference.getTypeSymbol();
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
        return (StaticObject) refField.getObject(refObj);
      }
      case ArrayElement -> {
        StaticObject refArr =
            (StaticObject) context.getArrayElementReferenceArrayProperty().getObject(reference);
        int index = context.getArrayElementReferenceIndexProperty().getInt(refArr);
        Object javaArr = context.getArrayProperty().getObject(refArr);
        return (StaticObject) Array.get(javaArr, index);
      }
    }

    throw new InterpreterException("Invalid reference type");
  }
}
