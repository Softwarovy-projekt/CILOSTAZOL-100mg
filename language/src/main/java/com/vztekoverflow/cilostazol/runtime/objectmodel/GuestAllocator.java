package com.vztekoverflow.cilostazol.runtime.objectmodel;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.vztekoverflow.cilostazol.CILOSTAZOLLanguage;
import com.vztekoverflow.cilostazol.exceptions.InstantiationError;
import com.vztekoverflow.cilostazol.exceptions.InstantiationException;
import com.vztekoverflow.cilostazol.exceptions.InterpreterException;
import com.vztekoverflow.cilostazol.exceptions.NegativeArraySizeException;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.other.SymbolResolver;
import com.vztekoverflow.cilostazol.runtime.symbols.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class GuestAllocator {
  private final CILOSTAZOLLanguage language;
  private final AllocationReporter allocationReporter;
  private final Map<String, StaticObject> stringCache = new HashMap<>();

  public GuestAllocator(CILOSTAZOLLanguage language, AllocationReporter allocationReporter) {
    this.language = language;
    this.allocationReporter = allocationReporter;
    if (allocationReporter != null) {
      // Can be already active, in which case the active value change notification is missed.
      if (allocationReporter.isActive()) {
        language.invalidateAllocationTrackingDisabled();
      }
      allocationReporter.addActiveListener(
          (isActive) -> {
            if (isActive) {
              language.invalidateAllocationTrackingDisabled();
            }
          });
    }
  }

  private static void initInstanceFields(StaticObject obj, NamedTypeSymbol typeSymbol) {
    if (CompilerDirectives.isPartialEvaluationConstant(typeSymbol)) {
      initLoop(obj, typeSymbol);
    } else {
      initLoopNoExplode(obj, typeSymbol);
    }
  }

  @ExplodeLoop
  private static void initLoop(StaticObject obj, NamedTypeSymbol typeSymbol) {
    for (StaticField f : typeSymbol.getInstanceFields()) {
      assert !f.isStatic();
      if (f.getKind() == SystemType.Object) {
        f.setObjectValue(obj, StaticObject.NULL);
      }
    }
  }

  private static void initLoopNoExplode(StaticObject obj, NamedTypeSymbol typeSymbol) {
    for (StaticField f : typeSymbol.getInstanceFields()) {
      assert !f.isStatic();
      if (f.getKind() == SystemType.Object) {
        f.setObjectValue(obj, StaticObject.NULL);
      }
    }
  }

  private static StaticObject trackAllocation(
      NamedTypeSymbol typeSymbol, StaticObject obj, CILOSTAZOLLanguage lang, Symbol symbol) {
    if (typeSymbol == null || lang.isAllocationTrackingDisabled()) {
      return obj;
    }
    if (!CompilerDirectives.isPartialEvaluationConstant(symbol)) {
      return trackAllocationBoundary(symbol, obj);
    }
    return symbol.getContext().getAllocator().trackAllocation(obj);
  }

  @CompilerDirectives.TruffleBoundary
  private static StaticObject trackAllocationBoundary(Symbol symbol, StaticObject obj) {
    return symbol.getContext().getAllocator().trackAllocation(obj);
  }

  /**
   * Allocates a new instance of the given class; does not call any constructor. Initializes the
   * class.
   *
   * @param typeSymbol The typeSymbol of the reference to allocate. If it is PE-constant, the field
   *     initialization loop can be exploded. This is expected to be the case when executing the
   *     {@code NEW} bytecode, but may not be the case always.
   */
  public StaticObject createNew(NamedTypeSymbol typeSymbol) {
    assert AllocationChecks.canAllocateNewReference(typeSymbol);
    typeSymbol.safelyInitialize();
    StaticObject newObj = typeSymbol.getShape(false).getFactory().create(typeSymbol);
    initInstanceFields(newObj, typeSymbol);
    return trackAllocation(typeSymbol, newObj);
  }

  // TODO: This might take constructors into consideration
  public StaticObject createClass(NamedTypeSymbol typeSymbol) {
    return createNew(typeSymbol);
  }

  private StaticObject trackAllocation(NamedTypeSymbol typeSymbol, StaticObject obj) {
    return trackAllocation(typeSymbol, obj, language, typeSymbol);
  }

  public <T> T trackAllocation(T object) {
    if (allocationReporter != null) {
      CompilerAsserts.partialEvaluationConstant(allocationReporter);
      allocationReporter.onEnter(null, 0, AllocationReporter.SIZE_UNKNOWN);
      allocationReporter.onReturnValue(object, 0, AllocationReporter.SIZE_UNKNOWN);
    }
    return object;
  }

  // region array creation
  public StaticObject createNewPrimitiveArray(ArrayTypeSymbol arrayType, int length) {
    var elementType = (NamedTypeSymbol) arrayType.getElementType();
    Object arr;
    switch (elementType.getSystemType()) {
      case Boolean -> {
        arr = new boolean[length];
      }
      case Char -> {
        arr = new char[length];
      }
      case Short -> {
        arr = new short[length];
      }
      case Int -> {
        arr = new int[length];
      }
      case Float -> {
        arr = new float[length];
      }
      case Long -> {
        arr = new long[length];
      }
      case Double -> {
        arr = new double[length];
      }
      default -> {
        throw new InterpreterException();
      }
    }

    return wrapArrayAs(arrayType, arr);
  }

  public StaticObject createNewReferenceArray(ArrayTypeSymbol arrayType, int length) {
    StaticObject[] arr = new StaticObject[length];
    Arrays.fill(arr, StaticObject.NULL);
    return wrapArrayAs(arrayType, arr);
  }

  public StaticObject createNewMultiArray(ArrayTypeSymbol arrayType, int[] dimensions) {
    int elemCount = 0;
    for (int i = 0; i < dimensions.length; i++) {
      elemCount += dimensions[i];
    }

    if (arrayType.getElementType().getSystemType() != SystemType.Object)
      return createNewPrimitiveArray(arrayType, elemCount);
    else return createNewReferenceArray(arrayType, elemCount);
  }

  public StaticObject wrapArrayAs(ArrayTypeSymbol typeSymbol, Object array) {
    StaticObject newObj = typeSymbol.getContext().getArrayShape().getFactory().create(typeSymbol);
    typeSymbol.getContext().getArrayProperty().setObject(newObj, array);
    return trackAllocation(newObj);
  }
  // endregion

  // region references creation
  public StaticObject createStackReference(ReferenceSymbol reference, Frame frame, int index) {
    StaticObject newRef =
        reference.getContext().getStackReferenceShape().getFactory().create(reference);
    reference.getContext().getStackReferenceFrameProperty().setObject(frame, newRef);
    reference.getContext().getStackReferenceIndexProperty().setObject(frame, index);
    return newRef;
  }

  public StaticObject createFieldReference(
      ReferenceSymbol reference, StaticObject referent, StaticField field) {
    StaticObject newRef =
        reference.getContext().getFieldReferenceShape().getFactory().create(reference);
    reference.getContext().getFieldReferenceFieldProperty().setObject(field, newRef);
    reference.getContext().getFieldReferenceObjectProperty().setObject(referent, newRef);
    return newRef;
  }

  public StaticObject createArrayElementReference(
      ReferenceSymbol reference, StaticObject array, int elemIndex) {
    StaticObject newRef =
        reference.getContext().getArrayElementReferenceShape().getFactory().create(reference);
    reference.getContext().getArrayElementReferenceArrayProperty().setObject(array, newRef);
    reference.getContext().getArrayElementReferenceIndexProperty().setObject(elemIndex, newRef);
    return newRef;
  }
  // endregion

  // region string creation
  public StaticObject createString(String value) {
    return stringCache.computeIfAbsent(
        value,
        k -> {
          final var ctx = CILOSTAZOLContext.CONTEXT_REF.get(null);
          final var stringChar = k.toCharArray();

          final var stringType = SymbolResolver.getString(ctx);
          final var charType = SymbolResolver.getChar(CILOSTAZOLContext.CONTEXT_REF.get(null));
          final var charArrayType =
              ArrayTypeSymbol.ArrayTypeSymbolFactory.create(charType, charType.getDefiningModule());
          final var charArray = createNewPrimitiveArray(charArrayType, stringChar.length);
          ctx.getArrayProperty().setObject(charArray, stringChar);

          final var result = createNew(SymbolResolver.getString(ctx));
          stringType.getInstanceFields()[0].setInt(result, stringChar.length);
          stringType.getInstanceFields()[1].setObject(result, charArray);
          return result;
        });
  }
  // endregion

  public interface AllocationProfiler {
    AllocationProfiler NO_PROFILE =
        new AllocationProfiler() {
          @Override
          public void enterNewReference() {}

          @Override
          public void enterNewArray() {}

          @Override
          public void enterNewMultiArray() {}
        };

    void enterNewReference();

    void enterNewArray();

    void enterNewMultiArray();
  }

  public static final class AllocationChecks {
    private AllocationChecks() {}

    public static void checkCanAllocateNewReference(NamedTypeSymbol typeSymbol, boolean error) {
      checkCanAllocateNewReference(typeSymbol, error, AllocationProfiler.NO_PROFILE);
    }

    public static void checkCanAllocateArray(int size) {
      checkCanAllocateArray(size, AllocationProfiler.NO_PROFILE);
    }

    public static void checkCanAllocateNewReference(
        NamedTypeSymbol typeSymbol, boolean error, AllocationProfiler profile) {
      if (!canAllocateNewReference(typeSymbol)) {
        profile.enterNewReference();
        throw error ? new InstantiationError() : new InstantiationException();
      }
    }

    public static void checkCanAllocateArray(int size, AllocationProfiler profile) {
      if (!canAllocateNewArray(size)) {
        profile.enterNewArray();
        throw new NegativeArraySizeException();
      }
    }

    private static boolean canAllocateNewReference(NamedTypeSymbol type) {
      return !type.isAbstract() && !type.isInterface();
    }

    private static boolean canAllocateNewArray(int size) {
      return size >= 0;
    }

    private static boolean invalidDimensionsArray(int[] dimensions) {
      return dimensions.length == 0 || dimensions.length > 255;
    }

    @ExplodeLoop
    private static boolean invalidDimensions(int[] dimensions) {
      if (CompilerDirectives.isPartialEvaluationConstant(dimensions)) {
        return invalidDimensionsExplode(dimensions);
      } else {
        return invalidDimensionsNoExplode(dimensions);
      }
    }

    private static boolean invalidDimensionsNoExplode(int[] dimensions) {
      for (int dim : dimensions) {
        if (!canAllocateNewArray(dim)) {
          return true;
        }
      }
      return false;
    }

    @ExplodeLoop
    private static boolean invalidDimensionsExplode(int[] dimensions) {
      for (int dim : dimensions) {
        if (!canAllocateNewArray(dim)) {
          return true;
        }
      }
      return false;
    }
  }
}
