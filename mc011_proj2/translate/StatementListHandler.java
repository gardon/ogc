package translate;

import semant.Env;
import symbol.ClassInfo;
import symbol.MethodInfo;
import syntaxtree.Statement;
import tree.CONST;
import tree.EXPSTM;
import tree.SEQ;
import tree.Stm;
import util.List;
import frame.Frame;

class StatementListHandler
{   
    private StatementListHandler()
    {
        super();
    }

    static List<Statement> getPrev(List<Statement> ls, List<Statement> actual)
    {
        for( ;ls != null; ls = ls.tail )
            if ( ls.tail == actual )
                return ls;
                
        return null;
    }
    
    static Exp translate(Frame f, Env e, ClassInfo c, MethodInfo m, List<Statement> ls)
    {
        Stm r = null;
        
        if ( ls == null)
            return new Nx(new EXPSTM(new CONST(0)));
        
        List<Statement> it = getPrev(ls, null);
        
        for( ; it != null; it = getPrev(ls, it) )
        {
            Stm s = StatementHandler.translate(f, e, c, m, it.head ).unNx();
            
            if ( r == null )
                r = s;
            else
                r = new SEQ(s, r);
        }
        
        return new Nx(r);
    }
}
