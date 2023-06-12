package com.vztekoverflow.cilostazol.runtime.typesystem.appdomain;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.runtime.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.typesystem.assembly.IAssembly;

import java.util.HashMap;
import java.util.Map;

public class AppDomain implements IAppDomain {
    //    private final ArrayList<IAssembly> _assemblies;
    private final Map<AssemblyIdentity, IAssembly> _assemblies;
    private final CILOSTAZOLContext _ctx;

    public AppDomain(CILOSTAZOLContext ctx) {
        _ctx = ctx;
        _assemblies = new HashMap<>();
    }

    //region IAppDomain
    @Override
    public IAssembly[] getAssemblies() {
        return _assemblies.values().toArray(new IAssembly[0]);
    }

    @Override
    public IAssembly getAssembly(AssemblyIdentity identity) {
        return _assemblies.get(identity);
    }

    @Override
    public void addAssembly(IAssembly assembly) {
        assembly.setAppDomain(this);
        _assemblies.put(assembly.getIdentity(), assembly);
    }

    @Override
    public CILOSTAZOLContext getContext() {
        return _ctx;
    }
    //endregion
}
