# CILOSTAZOL 💊

![graalVM CE build](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/actions/workflows/build_with_graalCE.yml/badge.svg)
![graalVM CE tests](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/actions/workflows/test_with_graalCE.yml/badge.svg)

![commit activity](https://img.shields.io/github/commit-activity/w/Softwarovy-projekt/CILOSTAZOL-100mg)
![last commit](https://img.shields.io/github/last-commit/Softwarovy-projekt/CILOSTAZOL-100mg)

Continuation of BACIL interpreter

## Development

We recommend the following configuration steps to run and contribute to CILOSTAZOL.

1. Make a folder for CILOSTAZOL development (for example `CilostazolDev`).
2. Download [.NET 7](https://dotnet.microsoft.com/en-us/download/dotnet/7.0) core library
    - We copied `dotnet-runtime-7.0.1-win-x64/shared/Microsoft.NETCoreApp/7.0.1` folder
      to `CilostatolDev/Microsoft.NETCoreApp/7.0.1` folder.
3. Download [GraalVM](https://www.graalvm.org/downloads/) compiler (at least 21.3 version).
    - We put it into `CilostazolDev/GraalVM/graalvm-ce-java17-22.2.0`.
4. Download Cilostazol [repo](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg).
    - We put it into `CilostazolDev/CILOSTAZOL-100mg` folder.
5. Download Benchmarks and Tests [repo](https://github.com/Softwarovy-projekt/TestsAndBenchmarks).
    - We put it into `CilostazolDev/TestsAndBenchmarks` folder.
6. Build Benchmark and Tests (
   See [BenchmarksAndTests/README.md](https://github.com/Softwarovy-projekt/TestsAndBenchmarks/blob/main/README.md)).
7. Fix paths in `CilostazolDev/CILOSTAZOL-100mg/.run` folder.
    - We changed the following keys in `BACIL-clean-build-run.run.xml` and `BACIL-dry-run.run.xml`
        - `<graalvm-path>` to `../GraalVM/graalvm-ce-java17-22.2.0`
        - `<dotnet-core-lib-path>` to `../Microsoft.NETCoreApp/7.0.1`
        - `<dotnet-app-dll-path>` to `../TestsAndBenchmarks/Tests/OSR/CilostazolEx/bin/Debug/net7.0/CilostazolEx.dll`
8. Open IntelliJ IDEA IDE, locate the configurations and run it.

### Notes

It would be better to set Project SDK in IntelliJ IDEA to GraalVM SDK, becuase of CodeInsight...

**Configurations**

- `BACIL-build(Maven).run.xml` - Builds BACIL.
- `BACIL-clean-build-run.run.xml` - Builds BACIL and runs it.
- `BACIL-dry-run.run.xml` - Runs BACIL without building.

![LOC](https://img.shields.io/tokei/lines/github/Softwarovy-projekt/CILOSTAZOL-100mg)
![code size](https://img.shields.io/github/languages/code-size/Softwarovy-projekt/CILOSTAZOL-100mg)

### Dev

**Interpreter**

| Opcode | Implemented | Tested | Issue|
| --- | -- | --- | --- |
| add | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| and | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| beq | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| beq.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bge | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bge.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bge.un | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bge.un.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bgt | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bgt.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bgt.un | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bgt.un.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| ble | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| ble.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| ble.un | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| ble.un.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| blt | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| blt.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| blt.un | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| blt.un.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bne.un | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| bne.un.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| box | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| br | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| brfalse | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| brfalse.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| br.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| brtrue | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| brtrue.s | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| call | x | x | [#62](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/62) | 
| callvirt | x | x | [#63](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/63) |
| ceq | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| cgt | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| cgt.un | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| clt | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| clt.un | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| conv.i | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.i1 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.i2 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.i4 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.i8 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.r4 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.r8 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.u | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.u1 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.u2 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.u4 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.u8 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| div | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| dup | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| initobj | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| ldarg.0 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldarg.1 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldarg.2 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldarg.3 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldarga.s | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldarg.s | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.0 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.1 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.2 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.3 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.4 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.5 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.6 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.7 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.8 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.m1 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i4.s | o | o | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.i8 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.r4 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldc.r8 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldelem | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelema | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.i | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.i1 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.i2 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.i4 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.i8 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.r4 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.r8 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.ref | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.u1 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.u2 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldelem.u4 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldfld | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| ldflda | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| ldind.i | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.i1 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.i2 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.i4 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.i8 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.r4 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.r8 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.ref | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.u1 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.u2 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldind.u4 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| ldlen | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| ldloc.0 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldloc.1 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldloc.2 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldloc.3 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldloca.s | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldloc.s | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldnull | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| ldsfld | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| ldsflda | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| ldstr | o | x | [#79](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/79) |
| ldtoken | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| mul | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| neg | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| newarr | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| newobj | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| nop | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| not | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| or | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| pop | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| rem | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| ret | o | o | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| shl | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| shr | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| shr.un | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| starg.s | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| stelem | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stelem.i | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stelem.i1 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stelem.i2 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stelem.i4 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stelem.i8 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stelem.r4 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stelem.r8 | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stelem.ref | o | x | [#76](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/76) |
| stfld | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| stind.i | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| stind.i1 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| stind.i2 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| stind.i4 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| stind.i8 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| stind.r4 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| stind.r8 | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| stind.ref | o | x | [#77](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/77) |
| stloc.0 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| stloc.1 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| stloc.2 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| stloc.3 | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| stloc.s | o | x | [#73](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/73) |
| stsfld | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| sub | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| unbox.any | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| xor | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| Doesn't have impl. in bachelor thesis |
| add.ovf | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| add.ovf.un | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| arglist | x | x | x |
| break | o | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| calli | x | x | x |
| castclass | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| ckfinite | x | x | [#68](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/68) |
| constrained. | x | x | [#68](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/68) |
| conv.ovf.i | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i1 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i1.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i2 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i2.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i4 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i4.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i8 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i8.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.i.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u1 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u1.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u2 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u2.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u4 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u4.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u8 | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u8.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.ovf.u.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| conv.r.un | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| cpblk | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| cpobj | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| div.un | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| endfilter | x | x | [#61](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/61) |
| endfinally | x | x | [#61](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/61) |
| initblk | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| isinst | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| jmp | x | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| ldarg | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| ldarga | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| ldftn | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| ldloc | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| ldloca | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| ldobj | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| ldvirtftn | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| leave | x | x | [#61](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/61) |
| leave.s | x | x | [#61](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/61) |
| localloc | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| mkrefany | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| mul.ovf | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| mul.ovf.un | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| no. | x | x | [#68](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/68) |
| readonly. | x | x | [#68](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/68) |
| Refanytype | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| refanyval | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| rem.un | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| rethrow | x | x | [#61](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/61) |
| sizeof | x | x | [#80](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/80) |
| starg | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| stloc | x | x | [#78](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/78) |
| stobj | x | x | [#64](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/64) |
| sub.ovf | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| sub.ovf.un | x | x | [#75](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/75) |
| switch | x | x | [#60](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/60) |
| tail. | x | x | x |
| throw | x | x | [#61](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/61) |
| unaligned. | x | x |
| unbox | x | x | [#66](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/issues/66) |
| volatile. | x | x | x |
