namespace OthersTest;

public class Class
{

    public int SimpleTryCatchWithThrow()
    {
        var a = 22;
        try
        {
            throw new Exception();
        }
        catch(Exception ex)
        {
            a = 42;
        }

        return a;
    }
    
    public int SimpleTryCatchFinallyWithThrow()
    {
        var a = 22;
        try
        {
            a = 32;
            throw new Exception();
        }
        catch (Exception ex)
        {
            a = 42;
        }
        finally
        {
            a = 52;
        }

        return a;
    }
    
    public int SimpleTryCatchWithoutThrow()
    {
        var a = 22;
        try
        {
            a = 42;
        }
        catch(Exception ex)
        {
            a = 52;
        }

        return a;
    }
    
    public int SimpleTryCatchFinallyWithoutThrow()
    {
        var a = 22;
        var b = 22;
        try
        {
            a = 32;
            b = 12;
        }
        catch (Exception ex)
        {
            a = 42;
            b = 42;
        }
        finally
        {
            a = 52;
        }

        return a + b;
    }
    
    public int ReturnInCatch()
    {
        var a = 22;
        try
        {
            if (a == 32)
                throw new Exception();
        }
        catch(Exception ex)
        {
            return 52;
        }

        return a;
    }
    
    public int ReturnInTry()
    {
        var a = 22;
        try
        {
            if (a == 32)
                return 52;
        }
        catch(Exception ex)
        {
            a = 42;
        }

        return a;
    }
    
    public int DoubleCatch()
    {
        var a = 22;
        try
        {
            a = 32;
        }
        catch (ArgumentNullException ex)
        {
            a = 42;
        }
        catch (Exception ex)
        {
            a = 52;
        }

        return a;
    }
    
    public int NestedTry()
    {
        var a = 22;
        var b = 22;
        try
        {
            a = 32;
            try
            {
                a = 42;
            }
            catch (Exception e)
            {
                a = 52;
            }

            b = 12;
        }
        catch (Exception ex)
        {
            a = 62;
        }

        return a + b;
    }
    
    public int NestedTryDoubleCatch()
    {
        var a = 22;
        var b = 22;
        try
        {
            a = 32;
            try
            {
                a = 42;
            }
            catch (ArgumentNullException ex)
            {
                a = 62;
            }
            catch (Exception e)
            {
                a = 22;
            }

            b = 23;
        }
        catch (Exception ex)
        {
            a = 11;
        }

        return a + b;
    }

    public int Rethrow()
    {
        var a = 22;
        var b = 22;
        try
        {
            a = 32;
        }
        catch (ArgumentNullException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            a = 23;
        }

        return a + b;
    }

    public int OnlyThrow()
    {
        throw new Exception();
    }
    
    public int OnlyConditionalThrow()
    {
        var a = 22;
        if (a == 32)
            throw new Exception();
        a = 62;
        return a;
    }

    public int NestedTryInCatchClause()
    {
        var a = 22;
        var b = 22;
        try
        {
            a = 32;
        }
        catch (Exception ex)
        {
            try
            {
                a = 42;
            }
            catch (Exception e)
            {
                a = 12;
            }
            
            b = 52;
        }
        b = 52;
        return a;
    }
    
    public int NestedTryInCatchClauseWithThrow()
    {
        var a = 22;
        var b = 22;
        try
        {
            a = 32;
        }
        catch (Exception ex)
        {
            try
            {
                a = 42;
                throw new Exception();
            }
            catch (Exception e)
            {
                a = 12;
            }
            b = 52;
        }

        return a + b;
    }
}