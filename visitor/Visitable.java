package visitor;

import syntaxtree.Type;

public interface Visitable
{
	public void accept(Visitor v);
	public Type accept(TypeVisitor v);
}
