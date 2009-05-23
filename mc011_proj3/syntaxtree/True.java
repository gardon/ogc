package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class True extends Exp
{
	public True(int l, int r)
	{
		super(l, r);
	}

	public String toString()
	{
		return "true ";
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
