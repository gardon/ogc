package syntaxtree;

public abstract class Type extends Absyn
{
    public boolean isComparable(Type t)
    {
        return t.getClass() == this.getClass();
    }
    
	public Type(int l, int r)
	{
		super(l, r);
	}
}
