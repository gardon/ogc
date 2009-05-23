package translate;

import temp.Label;

public abstract class Exp
{
    Exp()
    {
        super();
    }

    abstract tree.Exp unEx();
    
    abstract tree.Stm unNx();
    
    abstract tree.Stm unCx(Label t, Label f);
}
