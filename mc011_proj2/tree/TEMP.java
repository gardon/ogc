package tree;

import temp.Temp;
import util.List;

public class TEMP extends Exp
{
    public Temp temp;
    
    public TEMP(Temp t)
    {
        super();
        
        temp = t;
    }

    public List<Exp> kids()
    {
        return null;
    }
    
    public Exp build(List<Exp> kids)
    {
        return this;
    }
}
