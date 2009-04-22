package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class Formal extends Absyn
{
	public Type type;
	public Identifier name;
	
	public Formal(int l, int r, Type t, Identifier n)
	{
		super(l, r);
		type = t;
		name =n;
	}
	
	public String toString()
	{
		return "" + type + " " + name + " ";
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
