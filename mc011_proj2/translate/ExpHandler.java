package translate;

import frame.Frame;
import semant.Env;
import symbol.ClassInfo;
import symbol.MethodInfo;
import symbol.Symbol;
import symbol.VarInfo;
import syntaxtree.And;
import syntaxtree.ArrayLength;
import syntaxtree.ArrayLookup;
import syntaxtree.Call;
import syntaxtree.Equal;
import syntaxtree.False;
import syntaxtree.IdentifierExp;
import syntaxtree.IdentifierType;
import syntaxtree.IntegerLiteral;
import syntaxtree.LessThan;
import syntaxtree.Minus;
import syntaxtree.NewArray;
import syntaxtree.NewObject;
import syntaxtree.Not;
import syntaxtree.Plus;
import syntaxtree.This;
import syntaxtree.Times;
import syntaxtree.True;
import syntaxtree.VisitorAdapter;
import temp.Label;
import temp.Temp;
import tree.BINOP;
import tree.CALL;
import tree.CJUMP;
import tree.CONST;
import tree.ESEQ;
import tree.EXPSTM;
import tree.JUMP;
import tree.LABEL;
import tree.MEM;
import tree.MOVE;
import tree.NAME;
import tree.SEQ;
import tree.TEMP;
import util.List;

class ExpHandler extends VisitorAdapter
{
    private Exp result;
    
    private Env env;
    private ClassInfo cinfo;
    private MethodInfo minfo;
    private Frame frame;
    
    private tree.Exp getVariable(Symbol name)
    {
        if ( minfo != null )
        {
            VarInfo v = minfo.localsTable.get(name);
            
            if ( v != null )
                return v.access.exp(new TEMP(frame.FP()));
            
            v = minfo.formalsTable.get(name);
            
            if ( v != null )
                return v.access.exp(new TEMP(frame.FP()));
        }
        
        // se esta aqui, a variavel eh um atributo de classe
        tree.Exp t = minfo.thisPtr.exp(new TEMP(frame.FP()));
        
        int offset = cinfo.getAttributeOffset(name);
        
        tree.Exp addr = new BINOP(BINOP.PLUS, t, new BINOP(BINOP.LSHIFT, new CONST(offset), new CONST(2)));
        
        tree.Exp fetch = new MEM(addr);
        
        return fetch;
    }
    
    private tree.Exp getMethod(tree.Exp obj, Call node, ClassInfo c, MethodInfo m, List<tree.Exp> args)
    {
        Temp thisPtr = new Temp();
        
        tree.Exp vtable = new MEM(new BINOP(BINOP.PLUS, new TEMP(thisPtr), new CONST(0)));
        
        int index = c.getMethodOffset(m.name);
        
        tree.Exp methodOffset = new BINOP(BINOP.LSHIFT, new CONST(index), new CONST(2));
        
        MEM methodAddr = new MEM(new BINOP(BINOP.PLUS, vtable, methodOffset));
        
        args = new List<tree.Exp>( new TEMP(thisPtr), args); 
        
        Temp rv = new Temp();
        
        List<tree.Exp> params = new List<tree.Exp>(new TEMP(thisPtr),
                new List<tree.Exp>(new CONST(node.line), null));
        
        tree.Stm as = new EXPSTM(frame.externalCall("assertPtr", params));
        
        return new ESEQ(
                new SEQ(new MOVE(new TEMP(thisPtr), obj),
                        new SEQ( as,
                                new SEQ( new EXPSTM( 
                                        new CALL(methodAddr, args) ) , 
                                        new MOVE( new TEMP(rv), 
                                                new TEMP(frame.RV()))))), 
               new TEMP(rv) );
    }
    
    private ExpHandler(Frame f, Env e, ClassInfo c, MethodInfo m)
    {
        super();
        
        frame = f;
        env = e;
        cinfo = c;
        minfo = m;
        
        result = null;
    }
    
    static Exp translate(Frame f, Env e, ClassInfo c, MethodInfo m, syntaxtree.Exp node)
    {
        ExpHandler h = new ExpHandler(f, e, c, m);
        
        node.accept(h);
        
        return h.result;
    }
    
