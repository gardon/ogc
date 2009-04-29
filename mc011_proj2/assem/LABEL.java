package assem;

import temp.Label;
import temp.Temp;
import util.List;

public class LABEL extends Instr
{
    public Label label;

    public LABEL(String a, Label l)
    {
        assem=a; 
        label=l;
    }

    public List<Temp> use()
    {
        return null;
    }
    
    public List<Temp> def()
    {
        return null;
    }
    
    public Targets jumps()
    {
        return null;
    }
}
