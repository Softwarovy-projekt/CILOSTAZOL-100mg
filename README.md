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
3. Download [GraalVM](https://www.graalvm.org/downloads/) (at least 22.2 version).
    - We put it into `CilostazolDev/GraalVM/graalvm-ce-java17-22.2.0`.
4. Download Cilostazol [repo](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg).
    - We put it into `CilostazolDev/CILOSTAZOL-100mg` folder.
5. Open IntelliJ IDEA IDE, open Cilostazol as a Maven project
6. Set GraalVM as a Project SDK (if not set automatically)
   - File -> Project Structure -> Project Settings -> Project -> Project SDK -> Add SDK -> GraalVM

### Notes

It would be better to set Project SDK in IntelliJ IDEA to GraalVM SDK, because of CodeInsight...

![LOC](https://img.shields.io/tokei/lines/github/Softwarovy-projekt/CILOSTAZOL-100mg)
![code size](https://img.shields.io/github/languages/code-size/Softwarovy-projekt/CILOSTAZOL-100mg)
