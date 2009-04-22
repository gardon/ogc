package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class Identifier extends Absyn
{
    public String s;
    
    public Identifier(int l, int r, String s)
    {
    	super(l, r);
    	this.s = s;
    }
    
    public String toString()
	{
		return s;
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
