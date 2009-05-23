package syntaxtree;

import visitor.TypeVisitor;
import visitor.Visitor;

public class IdentifierType extends Type
{
	public String name;
	
	public IdentifierType(int l, int r, String n)
	{
		super(l, r);
		name = n;
	}
	
	public String toString()
	{
		return name;
	}

	public void accept(Visitor v)
	{
		v.visit(this);
	}
	
	public Type accept(TypeVisitor v)
	{
		return v.visit(this);
	}
    
    public boolean isComparable(Type t)
    {
        if ( !(t instanceof IdentifierType) )
            return false;
        
        IdentifierType i = (IdentifierType) t;
        
        return i.name.compareTo(name) == 0;
    }
}
