package translate;

import semant.Env;
import symbol.ClassInfo;
import symbol.MethodInfo;
import symbol.Symbol;
import syntaxtree.MethodDecl;
import tree.ESEQ;

class MethodDeclHandler
{
    private MethodDeclHandler()
    {
        super();
    }

    static public tree.Exp translate(Env e, ClassInfo c, MethodDecl m)
    {
        Symbol name = Symbol.symbol(m.name.s);
        
        MethodInfo i = c.methods.get(name);
        
        Exp r = StatementListHandler.translate(i.frame, e, c, i, m.body);
        Exp v = ExpHandler.translate(i.frame, e, c, i, m.returnExp);
        
        //Label label = new Label(i.decorateName());
        
        //tree.Exp result = new ESEQ(new SEQ(new LABEL(label), r.unNx()),v.unEx());
        
        return new ESEQ(r.unNx(), v.unEx());
    }
}
