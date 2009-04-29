package temp;

import symbol.Symbol;

public class Label
{
    private static long count = 0;
    
    private String myLabel;

    public Label(String l)
    {
        myLabel = l;
    }
    
    public Label()
    {
        this("L" + count++);
    }
    
    public Label(Symbol s)
    {
        this(s.toString());
    }

    public String toString()
    {
        return myLabel;
    }
}
