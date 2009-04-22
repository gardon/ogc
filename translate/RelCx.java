package translate;

import temp.Label;
import tree.CJUMP;
import tree.Stm;

class RelCx extends Cx
{
    private Exp left;
    private Exp right;
    private int op;
    
    RelCx(int o, Exp l, Exp r)
    {
        super();
        
        op = o;
        left = l;
        right = r;
    }

    Stm unCx(Label t, Label f)
    {
        return new CJUMP(op, left.unEx(), right.unEx(), t, f);
    }

}
