package translate;

import frame.Frame;
import semant.Env;
import symbol.ClassInfo;
import symbol.MethodInfo;
import symbol.Symbol;
import syntaxtree.ClassDecl;
import syntaxtree.ClassDeclExtends;
import syntaxtree.ClassDeclSimple;
import syntaxtree.MainClass;
import syntaxtree.MethodDecl;
import syntaxtree.Program;
import syntaxtree.VisitorAdapter;
import temp.Label;
import tree.CONST;
import tree.ESEQ;
import tree.Stm;
import util.List;

class IRBuilder extends VisitorAdapter
{
    private Env env;
    private Frame frame;
    
    private Frag result;
    private Frag tail;
    
    private ClassInfo cinfo;
    private MethodInfo minfo;
    
    private IRBuilder(Env e, Frame f)
    {
        super();
        
        env = e;
        frame = f;
        
        result = tail = null;
    }

    private void addFrag(Frag f)
    {
        if ( result == null )
            result = tail = f;
        else
            tail = tail.next = f;
    }
    
    static Frag build(Env e, Program p, Frame f)
    {
        IRBuilder b = new IRBuilder(e, f);
        
        Label l = new Label("_minijava_main_1");
        b.frame = f.newFrame(l, null);
        
        p.accept(b);
        
        return b.result;
    }
    
    public void visit(Program p)
    {
        p.mainClass.accept(this);
        
        for(List<ClassDecl> c = p.classList; c != null; c = c.tail)
            c.head.accept(this);
    }
    
    public void visit(MainClass node)
    {
        Stm s = StatementHandler.translate(frame, env, null, null, node.s).unNx();
        
        //Label l = new Label("_minijava_main$1");
        List<tree.Exp> param = new List<tree.Exp>(new CONST(0),null);
        tree.Exp r = new ESEQ( s, frame.externalCall("minijavaExit", param));
        
        s = frame.procEntryExit1(r);
        
        ProcFrag f = new ProcFrag(s, frame);
        
        addFrag(f);
    }
    
    public void visit(ClassDeclSimple node)
    {
        cinfo = env.classes.get(Symbol.symbol(node.name.s));
        for ( List<MethodDecl> aux = node.methodList; aux != null; aux = aux.tail )
        {
            minfo = cinfo.methods.get(Symbol.symbol(aux.head.name.s));
            tree.Exp body = MethodDeclHandler.translate(env, cinfo, aux.head);
            
            Stm b = minfo.frame.procEntryExit1(body);
            ProcFrag f = new ProcFrag(b, minfo.frame);
            addFrag(f);
        }
    }
    
    public void visit(ClassDeclExtends node)
    {
        cinfo = env.classes.get(Symbol.symbol(node.name.s));
        for ( List<MethodDecl> aux = node.methodList; aux != null; aux = aux.tail )
        {
            minfo = cinfo.methods.get(Symbol.symbol(aux.head.name.s));
            tree.Exp body = MethodDeclHandler.translate(env, cinfo, aux.head);
            
            Stm b = minfo.frame.procEntryExit1(body);
            ProcFrag f = new ProcFrag(b, minfo.frame);
            addFrag(f);
        }
    }
}
