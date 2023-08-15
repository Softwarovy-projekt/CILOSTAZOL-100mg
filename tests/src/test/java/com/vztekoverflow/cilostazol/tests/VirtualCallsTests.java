package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class VirtualCallsTests extends TestBase {
  @Test
  @Disabled(
      "Its purpose is to rapidly develop tests while being able to see CIL next to in thanks to the Rider IDE.")
  public void simpleVirtCallDll() {
    var result = runTestFromDll("CallsTests");

    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void simpleVirtCall() {
    var result =
        runTestFromCode(
            """
                    using System;
                    namespace CustomTest
                    {
                        public class Program
                        {
                            public static int Main()
                            {
                                return new A().Foo();
                            }
                        }

                        public class A{
                            public virtual int Foo(){
                                return 42;
                            }
                        }
                    }
                      """);

    assertEquals(42, result.exitCode());
  }

  @Test
  public void OverriddenAbstractMethodFromAbstractPredecessorCall() {
    var result =
        runTestFromCode(
            """
                    using System;
                    namespace CallsTests;

                    public class Program
                    {
                        public static int Main()
                        {
                            return new B().Foo();
                        }
                    }

                    public abstract class A{
                        public abstract int Foo();
                    }

                    public class B : A{
                        public override int Foo(){
                            Console.Write("B.Foo");
                            return 52;
                        }
                    }

                    """);
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void OverriddenVirtualMethodFromAbstractPredecessorCall() {
    var result =
        runTestFromCode(
            """
                    using System;
                    namespace CallsTests;

                    public class Program
                    {
                        public static int Main()
                        {
                            return new B().Foo();
                        }
                    }

                    public abstract class A
                    {
                        public virtual int Foo()
                        {
                            Console.Write("A.Foo");
                            return 42;
                        }
                    }

                    public class B : A{
                        public override int Foo(){
                            Console.Write("B.Foo");
                            return 52;
                        }
                    }

                    """);
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void OverriddenVirtualMethodFromPredecessorCall() {
    var result =
        runTestFromCode(
            """
                    using System;
                    namespace CallsTests;

                    public class Program
                    {
                        public static int Main()
                        {
                            return new B().Foo();
                        }
                    }

                    public class A
                    {
                        public virtual int Foo()
                        {
                            Console.Write("A.Foo");
                            return 42;
                        }
                    }

                    public class B : A{
                        public override int Foo(){
                            Console.Write("B.Foo");
                            return 52;
                        }
                    }

                    """);
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void InheritedVirtualMethodFromPredecessorCall() {
    var result =
        runTestFromCode(
            """
                    using System;
                    namespace CallsTests;

                    public class Program
                    {
                        public static int Main()
                        {
                            return new B().Foo();
                        }
                    }

                    public class A
                    {
                        public virtual int Foo()
                        {
                            Console.Write("A.Foo");
                            return 42;
                        }
                    }

                    public class B : A{
                    }

                    """);
    assertEquals(42, result.exitCode());
    assertEquals("A.Foo", result.output());
  }

  @Test
  public void InheritedVirtualMethodFromAbstractPredecessorCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return new B().Foo();
                    }
                }

                public abstract class A
                {
                    public virtual int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A{
                }

                    """);
    assertEquals(42, result.exitCode());
    assertEquals("A.Foo", result.output());
  }

  @Test
  public void InheritedOverriddenVirtualMethodFromPredecessorCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return new C().Foo();
                    }
                }

                public abstract class A
                {
                    public virtual int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A {
                    public override int Foo()
                    {
                        Console.Write("B.Foo");
                        return 52;
                    }
                }

                public class C : B
                {

                }
""");
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void InheritedOverriddenAsVirtualVirtualMethodFromPredecessorCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return new C().Foo();
                    }
                }

                public abstract class A
                {
                    public virtual int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A {
                    public virtual int Foo()
                    {
                        Console.Write("B.Foo");
                        return 52;
                    }
                }

                public class C : B
                {

                }
""");
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void InheritedVirtualMethodFromPredecessorsPredecessorCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return new C().Foo();
                    }
                }

                public abstract class A
                {
                    public virtual int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A
                {
                }

                public class C : B
                {
                }
""");
    assertEquals(42, result.exitCode());
    assertEquals("A.Foo", result.output());
  }

  @Test
  public void CastedInheritedVirtualMethodFromPredecessorsPredecessorCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return ((B)new C()).Foo();
                    }
                }

                public abstract class A
                {
                    public virtual int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A
                {
                }

                public class C : B
                {
                }
""");
    assertEquals(42, result.exitCode());
    assertEquals("A.Foo", result.output());
  }

  @Test
  public void OverriddenInheritedVirtualMethodFromPredecessorsPredecessorCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return new C().Foo();
                    }
                }

                public abstract class A
                {
                    public virtual int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A
                {
                }

                public class C : B
                {
                    public override int Foo()
                    {
                        Console.Write("C.Foo");
                        return 62;
                    }
                }
""");
    assertEquals(62, result.exitCode());
    assertEquals("C.Foo", result.output());
  }

  @Test
  public void LaterOverriddenInheritedVirtualMethodFromPredecessorsPredecessorCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return new B().Foo();
                    }
                }

                public abstract class A
                {
                    public virtual int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A
                {
                }

                public class C : B
                {
                    public override int Foo()
                    {
                        Console.Write("C.Foo");
                        return 62;
                    }
                }
""");
    assertEquals(42, result.exitCode());
    assertEquals("A.Foo", result.output());
  }

  @Test
  public void CastedLaterOverriddenInheritedVirtualMethodFromPredecessorsPredecessorCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return ((B)new C()).Foo();
                    }
                }

                public abstract class A
                {
                    public virtual int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A
                {
                }

                public class C : B
                {
                    public override int Foo()
                    {
                        Console.Write("C.Foo");
                        return 62;
                    }
                }
""");
    assertEquals(62, result.exitCode());
    assertEquals("C.Foo", result.output());
  }

  @Test
  public void CovariantCompliantOverriddenMethodCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return ((A)new C()).Foo().Get();
                    }
                }

                public abstract class A
                {
                    public virtual RetA Foo()
                    {
                        Console.Write("A.Foo");
                        return new RetA();
                    }
                }

                public class B : A {
                    public override RetB Foo()
                    {
                        Console.Write("B.Foo");
                        return new RetB();
                    }
                }

                public class C : B
                {
                }

                public class RetA
                {
                    public int Get() => 42;
                }

                public class RetB : RetA
                {
                    public int Get() => 52;
                }
