package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class IntegerLiteral extends Exp
{
	public int value;

	public IntegerLiteral(int l, int r, int v) {
		super(l, r);
		value = v;
	}

	public String toString()
	{
		return "" + value + " ";
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
