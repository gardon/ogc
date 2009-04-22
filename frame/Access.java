package frame;

import tree.Exp;

public abstract class Access
{
    public Access()
    {
    }

    public abstract Exp exp(Exp framePtr);
}
