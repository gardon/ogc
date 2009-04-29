package syntaxtree;

import util.List;
import visitor.TypeVisitor;
import visitor.Visitor;

public class ClassDeclExtends extends ClassDecl
{
	public Identifier superClass;
	
	public ClassDeclExtends(int l, int r, Identifier n, Identifier s, List<VarDecl> vl, List<MethodDecl> ml)
	{
		super(l, r, n, vl, ml);
		superClass = s;
	}
	
	public String toString()
	{
		StringBuffer vars = new StringBuffer();
		StringBuffer methods = new StringBuffer();
		
		for ( List<VarDecl> aux = varList; aux != null; aux = aux.tail )
			vars.append( aux.head );
		
		for ( List<MethodDecl> aux = methodList; aux != null; aux = aux.tail )
			methods.append( aux.head );
		
		return "class " + name + "extends " + superClass + "{ " + vars + methods + "} ";
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
