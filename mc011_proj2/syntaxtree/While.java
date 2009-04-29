package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class While extends Statement
{
	public Exp condition;
	public Statement body;
	
	public While(int l, int r, Exp c, Statement b)
	{
		super(l, r);
		condition = c;
		body = b;
	}
	
	public String toString()
	{
		return "while (" + condition + ") " + body;
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
