package translate;

import temp.Label;
import tree.Stm;

// muito util para implementar expressoes. Exemplos:
// e1 && e2 -> if e1 then e2 else 0
// e1 || e2 -> if e1 then 0 else e2
class IfThenElseExp extends Cx
{
    Exp cond;
    Exp e1;
    Exp e2;
    Label t;
    Label f;
    Label join;
    
    IfThenElseExp( Exp c, Exp ee1, Exp ee2)
    {
        super();
        
        cond = c;
        e1 = ee1;
        e2 = ee2;
        t = new Label();
        f = new Label();
        join = new Label();
    }

    Stm unCx(Label tt, Label ff)
    {
        //Stm c = cond.unCx(t, f);
        //tree.Exp t;
        //tree.Exp f;
        
        
        return null;
    }

}
