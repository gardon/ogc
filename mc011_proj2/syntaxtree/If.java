package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class If extends Statement
{
	public Exp condition;
	public Statement thenClause;
	public Statement elseClause;
	
	public If(int l, int r, Exp c, Statement t, Statement e)
	{
		super(l, r);
		condition = c;
		thenClause = t;
		elseClause = e;
	}
	
	public If(int l, int r, Exp c, Statement t)
	{
		this(l, r, c, t, null);
	}

	public String toString()
	{
		if ( elseClause == null )
			return "if (" + condition + ") " + thenClause + " ";
		return "if (" + condition + ") " + thenClause + "else " + elseClause + " ";
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
