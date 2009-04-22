package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitable;
import visitor.Visitor;

public abstract class Absyn implements Visitable
{
	public int line;
	public int row;
	
	public Absyn(int l, int r)
	{
		line = l;
		row = r;
	}
	
	public void accept(Visitor v)
	{
		throw new Error("visit not implement on " + this.getClass());
	}

	public Type accept(TypeVisitor v)
	{
		throw new Error("type visit not implement on " + this.getClass());
	}
}
