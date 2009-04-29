package syntaxtree;

public abstract class Exp extends Absyn
{
    public Type type;
    
	public Exp(int l, int r)
	{
		super(l, r);
	}
}
