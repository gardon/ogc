package x86;

import temp.Temp;
import tree.Exp;
import tree.TEMP;
import frame.Access;

class InReg extends Access
{
    public Temp temp;
    
    public InReg()
    {
        super();
    }

    public Exp exp(Exp framePtr)
    {
        return new TEMP(temp);
    }

}
