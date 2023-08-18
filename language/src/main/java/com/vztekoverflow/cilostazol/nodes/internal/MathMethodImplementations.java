package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class MathMethodImplementations {
  public static Object MathAcos(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.acos(d);
  }

  public static Object MathAcosh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log(d + Math.sqrt(d * d - 1));
  }

  public static Object MathAsin(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.asin(d);
  }

  public static Object MathAsinh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log(d + Math.sqrt(d * d + 1));
  }

  public static Object MathAtan(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.atan(d);
  }

  public static Object MathAtanh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log((1 + d) / (1 - d)) / 2;
  }

  public static Object MathAtan2(VirtualFrame frame) {
    double y = (Double) frame.getArguments()[0];
    double x = (Double) frame.getArguments()[1];
    return Math.atan2(y, x);
  }

  public static Object MathCbrt(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.cbrt(d);
  }

  public static Object MathCeiling(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.ceil(d);
  }

  public static Object MathCos(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.cos(d);
  }

  public static Object MathCosh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.cosh(d);
  }

  public static Object MathExp(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.exp(d);
  }

  public static Object MathFloor(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.floor(d);
  }

  public static Object MathFusedMultiplyAdd(VirtualFrame frame) {
    double x = (Double) frame.getArguments()[0];
    double y = (Double) frame.getArguments()[1];
    double z = (Double) frame.getArguments()[2];
    return Math.fma(x, y, z);
  }

  public static Object MathLog(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log(d);
  }

  public static Object MathLog10(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log10(d);
  }

  public static Object MathLog2(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log(d) / Math.log(2);
  }

  public static Object MathPow(VirtualFrame frame) {
    double x = (Double) frame.getArguments()[0];
    double y = (Double) frame.getArguments()[1];
    return Math.pow(x, y);
  }

  public static Object MathSin(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.sin(d);
  }

  public static Object MathSinh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.sinh(d);
  }

  public static Object MathSqrt(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.sqrt(d);
  }

  public static Object MathTan(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.tan(d);
  }

  public static Object MathTanh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.tanh(d);
  }

  public static Object MathModF(VirtualFrame frame) {
    double x = (Double) frame.getArguments()[0];
    double y = (Double) frame.getArguments()[1];
    return Math.IEEEremainder(x, y);
  }

  public static Object MathSinCos(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return new double[] {Math.sin(d), Math.cos(d)};
  }
}
