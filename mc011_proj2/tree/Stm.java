package tree;

import util.List;

public abstract class Stm
{
    public Stm()
    {
        super();
    }
    
    abstract public List<Exp> kids();
    
    abstract public Stm build(List<Exp> kids);
}
