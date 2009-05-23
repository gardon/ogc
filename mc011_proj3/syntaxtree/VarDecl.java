package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class VarDecl extends Absyn
{
	public Type type;
	public Identifier name;
	
	public VarDecl(int l, int r, Type t, Identifier n)
	{
		super(l, r);
		type = t;
		name = n;
	}
	
	public String toString()
	{
		return "" + type + " " + name + "; ";
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
