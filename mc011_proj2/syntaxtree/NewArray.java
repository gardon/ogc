package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class NewArray extends Exp
{
	public Exp size;
	
	public NewArray(int l, int r, Exp s)
	{
		super(l, r);
		size = s;
	}

	public String toString()
	{
		return "new int[" + size + "] ";
	}

	public void accept(Visitor v)
	{
		v.visit(this);
	}
	
	public Type accept(TypeVisitor v)
	{
		return v.visit(this);
	}
}
