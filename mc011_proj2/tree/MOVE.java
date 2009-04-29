package tree;

import util.List;

public class MOVE extends Stm
{
    public Exp dst;
    public Exp src;
    
    public MOVE(Exp d, Exp s)
    {
        super();
        
        dst = d;
        src = s;
    }

    public List<Exp> kids()
    {
        if (dst instanceof MEM)
            return new List<Exp>(((MEM)dst).exp, new List<Exp>(src,null));
        else
            return new List<Exp>(src,null);
    }
    
    public Stm build(List<Exp> kids)
    {
        if (dst instanceof MEM)
            return new MOVE(new MEM(kids.head), kids.tail.head);
        else
            return new MOVE(dst, kids.head);
    }
}
