package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class Not extends Exp
{
	public Exp exp;
	
	public Not(int l, int r, Exp e) {
		super(l, r);
		exp = e;
	}

	public String toString()
	{
		return "!" + exp + " ";
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
