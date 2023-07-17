package com.vztekoverflow.cilostazol;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.nodes.Node;
import com.vztekoverflow.cilostazol.nodes.CallEntryPointCallTarget;
import com.vztekoverflow.cilostazol.runtime.context.CILOSTAZOLContext;
import com.vztekoverflow.cilostazol.runtime.other.GuestAllocator;
import com.vztekoverflow.cilostazol.runtime.symbols.MethodSymbol;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.graalvm.options.*;
import org.graalvm.polyglot.Source;

/** The BACIL language class implementing TruffleLanguage. */
@TruffleLanguage.Registration(
    id = CILOSTAZOLLanguage.ID,
    name = CILOSTAZOLLanguage.NAME,
    interactive = false,
    defaultMimeType = CILOSTAZOLLanguage.CIL_PE_MIME_TYPE,
    byteMimeTypes = {CILOSTAZOLLanguage.CIL_PE_MIME_TYPE})
public class CILOSTAZOLLanguage extends TruffleLanguage<CILOSTAZOLContext> {

  public static final String ID = "cil";
  public static final String NAME = "CIL";
  public static final String CIL_PE_MIME_TYPE = "application/x-dosexec";
  private static final LanguageReference<CILOSTAZOLLanguage> REFERENCE =
      LanguageReference.create(CILOSTAZOLLanguage.class);

  @CompilerDirectives.CompilationFinal
  private final Assumption noAllocationTracking =
      Assumption.create("No allocation tracking assumption");

  @CompilerDirectives.CompilationFinal private GuestAllocator allocator;

  public static CILOSTAZOLLanguage get(Node node) {
    return REFERENCE.get(node);
  }

  @Override
  protected OptionDescriptors getOptionDescriptors() {
    return new MyOptionDescriptors();
  }

  public final class MyOptionDescriptors implements OptionDescriptors {
    public static final String LIBRARY_PATH_NAME = "cil.libraryPath";
    public static final String OPTION_ARRAY_SEPARATOR = ";";
    public static final OptionKey<String> LIBRARY_PATH = new OptionKey<>("");

    @Override
    public OptionDescriptor get(String optionName) {
      if (optionName.equals("cil.libraryPath")) {
        return OptionDescriptor.newBuilder(LIBRARY_PATH, "cil.libraryPath")
            .deprecated(false)
            .help(
                "A list of paths where CILOSTAZOL will search for relative libraries. Paths are delimited by a semicolon ';'.")
            .usageSyntax("")
            .category(OptionCategory.USER)
            .stability(OptionStability.STABLE)
            .build();
      }
      return null;
    }

    @Override
    public Iterator<OptionDescriptor> iterator() {
      return List.of(
              OptionDescriptor.newBuilder(LIBRARY_PATH, "cil.libraryPath")
                  .deprecated(false)
                  .help(
                      "A list of paths where CILOSTAZOL will search for relative libraries. Paths are delimited by a semicolon ';'.")
                  .usageSyntax("")
                  .category(OptionCategory.USER)
                  .stability(OptionStability.STABLE)
                  .build())
          .iterator();
    }

    public static Path[] getPolyglotOptionSearchPaths(TruffleLanguage.Env env) {
      if (env.getOptions().getDescriptors().get(LIBRARY_PATH_NAME) == null)
        return new Path[] {Paths.get(".")};
      else {
        OptionDescriptor desc = env.getOptions().getDescriptors().get(LIBRARY_PATH_NAME);
        String libraryPathOption = env.getOptions().get((OptionKey<String>) desc.getKey());
        String[] libraryPaths =
            "".equals(libraryPathOption)
                ? new String[0]
                : libraryPathOption.split(OPTION_ARRAY_SEPARATOR);
        return Arrays.stream(libraryPaths).map(Paths::get).toArray(Path[]::new);
      }
    }
  }

  @Override
  protected CILOSTAZOLContext createContext(Env env) {
    return new CILOSTAZOLContext(this, env);
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    var source =
        Source.newBuilder(
                CILOSTAZOLLanguage.ID,
                request.getSource().getBytes(),
                request.getSource().getName())
            .build();

    var assembly = CILOSTAZOLContext.get(null).loadAssembly(source);
    MethodSymbol main = assembly.getEntryPoint();
    return new CallEntryPointCallTarget(
        main.getNode().getCallTarget(), main.getParameters().length == 1);
  }

  public boolean isAllocationTrackingDisabled() {
    return noAllocationTracking.isValid();
  }

  public void invalidateAllocationTrackingDisabled() {
    noAllocationTracking.invalidate();
  }

  public GuestAllocator getAllocator() {
    return allocator;
  }

  public void initializeGuestAllocator(TruffleLanguage.Env env) {
    this.allocator = new GuestAllocator(this, env.lookup(AllocationReporter.class));
  }
}
