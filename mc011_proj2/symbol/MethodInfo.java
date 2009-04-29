package symbol;

import java.util.HashSet;
import java.util.Hashtable;

import frame.Access;
import frame.Frame;

import syntaxtree.Type;
import util.List;

public class MethodInfo
{
	public Type type;
	public Symbol name;
    public Symbol parent;
	public List<VarInfo> formals;
	public List<VarInfo> locals;
	
    public Hashtable<Symbol, VarInfo> formalsTable;
    public Hashtable<Symbol, VarInfo> localsTable;
    
	private HashSet<Symbol> formalsNames;
	private HashSet<Symbol> localsNames;
    
    private List<VarInfo> formalsTail;
    private List<VarInfo> localsTail;
    
    public Access thisPtr;
    
    public Frame frame;
	
    public String decorateName()
    {
        return "??" + name + "$" + parent;
    }
    
	public MethodInfo(Type t, Symbol n, Symbol p)
	{
		super();
		
		type = t;
		name = n;
        parent = p;
		
		formals = null;
		locals = null;
		
		formalsNames = new HashSet<Symbol>();
		localsNames = new HashSet<Symbol>();
        
        formalsTable = new Hashtable<Symbol, VarInfo>();
        localsTable = new Hashtable<Symbol, VarInfo>();
	}

	public boolean addFormal(VarInfo formal)
	{
		if ( formalsNames.contains(formal.name) || localsNames.contains(formal.name) )
			return false;
		
		List<VarInfo> nn = new List<VarInfo>(formal, null);
        
        if ( formals == null )
            formals = formalsTail = nn;
        else
            formalsTail = formalsTail.tail = nn;
        
        formalsTable.put(formal.name, formal);
		formalsNames.add(formal.name);
		
		return true;
	}
	
	public boolean addLocal(VarInfo local)
	{
		if ( formalsNames.contains(local.name) || localsNames.contains(local.name) )
			return false;
		
        List<VarInfo> nn = new List<VarInfo>(local, null);
        
        if ( locals == null )
            locals = localsTail = nn;
        else
            localsTail = localsTail.tail = nn;
        
        localsTable.put(local.name, local);
		localsNames.add(local.name);
		
		return true;
	}
}
