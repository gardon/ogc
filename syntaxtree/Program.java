package syntaxtree;

import util.List;
import visitor.TypeVisitor;
import visitor.Visitor;

public class Program extends Absyn
{
	public MainClass mainClass;
	public List<ClassDecl> classList;
	
	public Program(int l, int r, MainClass m, List<ClassDecl> cl)
	{
		super(l, r);
		mainClass = m;
		classList = cl;
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		for ( List<ClassDecl> aux = classList; aux != null; aux = aux.tail )
			buffer.append( aux.head );
		
		return "" + mainClass + buffer;
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
