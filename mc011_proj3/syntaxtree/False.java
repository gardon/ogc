package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class False extends Exp
{
	public False(int l, int r)
	{
		super(l, r);
	}

	public String toString()
	{
		return "false ";
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
