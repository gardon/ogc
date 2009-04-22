package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class ArrayLength extends Exp
{
	public Exp array;

	public ArrayLength(int l, int r, Exp a)
	{
		super(l, r);
		array = a;
	}

	public String toString()
	{
		return "" + array + ".length ";
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
