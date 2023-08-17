package com.vztekoverflow.cilostazol.runtime.objectmodel;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import java.util.Objects;

public enum SystemType {
  Boolean,
  Char,
  Byte,
  Int,
  Short,
  Float,
  Long,
  Double,
  Void,
  Object;

  public static SystemType getTypeKind(String name, String namespace, AssemblyIdentity assembly) {
    if (AssemblyIdentity.isStandardLib(assembly) && Objects.equals(namespace, "System")) {
      return switch (name) {
        case "Boolean" -> Boolean;
        case "Byte", "SByte" -> Byte;
        case "Char" -> Char;
        case "Int16", "UInt16" -> Short;
        case "Int32", "UInt32" -> Int;
        case "Double" -> Double;
        case "Single" -> Float;
        case "Int64", "UInt64" -> Long;
        case "Void" -> Void;
        case "UIntPtr", "IntPtr" -> Long;
        default -> Object;
      };
    }

    return Object;
  }
}
