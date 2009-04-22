package translate;

import temp.Label;
import temp.Temp;
import tree.CONST;
import tree.ESEQ;
import tree.LABEL;
import tree.MOVE;
import tree.SEQ;
import tree.Stm;
import tree.TEMP;

public abstract class Cx extends Exp
{
    tree.Exp unEx()
    {
        Label t = new Label();
        Label f = new Label();
        Temp r = new Temp();
        
        MOVE move1 = new MOVE( new TEMP(r),new CONST(1) );
        MOVE move0 = new MOVE( new TEMP(r),new CONST(0) );
        SEQ tail = new SEQ( move0, new LABEL(t) );
        SEQ mid = new SEQ(new LABEL(f), tail);
        SEQ before = new SEQ(unCx(t,f), mid);
        SEQ retval = new SEQ(move1, before);
        
        return new ESEQ(retval, new TEMP(r));
    }

    // TODO: Sera que esta certo???
    Stm unNx()
    {
        Label t = new Label();
        Label f = new Label();
        /*---------------------------------------
        Temp r = new Temp();
        
        MOVE move1 = new MOVE( new TEMP(r),new CONST(1) );
        MOVE move0 = new MOVE( new TEMP(r),new CONST(0) );
        SEQ tail = new SEQ( move0, new LABEL(t) );
        SEQ mid = new SEQ(new LABEL(f), tail);
        SEQ before = new SEQ(unCx(t,f), mid);
        SEQ retval = new SEQ(move1, before);
        ---------------------------------------*/
        
        return unCx(t,f);
    }

    abstract Stm unCx(Label t, Label f);
}
