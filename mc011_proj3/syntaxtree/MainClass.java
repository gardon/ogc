package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class MainClass extends Absyn
{
    public Identifier className;
    public Identifier mainArgName;
    public Statement s;
    
    public MainClass(int l, int r, Identifier i1, Identifier i2, Statement s)
    {
    	super(l, r);
    	
    	className = i1;
    	mainArgName = i2;
    	this.s = s;
    }
    
    public String toString()
	{
		return "class " + className + "{ public static void main( String[] " + mainArgName + ") { " + s + "} ";
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
