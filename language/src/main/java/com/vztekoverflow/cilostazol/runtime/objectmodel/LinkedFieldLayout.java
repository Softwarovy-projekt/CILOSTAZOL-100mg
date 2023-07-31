package com.vztekoverflow.cilostazol.runtime.objectmodel;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.staticobject.StaticShape;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.symbols.FieldSymbol;
import com.vztekoverflow.cilostazol.runtime.symbols.NamedTypeSymbol;
import java.util.Map;

public final class LinkedFieldLayout {
  public final StaticShape<StaticObject.StaticObjectFactory> instanceShape;
  public final StaticShape<StaticObject.StaticObjectFactory> staticShape;

  // instance fields declared in the corresponding LinkedKlass (includes hidden fields)
  @CompilerDirectives.CompilationFinal(dimensions = 1) //
  public final StaticField[] instanceFields;
  // static fields declared in the corresponding LinkedKlass (no hidden fields)
  @CompilerDirectives.CompilationFinal(dimensions = 1) //
  public final StaticField[] staticFields;

  final int fieldTableLength;

  public LinkedFieldLayout(
      CILOSTAZOLContext description,
      NamedTypeSymbol parserTypeSymbol,
      NamedTypeSymbol superClass,
      Map<FieldSymbol, Integer> instanceFieldMapping,
      Map<FieldSymbol, Integer> staticFieldMapping) {
    StaticShape.Builder instanceBuilder = StaticShape.newBuilder(description.getLanguage());
    StaticShape.Builder staticBuilder = StaticShape.newBuilder(description.getLanguage());

    FieldCounter fieldCounter = new FieldCounter(parserTypeSymbol);
    int nextInstanceFieldIndex = 0;
    int nextStaticFieldIndex = 0;
    int nextInstanceFieldSlot = superClass == null ? 0 : superClass.getFields().length;
    int nextStaticFieldSlot = 0;

    staticFields = new StaticField[fieldCounter.staticFields];
    instanceFields = new StaticField[fieldCounter.instanceFields];

    for (FieldSymbol parserField : parserTypeSymbol.getFields()) {
      if (parserField.isStatic()) {
        createAndRegisterLinkedField(
            parserField,
            staticFieldMapping,
            nextStaticFieldSlot++,
            nextStaticFieldIndex++,
            staticBuilder,
            staticFields);
      } else {
        createAndRegisterLinkedField(
            parserField,
            instanceFieldMapping,
            nextInstanceFieldSlot++,
            nextInstanceFieldIndex++,
            instanceBuilder,
            instanceFields);
      }
    }

    if (superClass == null) {
      instanceShape =
          instanceBuilder.build(StaticObject.class, StaticObject.StaticObjectFactory.class);
    } else {
      instanceShape = instanceBuilder.build(superClass.getShape(false));
    }
    staticShape = staticBuilder.build(StaticObject.class, StaticObject.StaticObjectFactory.class);
    fieldTableLength = nextInstanceFieldSlot;
  }

  private static void createAndRegisterLinkedField(
      FieldSymbol parserField,
      Map<FieldSymbol, Integer> fieldMapping,
      int slot,
      int index,
      StaticShape.Builder builder,
      StaticField[] linkedFields) {
    StaticField field = new StaticField(parserField);
    builder.property(field, field.getPropertyType(), storeAsFinal(parserField));
    linkedFields[index] = field;
    fieldMapping.put(parserField, index);
  }

  private static boolean storeAsFinal(FieldSymbol field) {
    return field.isLiteral();
  }

  private static final class FieldCounter {
    // Includes hidden fields
    final int instanceFields;
    final int staticFields;

    FieldCounter(NamedTypeSymbol parserTypeSymbol) {
      int iFields = 0;
      int sFields = 0;
      for (FieldSymbol f : parserTypeSymbol.getFields()) {
        if (f.isStatic()) {
          sFields++;
        } else {
          iFields++;
        }
      }

      instanceFields = iFields;
      staticFields = sFields;
    }
  }
}
