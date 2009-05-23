package tree;

import temp.Label;
import util.List;

public class JUMP extends Stm
{
    public Exp exp;
    public List<Label> targets;
    
    public JUMP(Exp e, List<Label> t)
    {
        super();
        
        exp = e;
        targets = t;
    }
    
    public JUMP(Label target)
    {
        this(new NAME(target),new List<Label>(target,null));
    }
    
    public List<Exp> kids()
    {
        return new List<Exp>(exp,null);
    }
    
    public Stm build(List<Exp> kids)
    {
        return new JUMP(kids.head,targets);
    }
}
