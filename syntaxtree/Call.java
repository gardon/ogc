package syntaxtree;

import util.List;
import visitor.TypeVisitor;
import visitor.Visitor;

public class Call extends Exp
{
	public Exp object;
	public Identifier method;
	public List<Exp> actuals;

	public Call(int l, int r, Exp o, Identifier m, List<Exp> a)
	{
		super(l, r);
		object = o;
		method = m;
		actuals = a;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		for ( List<Exp> aux = actuals; aux != null; aux = aux.tail )
		{
			buffer.append( aux.head );
			if ( aux.tail != null )
				buffer.append( "," );
		}
		
		return "" + object + "." + method + "(" + buffer + "); ";
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
