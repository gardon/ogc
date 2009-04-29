package temp;

public class Temp
{
    private static long count = 0;
    private long myNumber;
    
    public Temp()
    {
        super();
        
        myNumber = count++;
    }

    public String toString()
    {
        return "_t" + myNumber;
    }
}
