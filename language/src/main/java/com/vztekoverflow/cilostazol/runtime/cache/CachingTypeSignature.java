package com.vztekoverflow.cilostazol.runtime.cache;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;

//TODO: add type and generic parameters
public class CachingTypeSignature {
    private final String name;
    private final String namespace;
    private final AssemblyIdentity assemblyIdentity;

    public CachingTypeSignature(String name, String namespace, AssemblyIdentity assemblyIdentity) {
        this.name = name;
        this.namespace = namespace;
        this.assemblyIdentity = assemblyIdentity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CachingTypeSignature other) {
            return name.equals(other.name)
                    && namespace.equals(other.namespace)
                    && assemblyIdentity.equals(other.assemblyIdentity);
        }
        return false;
    }
}
