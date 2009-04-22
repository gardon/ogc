package translate;

import frame.Frame;
import semant.Env;
import symbol.ClassInfo;
import symbol.MethodInfo;
import util.List;

class ExpListHandler
{
    private ExpListHandler()
    {
        super();
    }

    static List<tree.Exp> translate(Frame f, Env e, ClassInfo c, MethodInfo m, List<syntaxtree.Exp> le)
    {
        List<tree.Exp> result = null, tail = null, nn;
        
        for ( ; le != null; le = le.tail )
        {
            tree.Exp aux = ExpHandler.translate(f, e, c, m, le.head).unEx();
            
            nn = new List<tree.Exp>(aux, null);
            
            if ( result == null)
                result = tail = nn;
            else
                tail = tail.tail = nn;
        }
        return result;
    }
}
