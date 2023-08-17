package com.vztekoverflow.cilostazol.nodes;


/** Helper methods for converting between various integer representations. */
public final class TypeHelpers {

  /** Sign-extend the 8 least significant bits of the value. */
  public static int signExtend8(long value) {
    return (byte) value;
  }

  public static int signExtend8Exact(long value) {
    if (value != (byte) value) {
      // TODO: Throw a proper exception
      throw new ArithmeticException("Value overflows when casted to Int8");
    }
    return (byte) value;
  }

  /** Sign-extend the 16 least significant bits of the value. */
  public static int signExtend16(long value) {
    return (short) value;
  }

  public static int signExtend16Exact(long value) {
    if (value != (short) value) {
      // TODO: Throw a proper exception
      throw new ArithmeticException("Value overflows when casted to Int16");
    }
    return (short) value;
  }

  /** Sign-extend the 32 least significant bits of the value. */
  public static int signExtend32(long value) {
    return (int) value;
  }

  public static int signExtend32Exact(long value) {
    return Math.toIntExact(value);
  }

  /**
   * Sign-extend the 8 least significant bits of the value to the 32 least significant bits, leaving
   * the 32 most significant bits zero.
   */
  public static long signExtend8to32(long value) {
    return truncate32(signExtend8(value));
  }

  /**
   * Sign-extend the 16 least significant bits of the value to the 32 least significant bits,
   * leaving the 32 most significant bits zero.
   */
  public static long signExtend16to32(long value) {
    return truncate32(signExtend16(value));
  }

  /** Zero-extend the 8 least significant bits of the value. */
  public static int zeroExtend8(long value) {
    return (int) (value & 0xFFL);
  }

  public static int zeroExtend8Exact(long value) {
    long extendedValue = (value & 0xFFL);
    if (extendedValue != (int) extendedValue) {
      // TODO: Throw a proper exception
      throw new ArithmeticException("Value overflows when casted to UInt8");
    }
    return (int) extendedValue;
  }

  /** Zero-extend the 16 least significant bits of the value. */
  public static int zeroExtend16(long value) {
    return (int) (value & 0xFFFFL);
  }

  public static int zeroExtend16Exact(long value) {
    long extendedValue = (value & 0xFFFFL);
    if (extendedValue != (int) extendedValue) {
      // TODO: Throw a proper exception
      throw new ArithmeticException("Value overflows when casted to UInt16");
    }
    return (int) extendedValue;
  }

  /** Zero-extend the 32 least significant bits of the value. */
  public static long zeroExtend32(long value) {
    return value & 0xFFFFFFFFL;
  }

  public static long zeroExtend32Exact(long value) {
    return zeroExtend32(value);
  }

  /** Truncate the value to the 8 least significant bits. */
  public static long truncate8(long value) {
    return value & 0xFFL;
  }

  /** Truncate the value to the 16 least significant bits. */
  public static long truncate16(long value) {
    return value & 0xFFFFL;
  }

  /** Truncate the value to the 32 least significant bits. */
  public static long truncate32(long value) {
    return value & 0xFFFFFFFFL;
  }

  public static long truncate32Exact(long value) {
    long truncatedValue = (value & 0xFFFFFFFFL);
    if (truncatedValue != (int) truncatedValue) {
      // TODO: Throw a proper exception
      throw new ArithmeticException("Value overflows when casted to Int32");
    }
    return truncatedValue;
  }
}
