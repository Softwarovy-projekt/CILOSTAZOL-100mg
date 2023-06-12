package com.vztekoverflow.cilostazol.runtime;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;
import com.vztekoverflow.cil.parser.cli.AssemblyIdentity;
import com.vztekoverflow.cilostazol.CILOSTAZOLEngineOption;
import com.vztekoverflow.cilostazol.CILOSTAZOLLanguage;
import com.vztekoverflow.cilostazol.meta.Meta;
import com.vztekoverflow.cilostazol.runtime.cache.CachingTypeSignature;
import com.vztekoverflow.cilostazol.runtime.typesystem.appdomain.AppDomain;
import com.vztekoverflow.cilostazol.runtime.typesystem.assembly.Assembly;
import com.vztekoverflow.cilostazol.runtime.typesystem.assembly.IAssembly;
import com.vztekoverflow.cilostazol.runtime.typesystem.type.IType;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.ByteSequence;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CILOSTAZOLContext {
    private final Path[] _libraryPaths;
    private final CILOSTAZOLLanguage _language;
    private final TruffleLanguage.Env _env;
    private final AppDomain _appDomain = new AppDomain(this);
    private final Map<CachingTypeSignature, IType> _typeCache = new HashMap<>();

    public static final TruffleLanguage.ContextReference<CILOSTAZOLContext> CONTEXT_REF = TruffleLanguage.ContextReference.create(CILOSTAZOLLanguage.class);

    @CompilerDirectives.CompilationFinal
    private Meta meta;

    public CILOSTAZOLContext(CILOSTAZOLLanguage lang, TruffleLanguage.Env env) {
        _language = lang;
        _env = env;
        getLanguage().initializeGuestAllocator(env);
        _libraryPaths = Arrays.stream(CILOSTAZOLEngineOption.getPolyglotOptionSearchPaths(env)).filter(p -> {
            TruffleFile file = getEnv().getInternalTruffleFile(p.toString());
            return file.isDirectory();
        }).distinct().toArray(Path[]::new);
    }

    //For test propose only
    public CILOSTAZOLContext(CILOSTAZOLLanguage lang, Path[] libraryPaths) {
        _language = lang;
        _env = null;
        _libraryPaths = libraryPaths;
    }

    public IAssembly findAssembly(AssemblyIdentity assemblyIdentity) {
        //Loading assemblies is an expensive task which should be never compiled
        CompilerAsserts.neverPartOfCompilation();

        //TODO: resolve and load PrimitiveTypes

        //Locate dlls in paths

        for (Path path : _libraryPaths) {
            File file = new File(path.toString() + "/" + assemblyIdentity.getName() + ".dll");
            if (file.exists()) {
                try {
                    return loadAssembly(Source.newBuilder(
                            CILOSTAZOLLanguage.ID,
                            ByteSequence.create(Files.readAllBytes(file.toPath())),
                            file.getName()).build());
                } catch (Exception e) {
                    throw new RuntimeException("Error loading assembly " + assemblyIdentity.getName() + " from " + path.toString(), e);
                }
            }
        }
        return null;
    }

    public IAssembly loadAssembly(Source source) {
        return Assembly.parse(source, _appDomain);
    }

    public CILOSTAZOLLanguage getLanguage() {
        return _language;
    }

    public Meta getMeta() {
        return meta;
    }

    public GuestAllocator getAllocator() {
        return getLanguage().getAllocator();
    }

    public TruffleLanguage.Env getEnv() {
        return _env;
    }

    public IType getType(String name, String namespace, AssemblyIdentity assemblyIdentity) {
        var signature = new CachingTypeSignature(name, namespace, assemblyIdentity);

        return _typeCache.computeIfAbsent(signature, s -> materializeType(name, namespace, assemblyIdentity));
    }

    //REVIEW: I propose to rename all subsequent methods "getLocalType" to "materializeType" to better reflect what they do
    private IType materializeType(String name, String namespace, AssemblyIdentity assemblyIdentity) {
        if (namespace.startsWith("System"))
            return null;

        var assembly = _appDomain.getAssembly(assemblyIdentity);
        if (assembly == null) {
            assembly = findAssembly(assemblyIdentity);
            if (assembly == null)
                throw new RuntimeException("Assembly " + assemblyIdentity.getName() + " not found");
        }

        return assembly.getLocalType(name, namespace);
    }

    public Path[] getLibsPaths() {
        return _libraryPaths;
    }

    public void setBootstrapMeta(Meta meta) {
        this.meta = meta;
    }

    public static CILOSTAZOLContext get(Node node) {
        return CONTEXT_REF.get(node);
    }
}
