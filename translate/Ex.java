package translate;

import temp.Label;
import tree.CJUMP;
import tree.CONST;
import tree.Stm;

class Ex extends Exp
{
    tree.Exp exp;
    
    Ex(tree.Exp e)
    {
        super();
        
        exp = e;
    }

    tree.Exp unEx()
    {    
        return exp;
    }

    Stm unNx()
    {
        return new tree.EXPSTM(exp);
    }

    Stm unCx(Label t, Label f)
    {
        return new CJUMP(CJUMP.EQ, exp, new CONST(0), f, t);
    }

}
