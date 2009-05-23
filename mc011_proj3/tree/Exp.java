package tree;

import util.List;

public abstract class Exp
{
    abstract public List<Exp> kids();
    abstract public Exp build(List<Exp> kids);

    public Exp()
    {
        super();
    }
}
