package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class ArrayLookup extends Exp
{
	public Exp array;
	public Exp index;

	public ArrayLookup(int l, int r, Exp a, Exp i) {
		super(l, r);
		array = a;
		index = i;
	}

	public String toString()
	{
		return "" + array + "[" + index + "] ";
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
