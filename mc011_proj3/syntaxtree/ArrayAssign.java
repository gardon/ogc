package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class ArrayAssign extends Statement
{
	public Identifier var;
	public Exp index;
	public Exp value;
	
	public ArrayAssign(int l, int r, Identifier v, Exp i, Exp vv)
	{
		super(l, r);
		var = v;
		index = i;
		value = vv;
	}

	public String toString()
	{
		return "" + var + "[" + index + "] = " + value + " ";
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
