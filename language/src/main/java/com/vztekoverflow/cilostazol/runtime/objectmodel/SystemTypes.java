package com.vztekoverflow.cilostazol.runtime.objectmodel;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import java.util.Objects;

public enum SystemTypes {
  Boolean,
  Char,
  Int,
  Float,
  Long,
  Double,
  Void,
  Object,
  Array; // TODO: Klepitko -> array bude jako objekt

  public static SystemTypes getTypeKind(String name, String namespace, AssemblyIdentity assembly) {
    if (AssemblyIdentity.SystemPrivateCoreLib700().equalsVersionAgnostic(assembly)
        && Objects.equals(namespace, "System")) {
      switch (name) {
        case "Boolean":
          return SystemTypes.Boolean;
        case "Byte":
        case "SByte":
        case "Char":
          return SystemTypes.Char;
        case "Int16":
        case "UInt16":
        case "Int32":
        case "UInt32":
          return SystemTypes.Int;
        case "Double":
          return SystemTypes.Double;
        case "Single":
          return SystemTypes.Float;
        case "Int64":
        case "UInt64":
          return SystemTypes.Long;
        case "Void":
          return SystemTypes.Void;
          // Decimal, UIntPtr, IntPtr ??
      }
    }

    return SystemTypes.Object;
  }
}
