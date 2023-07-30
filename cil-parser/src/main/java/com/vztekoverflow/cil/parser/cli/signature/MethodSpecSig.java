package com.vztekoverflow.cil.parser.cli.signature;

public class MethodSpecSig {
  private final TypeSig[] typeArgs;
  private final int genArgCount;

  private MethodSpecSig(TypeSig[] typeArgs, int genArgCount) {
    this.typeArgs = typeArgs;
    this.genArgCount = genArgCount;
  }

  public TypeSig[] getTypeArgs() {
    return typeArgs;
  }

  public int getGenArgCount() {
    return genArgCount;
  }

  public static MethodSpecSig read(SignatureReader reader) {
    final int temp = reader.getUnsigned();
    assert temp == 0x0A;
    final int genArgCount = reader.getUnsigned();
    final TypeSig[] types = new TypeSig[genArgCount];
    for (int i = 0; i < genArgCount; i++) {
      types[i] = TypeSig.read(reader);
    }

    return new MethodSpecSig(types, genArgCount);
  }
}