    /*-----*/
    /* AND */
    /*-----*/
    public void visit(And node)
    {
        Temp res = new Temp();
        
        Exp lhs = ExpHandler.translate(frame, env, cinfo, minfo, node.lhs);
        Exp rhs = ExpHandler.translate(frame, env, cinfo, minfo, node.rhs);
        
        Label f = new Label();        
        Label secondPart = new Label();
        Label join = new Label();
        Label t = new Label();
        
        tree.Exp total = new ESEQ(
                new SEQ(new CJUMP(CJUMP.NE, lhs.unEx(), new CONST(0), secondPart, f),
                        new SEQ(new LABEL(secondPart),
                                new SEQ(rhs.unCx(t, f),
                                        new SEQ(new LABEL(f),
                                                new SEQ(new MOVE(new TEMP(res), new CONST(0)),
                                                        new SEQ(new JUMP(join),
                                                                new SEQ(new LABEL(t),
                                                                        new SEQ(new MOVE(new TEMP(res), new CONST(1)),
                                                                                new LABEL(join))))))))), new TEMP(res));
        
        result = new Ex(total);
    }
    
    /*-------*/
    /* EQUAL */
    /*-------*/
    public void visit(Equal node)
    {
        Exp lhs = ExpHandler.translate(frame, env, cinfo, minfo, node.lhs);
        Exp rhs = ExpHandler.translate(frame, env, cinfo, minfo, node.rhs);
        
        int op = CJUMP.EQ;
        
        result = new RelCx(op, lhs, rhs);        
    }
    
    /*-----------*/
    /* LESS THAN */
    /*-----------*/
    public void visit(LessThan node)
    {
        Exp lhs = ExpHandler.translate(frame, env, cinfo, minfo, node.lhs);
        Exp rhs = ExpHandler.translate(frame, env, cinfo, minfo, node.rhs);
        
        int op = CJUMP.LT;
        
        result = new RelCx(op, lhs, rhs);        
    }
    
    /*--------------------*/
    /* PLUS, MINUS, TIMES */
    /*--------------------*/
    public void visit(Plus node)
    {
        Exp lhs = ExpHandler.translate(frame, env, cinfo, minfo, node.lhs);
        Exp rhs = ExpHandler.translate(frame, env, cinfo, minfo, node.rhs);
        
        tree.Exp cmp = new BINOP(BINOP.PLUS, lhs.unEx(), rhs.unEx());
        
        result = new Ex(cmp);
    }
    
    public void visit(Minus node)
    {
        Exp lhs = ExpHandler.translate(frame, env, cinfo, minfo, node.lhs);
        Exp rhs = ExpHandler.translate(frame, env, cinfo, minfo, node.rhs);
        
        tree.Exp cmp = new BINOP(BINOP.MINUS, lhs.unEx(), rhs.unEx());
        
        result = new Ex(cmp);
    }
    
    public void visit(Times node)
    {
        Exp lhs = ExpHandler.translate(frame, env, cinfo, minfo, node.lhs);
        Exp rhs = ExpHandler.translate(frame, env, cinfo, minfo, node.rhs);
        
        tree.Exp cmp = new BINOP(BINOP.TIMES, lhs.unEx(), rhs.unEx());
        
        result = new Ex(cmp);
    }
    
    /*-----------------*/
    /* INTEGER LITERAL */
    /*-----------------*/
    public void visit(IntegerLiteral node)
    {
        result = new Ex(new CONST(node.value));
    }
    
    /*-------------------*/
    /* TRUE, FALSE, THIS */
    /*-------------------*/
    public void visit(True node)
    {
        result = new Ex(new CONST(1));
    }
    
    public void visit(False node)
    {
        result = new Ex(new CONST(0));
    }
    
    public void visit(This node)
    {
        result = new Ex(minfo.thisPtr.exp(new TEMP(frame.FP())));
    }
    
    /*-----------------------*/
    /* NEW OBJECT, NEW ARRAY */
    /*-----------------------*/
    public void visit(NewObject node)
    {
        Symbol s = Symbol.symbol(node.className.s);
        
        ClassInfo c = env.classes.get(s);
        
        // tamanho do objeto: numero de atributos + 1 palavras
        int tamanho = (c.attributesOrder.size() + 1) * frame.wordsize();
        
        Label vtableName = new Label("vtable_" + c.name);
        
        List<tree.Exp> params = new List<tree.Exp>(new CONST(tamanho), 
                new List<tree.Exp>(new NAME(vtableName),null));
        
        tree.Exp e = frame.externalCall("newObject", params);
        
        result = new Ex( e );
    }
    
    public void visit(NewArray node)
    {
        tree.Exp size = ExpHandler.translate(frame, env, cinfo, minfo, node.size).unEx();
        
        List<tree.Exp> params = new List<tree.Exp>(size, null);
        
        Temp t = new Temp();
        
        tree.Exp e = new ESEQ(new MOVE(new TEMP(t), frame.externalCall("newArray", params)),
                new TEMP(t));
        
        result = new Ex( e );
    }
    
