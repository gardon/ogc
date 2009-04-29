package tree;

import util.List;

public class MEM extends Exp
{
    public Exp exp;
    
    public MEM(Exp e)
    {
        super();
        
        exp = e;
    }

    public List<Exp> kids()
    {
        return new List<Exp>(exp,null);
    }
    
    public Exp build(List<Exp> kids)
    {
        return new MEM(kids.head);
    }
}
