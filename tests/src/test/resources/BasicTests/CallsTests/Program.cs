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