    /*------------*/
    /* IDENTIFIER */
    /*------------*/
    public void visit(IdentifierExp node)
    {
        Symbol name = Symbol.symbol(node.name.s);
        
        tree.Exp fetch = getVariable(name);
        
        result = new Ex(fetch);
    }
    
    /*----------------------------*/
    /* ARRAY LOOKUP, ARRAY LENGTH */
    /*----------------------------*/
    public void visit(ArrayLength node)
    {
        tree.Exp array = ExpHandler.translate(frame, env, cinfo, minfo, node.array).unEx();
        
        Temp arr = new Temp();
        
        Temp size = new Temp();
        
        List<tree.Exp> params = new List<tree.Exp>(new TEMP(arr),
                new List<tree.Exp>(new CONST(node.line), null));
        
        
        tree.Stm move = new MOVE(new TEMP(arr), array);
        tree.Stm as = new EXPSTM(frame.externalCall("assertPtr", params));
        tree.Stm s = new MOVE(new TEMP(size),
                new BINOP(BINOP.PLUS, new TEMP(arr), new CONST(0)));
        
        tree.Stm aux = new SEQ(move, new SEQ(as,s));
        
        tree.Exp fetchSize = new ESEQ(aux, new MEM(new TEMP(size)));
        
        result = new Ex(fetchSize);
    }
    
    public void visit(ArrayLookup node)
    {
        tree.Exp array = ExpHandler.translate(frame, env, cinfo, minfo, node.array).unEx();
        tree.Exp index = ExpHandler.translate(frame, env, cinfo, minfo, node.index).unEx();
        
        Temp arrayTemp = new Temp();
        Temp indexTemp = new Temp();
        
        List<tree.Exp> params = new List<tree.Exp>(new TEMP(arrayTemp),
                new List<tree.Exp>(new TEMP(indexTemp),
                        new List<tree.Exp>(new CONST(node.line),null)));
        
        List<tree.Exp> asParams = new List<tree.Exp>(new TEMP(arrayTemp),
                new List<tree.Exp>(new CONST(node.line), null));
        
        tree.Stm as = new EXPSTM(frame.externalCall("assertPtr", asParams));
        
        // tudo isso para fazer verificacao de limites...
        SEQ s = new SEQ(new MOVE(new TEMP(arrayTemp), array), 
                new SEQ(as,
                        new SEQ(new MOVE(new TEMP(indexTemp),index),
                                new EXPSTM(frame.externalCall("boundCheck",params)))));
        
        // faz o 'fetch' da posicao
        ESEQ fetch = new ESEQ(s, 
                new MEM(new BINOP(BINOP.PLUS, 
                        new TEMP(arrayTemp),
                        new BINOP(BINOP.LSHIFT, 
                                new BINOP(BINOP.PLUS, new TEMP(indexTemp), new CONST(1)),
                                new CONST(2)))));
        
        result = new Ex(fetch);
    }
    
    /*-----*/
    /* NOT */
    /*-----*/
    public void visit(Not node)
    {
        Temp r = new Temp();
        Label t = new Label();
        Label f = new Label();
        Label join = new Label();
        
        SEQ s = new SEQ( ExpHandler.translate(frame, env, cinfo, minfo, node.exp ).unCx(t,f),
                new SEQ(new LABEL(t),
                        new SEQ(new MOVE(new TEMP(r), new CONST(0)), 
                                new SEQ(new JUMP(join),
                                        new SEQ(new LABEL(f),
                                                new SEQ(new MOVE(new TEMP(r), new CONST(1)),
                                                        new LABEL(join)))))));
        
        ESEQ res = new ESEQ(s, new TEMP(r));
        
        result = new Ex(res);
    }
    
    /*------*/
    /* CALL */
    /*------*/
    public void visit(Call node)
    {
        tree.Exp thisPtr = ExpHandler.translate(frame, env, cinfo, minfo, node.object).unEx();
        
        IdentifierType type = (IdentifierType) node.object.type;
        
        ClassInfo ci = env.classes.get(Symbol.symbol(type.name));
        
        MethodInfo mi = ci.methods.get( Symbol.symbol(node.method.s) );
        
        List<tree.Exp> params = ExpListHandler.translate(frame, env, cinfo, minfo, node.actuals);
        
        result = new Ex(getMethod(thisPtr, node, ci, mi, params));
    }
}
