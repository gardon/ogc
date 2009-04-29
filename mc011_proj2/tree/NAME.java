package tree;

import temp.Label;
import util.List;

public class NAME extends Exp
{
    public Label label;
    
    public NAME(Label l)
    {
        super();
        
        label = l;
    }

    public List<Exp> kids()
    {
        return null;
    }
    
    public Exp build(List<Exp> kids)
    {
        return this;
    }
}
