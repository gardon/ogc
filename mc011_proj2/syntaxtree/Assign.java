package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class Assign extends Statement
{
	public Identifier var;
	public Exp exp;
	
	public Assign(int l, int r, Identifier v, Exp e)
	{
		super(l, r);
		var = v;
		exp = e;
	}

	public String toString()
	{
		return "" + var + " = " + exp + " ";
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
