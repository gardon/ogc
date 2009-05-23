package symbol;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import temp.Label;

public class ClassInfo
{
    public Label vtable;
    
	public Symbol name;
	
	public ClassInfo base;
	
	public Hashtable<Symbol, VarInfo> attributes;
	public Hashtable<Symbol, MethodInfo> methods;
	
	private HashSet<Symbol> attributesNames;
	private HashSet<Symbol> methodsNames;
    
    public Vector<Symbol> attributesOrder;
    public Vector<Symbol> vtableIndex;
    
	public ClassInfo(Symbol n)
	{
		this(n,null);
	}
     
    // metodo que serve SOMENTE para 'copiar' os atributos da classe base
    // para a classe derivada, bem como para criar a vtable.
    // CUIDADO: CHAMAR ESTE METODO SOMENTE UMA VEZ!
    public void setBase(ClassInfo base)
    {
        int i;
        // parte facil: 'copiar' os campos da classe pai.
        for( i = base.attributesOrder.size() - 1; i >= 0 ; i-- )
        {
            // coloca o dito cujo no lugar certo
            Symbol s = base.attributesOrder.get(i);
            this.attributesOrder.insertElementAt(s, 0);
            
            // coloca este atributo hash de campos
            // somente se nao ha campo declarado
            // com o mesmo nome na classe derivada.
            if ( !attributes.containsKey(s) )
                attributes.put(s, base.attributes.get(s));
        }
        
        // parte muito chata: criar a vtable
        Vector<Symbol> vtableClone = (Vector<Symbol>)base.vtableIndex.clone();
        Hashtable<Symbol, MethodInfo> methodTableClone = 
            (Hashtable<Symbol, MethodInfo>)base.methods.clone();
        HashSet<Symbol> methodsNameClone = (HashSet<Symbol>) base.methodsNames.clone();
        
        for( i = 0; i < vtableIndex.size(); i++ )       
        {
            // coloca na vtable somente os metodos que nao
            // sao herdados.
            Symbol name = vtableIndex.get(i);
            
            if ( !base.methodsNames.contains( name ) )
            {
                vtableClone.add(name);
                methodsNameClone.add(name);
                
                MethodInfo m = methods.get(name);
            }
            
            methodTableClone.put(name, methods.get(name));
        }
        
        vtableIndex = vtableClone;
        methods = methodTableClone;
        methodsNames = methodsNameClone;
        
        this.base = base;
    }
    
	public ClassInfo(Symbol n, ClassInfo b)
	{
		super();
		
		name = n;
		base = b;
		
		attributes = new Hashtable<Symbol, VarInfo>();
		methods = new Hashtable<Symbol, MethodInfo>();
		
		attributesNames = new HashSet<Symbol>();
		methodsNames = new HashSet<Symbol>();
        
        attributesOrder = new Vector<Symbol>();
        
        vtableIndex = new Vector<Symbol>();
	}
	
	public boolean addAttribute(VarInfo var)
	{
		if ( attributesNames.contains(var.name) )
			return false;

		attributes.put(var.name, var);
		attributesNames.add(var.name);
        
        attributesOrder.add(var.name);
		
		return true;
	}
	
    public int getAttributeOffset(Symbol name)
    {
        if ( !attributesNames.contains(name) )
            return -1;
        
        // pega a ultima declaracao do simbolo.
        return attributesOrder.lastIndexOf(name) + 1; // adicionando 1 para considerar a vtable
    }
    
    public int getMethodOffset(Symbol name)
    {
        if ( !methodsNames.contains(name) )
            return -1;
        
        return vtableIndex.indexOf(name);
    }
    
	public boolean addMethod(MethodInfo method)
	{
		if ( methodsNames.contains(method.name) )
			return false;
		
		methods.put(method.name, method);
		methodsNames.add(method.name);
        
        vtableIndex.add(method.name);
		
		return true;
	}

}
