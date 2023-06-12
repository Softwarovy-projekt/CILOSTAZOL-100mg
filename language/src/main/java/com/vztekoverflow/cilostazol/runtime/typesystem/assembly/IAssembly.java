package com.vztekoverflow.cilostazol.runtime.typesystem.assembly;

import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cil.parser.cli.CLIFile;
import com.vztekoverflow.cilostazol.runtime.typesystem.appdomain.IAppDomain;
import com.vztekoverflow.cilostazol.runtime.typesystem.component.IComponent;
import com.vztekoverflow.cilostazol.runtime.typesystem.type.IType;

public interface IAssembly {
    //TODO: CLIFIle in IAssembly ... get rid of it, or abandon IAssembly completely?
    CLIFile getDefiningFile();

    IComponent[] getComponents();

    AssemblyIdentity getIdentity();

    void setAppDomain(IAppDomain appDomain);

    IAppDomain getAppDomain();

    IType getLocalType(String name, String namespace);
}