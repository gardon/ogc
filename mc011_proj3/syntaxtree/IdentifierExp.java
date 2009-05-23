package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class IdentifierExp extends Exp
{
	public Identifier name;
	
	public IdentifierExp(int l, int r, Identifier n)
	{
		super(l, r);
		name = n;
	}

	public String toString()
	{
		return name.toString();
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
