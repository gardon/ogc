package translate;

import temp.Label;
import tree.Stm;

class Nx extends Exp
{
    Stm stm;
    
    public Nx(Stm s)
    {
        super();
        
        stm = s;
    }

    tree.Exp unEx()
    {
        throw new Error("unEx chamado para Nx");
    }

    Stm unNx()
    {
        return stm;
    }

    Stm unCx(Label t, Label f)
    {
        throw new Error("unEx chamado para Nx");
    }
}
