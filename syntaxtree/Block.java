package syntaxtree;

import util.List;
import visitor.TypeVisitor;
import visitor.Visitor;

public class Block extends Statement
{
	public List<Statement> body;
	
	public Block(int l, int r, List<Statement> b)
	{
		super(l, r);
		body = b;
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("{ ");
		for ( List<Statement> aux = body; aux != null; aux = aux.tail )
		{
			buffer.append( aux.head );
			buffer.append( ";" );
		}
		buffer.append("} ");
		
		return buffer.toString();
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
