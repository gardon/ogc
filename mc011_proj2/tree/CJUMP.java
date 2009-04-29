package tree;

import temp.Label;
import util.List;

public class CJUMP extends Stm
{
    public static final int EQ = 0;
    public static final int NE = 1;
    public static final int LT = 2;
    public static final int LE = 3;
    public static final int GT = 4;
    public static final int GE = 5;
    public static final int ULT = 6;
    public static final int ULE = 7;
    public static final int UGT = 8;
    public static final int UGE = 9;
    
    public int op;
    public Exp left;
    public Exp right;
    public Label ifTrue;
    public Label ifFalse;
    
    public CJUMP(int o, Exp l, Exp r, Label t, Label f)
    {
        super();
        
        op = o;
        left = l;
        right = r;
        ifTrue = t;
        ifFalse = f;
    }

    public static int notRel(int relop)
    {
        switch (relop)
        {
            case EQ:  return NE;
            case NE:  return EQ;
            case LT:  return GE;
            case GE:  return LT;
            case GT:  return LE;
            case LE:  return GT;
            case ULT: return UGE;
            case UGE: return ULT;
            case UGT: return ULE;
            case ULE: return UGT;
            default: throw new Error("bad relop in CJUMP.notRel");
        }
    }
    
    public List<Exp> kids()
    {
        return new List<Exp>(left, new List<Exp>(right,null));
    }
    
    public Stm build(List<Exp> kids)
    {
        return new CJUMP(op,kids.head,kids.tail.head,ifTrue,ifFalse);
    }
}
