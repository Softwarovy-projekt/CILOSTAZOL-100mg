# CILOSTAZOL 💊

![graalVM CE tests](https://github.com/Softwarovy-projekt/CILOSTAZOL-100mg/actions/workflows/test_with_graalCE.yml/badge.svg)

CILOSTAZOL is a CIL interpreter and a continuation of the BACIL project.
This project focuses on building a modern and extensible CIL interpreter with
the performance features and support of GraalVM.

## Overview

The project consists of the following modules:

- *cil-parser* - a Java-based parser for the CIL metadata format
- *language* - the interpreter and its models (types, objects, analysis, etc.)
- *launcher* - a launcher for the interpreter, handles command-line arguments
- *tests* - a set of integration tests for the interpreter

## Prerequisites

There are two main ways of running CILOSTAZOL.
Firstly, one can run CILOSTAZOL on GraalVM, which is the recommended way.
Secondly, one can run CILOSTAZOL on stock JDK.

The latter case is simpler.
One only needs to have Java 17 installed and recognized by Maven.
Truffle dependencies are then downloaded automatically.

The former case requires GraalVM to be installed.
GraalVM can be downloaded from the [GraalVM website](https://www.graalvm.org/downloads/) or installed using SDKMAN.
The current available version for Java 17 is *Oracle GraalVM 17.0.8*, although before changing the versioning scheme,
the version used for development was *Oracle GraalVM 22.3.0*.

The downloaded JDK works as any other.
If the user has properly set up the JAVA_HOME environment variable, the JDK is automatically recognized.
It can be checked by running `java --version` from the terminal.
The output should look like this:

```
java version "17.0.8" 2023-07-18 LTS
Java(TM) SE Runtime Environment Oracle GraalVM 17.0.8+9.1 (build 17.0.8+9-LTS-jvmci-23.0-b14)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 17.0.8+9.1 (build 17.0.8+9-LTS-jvmci-23.0-b14, mixed mode, sharing)
```

Or like this for older versions:

```
java 17.0.4 2022-07-19 LTS
Java(TM) SE Runtime Environment GraalVM EE 22.2.0 (build 17.0.4+11-LTS-jvmci-22.2-b05)
Java HotSpot(TM) 64-Bit Server VM GraalVM EE 22.2.0 (build 17.0.4+11-LTS-jvmci-22.2-b05, mixed mode, sharing)
```

The selected build system is Maven.
Maven can be installed using the package manager of the user's operating system or downloaded from the
[Maven website](https://maven.apache.org/download.cgi).
The version used for development was *Apache Maven 3.8.5*.
Ensure that Maven is using your preferred JDK by running `mvn --version` from the terminal.

In order to run tests, one should have .NET installed.
The version used for development was .NET 7.0.2, and we recommend using versions of .NET 7.
The .NET SDK can be downloaded from the [Microsoft website](https://dotnet.microsoft.com/download/dotnet/7.0).
It is enough to download the runtime, the entire SDK is not needed.

## Running CILOSTAZOL

Once the user has selected his JDK preference, he can run CILOSTAZOL.
The source code is available in the *CILOSTAZOL* repository
on [GitHub](https://github.com/Softwarovy-projekt/Cilostazol).
The project is built using Maven, and it can be done by running `mvn package` from the root of the repository.
This will build the project and run all tests.
Note that running tests in the *tests* package takes up to 10 minutes.
The tests can be skipped by running `mvn package -Dmaven.test.skip`.

As a result, we get the following jar files:

- *launcher.jar* - the main jar file that can be used to run CILOSTAZOL
- *cil-language.jar* - the jar file containing the CILOSTAZOL language and its parser
- *cil-parser-1.0-SNAPSHOT.jar* - a jar file containing the parser for the CIL language, hidden to the user
- *tests-1.0-SNAPSHOT.jar* - a jar file containing the tests for the CIL language, hidden to the user

Maven does two types of shading.
First, the *cil-language.jar* provides the language and all its dependencies.
This is important as we will be providing the language as its own artifact.
Second, the *launcher.jar* provides the launcher and uses the *cil-language.jar* as a dependency.

The *launcher.jar* can be run using `java -jar <path>/launcher.jar`.
This by itself only prints the help message.
If we want to run a CIL program, we need to append *cil-language.jar* to the Truffle class path.
Running `java -Dtruffle.class.path.append=<path>/cil-language.jar -jar <path>/launcher.jar` now shows the CIL language
support in the help message.

Finally, if we
run `java -Dtruffle.class.path.append=<path>/cil-language.jar -jar <path>/launcher.jar --cil.libraryPath=<path>/Microsoft.NETCore.App/7.0.3/ <path>/nbody-2.dll`,
we successfully run the interpreter on the *nbody-2.dll* program.

This way, we parametrize the launch of the interpreter to allow for different versions of .NET and different versions
of the interpreter using the same launcher.

The project can be opened in IntelliJ IDEA as a Maven project.
This way, the user can run the project from the IDE and debug it.
During development, this was the preferred way of running the project.
