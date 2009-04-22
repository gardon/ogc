package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class IntegerType extends Type
{
	public IntegerType(int l, int r)
	{
		super(l, r);
	}
	
	public String toString()
	{
		return "int ";
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
