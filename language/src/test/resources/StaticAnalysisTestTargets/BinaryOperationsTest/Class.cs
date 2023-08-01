namespace BinaryOperationsTest;

public class Class
{
    public void LoadTwoInt32_Add_SaveToInt32(){
        int a = 1;
        int b = 2;
        int c = a + b;
    }

    public void LoadTwoByte_Add_SaveToInt32(){
        byte a = 1;
        byte b = 2;
        int c = a + b;
    }

    public void LoadByteAndLong_Add_SaveToLong(){
        byte a = 1;
        long b = 2;
        long c = a + b;
    }
}