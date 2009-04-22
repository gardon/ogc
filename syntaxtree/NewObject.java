package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class NewObject extends Exp
{
	public Identifier className;

	public NewObject(int l, int r, Identifier c) {
		super(l, r);
		className = c;
	}

	public String toString()
	{
		return "new " + className + "() ";
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
