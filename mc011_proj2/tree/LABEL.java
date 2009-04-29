package tree;

import temp.Label;
import util.List;

public class LABEL extends Stm
{
    public Label label;
    
    public LABEL(Label l)
    {
        super();
        
        label = l;
    }

    public List<Exp> kids()
    {
        return null;
    }
    
    public Stm build(List<Exp> kids)
    {
        return this;
    }
}
