namespace BranchOperationsTest;

public class Class
{
    public int BrFalse(){
        int a = 1;
        if (a < 100)
            return 42;
        return 10;
    }

    public int Switch(){
        byte a = 1;
        switch (a)
        {
            case 1:
                return 42;
            case 2:
                return 52;
            case 3:
                return 64;
            case 5:
                return 20;
            default:
                return 10;
        }
    }
}