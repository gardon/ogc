package translate;

import frame.Frame;
import semant.Env;
import symbol.ClassInfo;
import symbol.MethodInfo;
import symbol.Symbol;
import symbol.VarInfo;
import syntaxtree.ArrayAssign;
import syntaxtree.Assign;
import syntaxtree.Block;
import syntaxtree.If;
import syntaxtree.Print;
import syntaxtree.Statement;
import syntaxtree.VisitorAdapter;
import syntaxtree.While;
import temp.Label;
import temp.Temp;
import tree.BINOP;
import tree.CONST;
import tree.EXPSTM;
import tree.JUMP;
import tree.LABEL;
import tree.MEM;
import tree.MOVE;
import tree.SEQ;
import tree.Stm;
import tree.TEMP;
import util.List;

class StatementHandler extends VisitorAdapter
{
    private Exp result;
    
    private ClassInfo cinfo;
    
    private MethodInfo minfo;
    
    private Env env;
    
    private Frame frame;
    
    private tree.Exp getVariable(Symbol name)
    {
        VarInfo v = minfo.localsTable.get(name);
        
        if ( v != null )
            return v.access.exp(new TEMP(frame.FP()));
        
        v = minfo.formalsTable.get(name);
        
        if ( v != null )
            return v.access.exp(new TEMP(frame.FP()));
        
        // obtendo atributo de classe
        int idx = cinfo.getAttributeOffset(name);
        
        MEM node = new MEM( 
                new BINOP(BINOP.PLUS,
                        minfo.thisPtr.exp(new TEMP(frame.FP())),
                        new BINOP(BINOP.LSHIFT, new CONST(idx), new CONST(2))));
        
        return node;
    }
    
    private StatementHandler(Frame f, Env e, ClassInfo c, MethodInfo m)
    {
        super();
        
        frame = f;
        env = e;
        cinfo = c;
        minfo = m;
    }

    static Exp translate(Frame f, Env e, ClassInfo c, MethodInfo m, Statement stm)
    {
        StatementHandler h = new StatementHandler(f, e, c, m);
        
        stm.accept(h);
        
        return h.result;
    }
    
    /*----*/
    /* IF */
    /*----*/
    public void visit(If node)
    {
        Label t = new Label();
        Label f = new Label();
        Label j = new Label();
        
        Exp cond = ExpHandler.translate(frame, env, cinfo, minfo, node.condition);
        Exp th = StatementHandler.translate(frame, env, cinfo, minfo, node.thenClause);
        Exp el = node.elseClause == null ? null : 
            StatementHandler.translate(frame, env, cinfo, minfo, node.elseClause);
        
        if ( el == null )
        {
            Stm r = new SEQ(cond.unCx(t,j),
                    new SEQ(new LABEL(t),
                            new SEQ(th.unNx(),
                                    new LABEL(j))));
            
            result = new Nx(r);
        }
        else
        {
            Stm r = new SEQ(cond.unCx(t,f),
                    new SEQ(new LABEL(t),
                            new SEQ(new SEQ(th.unNx(),new JUMP(j)),
                    new SEQ(new LABEL(f), 
                            new SEQ(el.unNx(),
                    new LABEL(j))))));
            
            result = new Nx(r);
        }
    }
    
    /*-------*/
    /* WHILE */
    /*-------*/
    public void visit(While node)
    {
        Label test = new Label();
        Label done = new Label();
        Label body = new Label();
        
        Exp cond = ExpHandler.translate(frame, env, cinfo, minfo, node.condition );
        Exp b = StatementHandler.translate(frame, env, cinfo, minfo, node.body);
        
        Stm r = new SEQ(new LABEL(test),
                  new SEQ(cond.unCx(body, done),
                          new SEQ(new LABEL(body),
                                  new SEQ(b.unNx(),
                                          new SEQ(new JUMP(test),
                                                  new LABEL(done))))));
        
        result = new Nx(r);
    }
    
    /*-------*/
    /* PRINT */
    /*-------*/
    public void visit(Print node)
    {
        tree.Exp arg = ExpHandler.translate(frame, env, cinfo, minfo, node.exp).unEx();
        
        List<tree.Exp> param = new List<tree.Exp>(arg, null);
        
        tree.Exp call = frame.externalCall("printInt", param);
        
        result = new Nx(new EXPSTM(call));
    }
    
    /*-------*/
    /* BLOCK */
    /*-------*/
    public void visit(Block node)
    {
        result = StatementListHandler.translate(frame, env, cinfo, minfo, node.body);
    }
    
    /*----------------------*/
    /* ASSIGN, ARRAY ASSIGN */
    /*----------------------*/
    public void visit(Assign node)
    {        
        Symbol name = Symbol.symbol(node.var.s);
        
        tree.Exp var = getVariable(name);
        
        Exp e = ExpHandler.translate(frame, env, cinfo, minfo, node.exp);
        
        Stm s = new MOVE(var, e.unEx());
        
        result = new Nx(s);
    }
    
    public void visit(ArrayAssign node)
    {
        Symbol name = Symbol.symbol(node.var.s);
        
        tree.Exp var = getVariable(name);
        
        Temp index = new Temp();
        
        Temp arr = new Temp();
        
        tree.Exp idx = ExpHandler.translate(frame, env, cinfo, minfo, node.index).unEx();
        tree.Exp val = ExpHandler.translate(frame, env, cinfo, minfo, node.value).unEx();
        
        List<tree.Exp> params = new List<tree.Exp>(new TEMP(arr),
                new List<tree.Exp>(new TEMP(index),
                        new List<tree.Exp>(new CONST(node.line),null)));
        
        tree.Exp arrayIndex = new BINOP(BINOP.LSHIFT, new BINOP(BINOP.PLUS, idx, new CONST(1)), new CONST(2));
        
        tree.Exp access = new MEM(new BINOP(BINOP.PLUS, var, arrayIndex));
        
        List<tree.Exp> asParams = new List<tree.Exp>(new TEMP(arr),
                new List<tree.Exp>(new CONST(node.line), null));
        
        Stm as = new EXPSTM(frame.externalCall("assertPtr", asParams));
        
        Stm s = new SEQ( new MOVE(new TEMP(arr), var),
                        new SEQ(as,
                                new SEQ( new MOVE(new TEMP(index), idx),
                                        new SEQ( new EXPSTM(frame.externalCall("boundCheck", params)),
                                                new MOVE(access, val)))));
        
        result = new Nx(s);
    }
}
