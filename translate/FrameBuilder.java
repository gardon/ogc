package translate;

import frame.Access;
import frame.Frame;
import semant.Env;
import symbol.ClassInfo;
import symbol.MethodInfo;
import symbol.Symbol;
import symbol.VarInfo;
import syntaxtree.ClassDecl;
import syntaxtree.ClassDeclExtends;
import syntaxtree.ClassDeclSimple;
import syntaxtree.Formal;
import syntaxtree.MethodDecl;
import syntaxtree.Program;
import syntaxtree.VarDecl;
import syntaxtree.VisitorAdapter;
import temp.Label;
import util.List;

class FrameBuilder extends VisitorAdapter
{
    private Env env;
    private Frame parent;
    private ClassInfo cinfo;
    private MethodInfo minfo;
    
    private FrameBuilder(Frame p, Env e)
    {
        super();
        
        env = e;
        parent = p;
    }
    
    static void translate(Frame p, Env e, Program pp)
    {
        FrameBuilder b = new FrameBuilder(p, e);
        
        pp.accept(b);
    }
    
    public void visit(Program node)
    {
        for ( List<ClassDecl> aux = node.classList; aux != null; aux = aux.tail )
        {
            cinfo = env.classes.get(Symbol.symbol(aux.head.name.s));
            aux.head.accept(this);
        }
    }
    
    public void visit(ClassDeclSimple node)
    {       
        for ( List<MethodDecl> ms = node.methodList; ms != null; ms = ms.tail )
            ms.head.accept(this);
    }
    
    public void visit(ClassDeclExtends node)
    {
        
        for ( List<MethodDecl> ms = node.methodList; ms != null; ms = ms.tail )
            ms.head.accept(this);
    }
    
    public void visit(MethodDecl node)
    {
        // criando o frame
        List<Boolean> head = null, tail = null, nn;
        
        for ( List<Formal> aux = node.formals; aux != null; aux = aux.tail )
        {
            nn = new List<Boolean>(false, null);
            
            if ( head == null )
                head = tail = nn;
            else
                tail = tail.tail = nn;
        }
        
        // colocando o parametro 'this'
        head = new List<Boolean>(false, head);
        
        minfo = cinfo.methods.get(Symbol.symbol(node.name.s));
        
        Label methodName = new Label( minfo.decorateName() );
        
        Frame methodFrame = parent.newFrame(methodName, head);
                
        
        // facilitando a vida de muitas partes do compilador
        minfo.frame = methodFrame;
        minfo.thisPtr = methodFrame.formals.head;
        
        List<Access> f = methodFrame.formals.tail;
        
        for ( List<Formal> aux = node.formals; aux != null; aux = aux.tail, f = f.tail )
        {
            VarInfo v = minfo.formalsTable.get(Symbol.symbol(aux.head.name.s));
            
            v.access = f.head;
        }
        
        // criando espaco para as variaveis locais
        for ( List<VarDecl> locals = node.locals; locals != null; locals = locals.tail )
            locals.head.accept(this);
    }
    
    public void visit(VarDecl node)
    {
        VarInfo info = minfo.localsTable.get(Symbol.symbol(node.name.s));
        
        info.access = minfo.frame.allocLocal(false);
    }
}