""");
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void CovariantCompliantOverriddenMethodWithOverridenResultCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        return ((A)new C()).Foo().Get();
                    }
                }

                public abstract class A
                {
                    public virtual RetA Foo()
                    {
                        Console.Write("A.Foo");
                        return new RetA();
                    }
                }

                public class B : A {
                    public override RetB Foo()
                    {
                        Console.Write("B.Foo");
                        return new RetB();
                    }
                }

                public class C : B
                {
                }

                public class RetA
                {
                    public virtual int Get() => 42;
                }

                public class RetB : RetA
                {
                    public override int Get() => 52;
                }
""");
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }

  @Test
  public void MethodFromInterfaceCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        IA a = getA();
                        return a.Foo();
                    }

                    public static IA getA() {
                        return new A();
                    }
                }

                public interface IA
                {
                    public int Foo();
                }

                public class A : IA
                {
                    public int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }
        """);
    assertEquals(42, result.exitCode());
    assertEquals("A.Foo", result.output());
  }

  @Test
  public void InheritedMethodFromInterfaceCall() {
    var result =
        runTestFromCode(
            """
                using System;
                namespace CallsTests;

                public class Program
                {
                    public static int Main()
                    {
                        IA a = getB();
                        return a.Foo();
                    }

                    public static IA getB() {
                        return new B();
                    }
                }

                public interface IA
                {
                    public int Foo();
                }

                public class A : IA
                {
                    public int Foo()
                    {
                        Console.Write("A.Foo");
                        return 42;
                    }
                }

                public class B : A
                {
                }
        """);
    assertEquals(42, result.exitCode());
    assertEquals("A.Foo", result.output());
  }

  @Test
  public void OverwrittenInheritedMethodFromInterfaceCall() {
    var result =
        runTestFromCode(
            """
                            using System;
                            namespace CallsTests;

                            public class Program
                            {
                                public static int Main()
                                {
                                    IA a = getB();
                                    return a.Foo();
                                }

                                public static IA getB() {
                                    return new B();
                                }
                            }

                            public interface IA
                            {
                                public int Foo();
                            }

                            public class A : IA
                            {
                                public int Foo()
                                {
                                    Console.Write("A.Foo");
                                    return 42;
                                }
                            }

                            public class B : A
                            {
                                public int Foo()
                                {
                                    Console.Write("B.Foo");
                                    return 52;
                                }
                            }
                    """);
    assertEquals(52, result.exitCode());
    assertEquals("B.Foo", result.output());
  }
}
