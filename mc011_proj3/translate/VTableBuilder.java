package translate;

import semant.Env;
import symbol.ClassInfo;
import symbol.MethodInfo;
import symbol.Symbol;
import syntaxtree.ClassDecl;
import syntaxtree.ClassDeclExtends;
import syntaxtree.ClassDeclSimple;
import syntaxtree.Program;
import syntaxtree.VisitorAdapter;
import util.List;

class VTableBuilder extends VisitorAdapter
{
    private Frag result;
    private Frag tail;
    private Env env;
        
    private void addFrag(Frag f)
    {
        if ( result == null )
            result = tail = f;
        else
            tail = tail.next = f;
    }
    
    private VTableBuilder(Env e)
    {
        super();
        
        env = e;
        
        result = tail = null;
    }

    public static Frag build(Env e, Program p)
    {
        VTableBuilder b =  new VTableBuilder(e);
        
        p.accept(b);
        
        return b.result;
    }
    
    public void visit(Program p)
    {
        p.mainClass.accept(this);
        
        for ( List<ClassDecl> l = p.classList; l != null; l = l.tail )
            l.head.accept(this);
    }
    
    private void BuildVTable(ClassInfo info)
    {
        String name = info.name.toString();
        String[] indexes = new String[info.vtableIndex.size()];
        
        for ( int i = 0; i < info.vtableIndex.size(); i++ )
        {
            MethodInfo m = info.methods.get(info.vtableIndex.get(i));
            
            indexes[i] = m.decorateName();
        }
        
        VtableFrag frag = new VtableFrag(name, indexes);
        
        info.vtable = frag.name;
        
        this.addFrag(frag);
    }
        
    public void visit(ClassDeclSimple node)
    {
        Symbol name = Symbol.symbol(node.name.s);
        
        ClassInfo info = env.classes.get(name);
        
        this.BuildVTable(info);
    }
    
    public void visit(ClassDeclExtends node)
    {
        Symbol name = Symbol.symbol(node.name.s);
        
        ClassInfo info = env.classes.get(name);
        
        this.BuildVTable(info);
    }
}
