package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class Print extends Statement
{
	public Exp exp;
	
	public Print(int l, int r, Exp e)
	{
		super(l, r);
		exp = e;
	}

	public String toString()
	{
		return "System.out.println(" + exp + ")";
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
