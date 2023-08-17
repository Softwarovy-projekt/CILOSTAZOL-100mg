using System;
namespace MyApp
{
    class Program
    {
        public static int Main()
        {
            Console.WriteLine("Hello World!");
            Console.WriteLine(42);
            Console.WriteLine(42.0);
            Console.WriteLine(true);
            Console.WriteLine('c');
            Console.WriteLine(new WithOverride());
            Console.WriteLine(new WithoutOverride());
            return 0;
        }
    }

    class WithOverride
    {
        public override string ToString()
        {
            return "MyString";
        }
    }
    
    class WithoutOverride
    {
    }
}