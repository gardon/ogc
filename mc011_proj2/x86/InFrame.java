package x86;

import tree.BINOP;
import tree.CONST;
import tree.Exp;
import tree.MEM;
import frame.Access;

class InFrame extends Access
{
    public long offset;
    
    public InFrame()
    {
        super();
    }

    public Exp exp(Exp framePtr)
    {
        if ( offset >= 0 )
            return new MEM(new BINOP(BINOP.PLUS, framePtr, new CONST(offset)));
        else
            return new MEM(new BINOP(BINOP.MINUS, framePtr, new CONST(-offset)));
    }

}
