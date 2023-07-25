package com.vztekoverflow.cilostazol.runtime.context;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;

/**
 * A class that can be used to forward assemblies to other versions. @Note: Some types are defined
 * in assembly A, but their implementation is in assembly B. Since we do not support forwarders, we
 * need to forward these types manually.
 */
public class AssemblyForwarder {

  public static AssemblyIdentity forwardedAssembly(AssemblyIdentity assemblyIdentity) {
    if (assemblyIdentity.equalsVersionAgnostic(AssemblyIdentity.SystemRuntimeLib700()))
      return AssemblyIdentity.SystemPrivateCoreLib700();

    return assemblyIdentity;
  }
}
