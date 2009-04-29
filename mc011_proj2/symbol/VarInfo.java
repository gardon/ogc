package symbol;

import frame.Access;
import syntaxtree.Type;

public class VarInfo
{
	public Type type;
	public Symbol name;
	
    public Access access;
    
	public VarInfo(Type t, Symbol s)
	{
		super();
		
		type = t;
		name = s;
	}

}
