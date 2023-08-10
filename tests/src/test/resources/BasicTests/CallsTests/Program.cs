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