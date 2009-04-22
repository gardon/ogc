package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class BooleanType extends Type
{
	public BooleanType(int l, int r)
	{
		super(l, r);
	}
	
	public String toString()
	{
		return "boolean ";
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
