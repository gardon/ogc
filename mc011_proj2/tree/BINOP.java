package tree;

import util.List;

public class BINOP extends Exp
{
    public static final int PLUS = 0;
    public static final int MINUS = 1;
    public static final int TIMES = 2;
    public static final int DIV = 3;
    public static final int AND = 4;
    public static final int OR = 5;
    public static final int LSHIFT = 6;
    public static final int RSHIFT = 7;
    public static final int ARSHIFT = 8;
    public static final int XOR = 9;
    
    public int binop;
    public Exp left;
    public Exp right;
    
    public BINOP(int b, Exp l, Exp r)
    {
        super();
        
        binop = b;
        left = l;
        right = r;
    }

    public List<Exp> kids()
    {
        return new List<Exp>(left, new List<Exp>(right,null));
    }
    
    public Exp build(List<Exp> kids)
    {
        return new BINOP(binop,kids.head,kids.tail.head);
    }
}
