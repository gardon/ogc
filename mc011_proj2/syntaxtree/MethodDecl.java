package syntaxtree;

import util.List;
import visitor.TypeVisitor;
import visitor.Visitor;

public class MethodDecl extends Absyn
{
	public Type returnType;
	public Identifier name;
	public Exp returnExp;
	public List<Formal> formals;
	public List<Statement> body;
	public List<VarDecl> locals;
	
	public MethodDecl(int l, int r, Type rt, Identifier n, List<Formal> f, List<VarDecl> ll, List<Statement> sl, Exp rr)
	{
		super(l, r);
		returnType = rt;
		name = n;
		formals = f;
		body = sl;
		returnExp = rr;
		locals = ll;
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		StringBuffer b = new StringBuffer();
		
		for ( List<Formal> l = formals; l != null; l = l.tail )
		{
			buffer.append( l.head );
			if ( l.tail != null )
				buffer.append(", ");
		}
		
		for ( List<Statement> l = body; l != null; l = l.tail )
		{
			b.append( l.head );
			b.append( "; ");
		}
		
		return "public " + returnType + " " + name + "( " + buffer + ") {" + b + " return " + returnExp + "; } ";
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
