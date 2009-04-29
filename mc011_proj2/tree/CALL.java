package tree;

import util.List;

public class CALL extends tree.Exp
{
    public Exp func;
    public List<Exp> args;
    
    public CALL(Exp f, List<Exp> a)
    {
        super();
        
        func = f;
        args = a;
    }

    public List<Exp> kids()
    {
        return new List<Exp>(func,args);
    }
    
    public Exp build(List<Exp> kids)
    {
        return new CALL(kids.head,kids.tail);
    }
}
