package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class Times extends Exp
{
	public Exp lhs;
	public Exp rhs;
	
	public Times(int l, int r, Exp ll, Exp rr)
	{
		super(l, r);
		lhs = ll;
		rhs = rr;
	}

	public String toString()
	{
		return "" + lhs + "* " + rhs;
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
