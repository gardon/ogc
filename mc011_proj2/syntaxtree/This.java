package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class This extends Exp
{
	public This(int l, int r) 
	{
		super(l, r);
	}

	public String toString()
	{
		return "this ";
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
