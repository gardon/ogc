package tree;

import util.List;

public class ESEQ extends Exp
{
    public Stm stm;
    public Exp exp;
    
    public ESEQ(Stm s, Exp e)
    {
        super();
        
        stm = s;
        exp = e;
    }

    public List<Exp> kids()
    {
        throw new Error("kids() not applicable to ESEQ");
    }
    
    public Exp build(List<Exp> kids)
    {
        throw new Error("build() not applicable to ESEQ");
    }
}
