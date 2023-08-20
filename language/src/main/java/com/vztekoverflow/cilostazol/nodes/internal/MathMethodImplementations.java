package com.vztekoverflow.cilostazol.nodes.internal;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class MathMethodImplementations {
  public static Object mathAcos(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.acos(d);
  }

  public static Object mathAcosh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log(d + Math.sqrt(d * d - 1));
  }

  public static Object mathAsin(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.asin(d);
  }

  public static Object mathAsinh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log(d + Math.sqrt(d * d + 1));
  }

  public static Object mathAtan(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.atan(d);
  }

  public static Object mathAtanh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log((1 + d) / (1 - d)) / 2;
  }

  public static Object mathAtan2(VirtualFrame frame) {
    double y = (Double) frame.getArguments()[0];
    double x = (Double) frame.getArguments()[1];
    return Math.atan2(y, x);
  }

  public static Object mathCbrt(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.cbrt(d);
  }

  public static Object mathCeiling(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.ceil(d);
  }

  public static Object mathCos(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.cos(d);
  }

  public static Object mathCosh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.cosh(d);
  }

  public static Object mathExp(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.exp(d);
  }

  public static Object mathFloor(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.floor(d);
  }

  public static Object mathFusedMultiplyAdd(VirtualFrame frame) {
    double x = (Double) frame.getArguments()[0];
    double y = (Double) frame.getArguments()[1];
    double z = (Double) frame.getArguments()[2];
    return Math.fma(x, y, z);
  }

  public static Object mathLog(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log(d);
  }

  public static Object mathLog10(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log10(d);
  }

  public static Object mathLog2(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.log(d) / Math.log(2);
  }

  public static Object mathPow(VirtualFrame frame) {
    double x = (Double) frame.getArguments()[0];
    double y = (Double) frame.getArguments()[1];
    return Math.pow(x, y);
  }

  public static Object mathSin(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.sin(d);
  }

  public static Object mathSinh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.sinh(d);
  }

  public static Object mathSqrt(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.sqrt(d);
  }

  public static Object mathTan(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.tan(d);
  }

  public static Object mathTanh(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return Math.tanh(d);
  }

  public static Object mathModF(VirtualFrame frame) {
    double x = (Double) frame.getArguments()[0];
    double y = (Double) frame.getArguments()[1];
    return Math.IEEEremainder(x, y);
  }

  public static Object mathSinCos(VirtualFrame frame) {
    double d = (Double) frame.getArguments()[0];
    return new double[] {Math.sin(d), Math.cos(d)};
  }
}
