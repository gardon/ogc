package tree;

import util.List;

public class CONST extends Exp
{
    public long value;
    
    public CONST(long v)
    {
        super();
        
        value = v;
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
