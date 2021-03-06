package x86;

import assem.Instr;
import tree.Stm;
import temp.*;
import frame.*;
import graph.Node;
import util.List;

public class Codegen
{
    Frame frame;
    public Codegen(Frame f)
    {
        frame=f;        
    }

    private List<Instr> ilist=null;
    private List<Instr> last=null;

    private void emit(Instr inst)
    {
        if (last!=null)
            last = last.tail = new List<Instr>(inst,null);
        else 
            last = ilist = new List<Instr>(inst,null);
    }

    private void munchStm (tree.CJUMP cj) {
	/*Compare*/
	if (cj.left instanceof tree.MEM) {
            Temp lt = munchExp(((tree.MEM)cj.left).exp);
            Temp rt = munchExp(cj.right);
            emit( new assem.OPER("cmp [`s0], `s1", null, 
				    new List<Temp>(lt, new List<Temp>(rt, null))));
        } else if (cj.right instanceof tree.CONST) {
            Temp lt = munchExp(cj.left);
            emit( new assem.OPER("cmp `s0," + ((tree.CONST)cj.right).value, 
				    null, new List<Temp>(lt, null)));
        } else {
            Temp lt = munchExp(cj.left);
            Temp rt = munchExp(cj.right);
            emit( new assem.OPER("cmp `s0, `s1", null, 
				    new List<Temp>(lt, new List<Temp>(rt, null))));
        }	

        String op = "";
        switch (tree.CJUMP.notRel (cj.op)) {
        case tree.CJUMP.EQ:  op = "je";
                             break;
        case tree.CJUMP.GE:  op = "jge";
                             break;
        case tree.CJUMP.GT:  op = "jgt";
                             break;
        case tree.CJUMP.LE:  op = "jle";
                             break;
        case tree.CJUMP.LT:  op = "jl";
                             break;
        case tree.CJUMP.NE:  op = "jne";
                             break;
        case tree.CJUMP.UGE:  op = "jae";
                             break;
        case tree.CJUMP.UGT:  op = "ja";
                             break;
        case tree.CJUMP.ULE:  op = "jbe";
                             break;
        case tree.CJUMP.ULT:  op = "jb";
                             break;
        }
        emit (new assem.OPER ("" + op + "`j0", null, null, new List<Label>(cj.ifFalse, new List<Label>(cj.ifTrue,null))));
        emit (new assem.OPER ("jmp `j0", null, null, new List<Label>(cj.ifTrue,null)));;
    }

    private void munchStm (tree.EXPSTM e) {
        munchExp (e.exp);
    }

    private void munchStm (tree.LABEL l) {
        emit (new assem.LABEL ("" + l.label + ":", l.label));
    }

    private void munchStm (tree.JUMP j) {
        if (j.exp instanceof tree.NAME)
            emit (new assem.OPER("jmp `j0", null, null, j.targets));
        else
            throw new Error ("Árvore inválida: jumps só servem para NAMES");
    }

    private void munchStm (tree.MOVE m) {
    	if (m.dst instanceof tree.MEM) {
	    tree.MEM mem = (tree.MEM)m.dst;
	    if ((mem.exp instanceof tree.BINOP) && 
		    (((tree.BINOP)mem.exp).binop == tree.BINOP.PLUS) && 
		    (((tree.BINOP)mem.exp).left instanceof tree.CONST)) {
		tree.BINOP bop = (tree.BINOP)mem.exp;
    		long cst = ((tree.CONST)bop.left).value;
    		Temp dst = munchExp(bop.right);
		Temp src = munchExp(m.src);
		emit(new assem.OPER("mov [`s0 + " + cst + "],`s1", null, 
					new List<Temp>(dst, new List<Temp>(src, null))));
    	    } else if ((mem.exp instanceof tree.BINOP) && 
		    (((tree.BINOP)mem.exp).binop == tree.BINOP.MINUS) && 
		    (((tree.BINOP)mem.exp).left instanceof tree.CONST)) {
    		tree.BINOP bop = (tree.BINOP)mem.exp;
    		long cst = ((tree.CONST)bop.left).value;
    		Temp dst = munchExp(bop.right);
		Temp src = munchExp(m.src);
		emit(new assem.OPER("mov [`s0 - " + cst + "],`s1", null, 
					new List<Temp>(dst, new List<Temp>(src, null))));
    	    } else if ((mem.exp instanceof tree.BINOP) && 
		    (((tree.BINOP)mem.exp).binop == tree.BINOP.PLUS) && 
		    (((tree.BINOP)mem.exp).right instanceof tree.CONST)) {
		tree.BINOP bop = (tree.BINOP)mem.exp;
    		long cst = ((tree.CONST)bop.right).value;
    		Temp dst = munchExp(bop.left);
		Temp src = munchExp(m.src);
		emit(new assem.OPER("mov [`s0 + " + cst + "],`s1", null, 
					new List<Temp>(dst, new List<Temp>(src, null))));
    	    } else if ((mem.exp instanceof tree.BINOP) && 
		    (((tree.BINOP)mem.exp).binop == tree.BINOP.MINUS) && 
		    (((tree.BINOP)mem.exp).right instanceof tree.CONST)) {
    		tree.BINOP bop = (tree.BINOP)mem.exp;
    		long cst = ((tree.CONST)bop.right).value;
    		Temp dst = munchExp(bop.left);
		Temp src = munchExp(m.src);
		emit(new assem.OPER("mov [`s0 - " + cst + "],`s1", null, 
					new List<Temp>(dst, new List<Temp>(src, null))));
    	    } else if (mem.exp instanceof tree.CONST) {
    		Temp src = munchExp(m.src);
		emit(new assem.OPER("mov [" + ((tree.CONST)mem.exp).value  + "],`s0", null, 
					new List<Temp>(src, null)));    			
    	    } else if (m.src instanceof tree.CONST) {
		Temp dst = munchExp(mem.exp);
		emit(new assem.OPER("mov dword [`s0]," + ((tree.CONST)m.src).value, null, 
					new List<Temp>(dst, null)));    			
    	    } else {
		Temp dst = munchExp(mem.exp);
		Temp src = munchExp(m.src);
		emit(new assem.OPER("mov [`s0],`s1", null, 
					new List<Temp>(dst, new List<Temp>(src, null))));
    	    }
	} else if (m.dst instanceof tree.TEMP) {
	    if (m.src instanceof tree.MEM) {
		tree.MEM mem = (tree.MEM)m.src;
		if ((mem.exp instanceof tree.BINOP) && 
			(((tree.BINOP)mem.exp).binop == tree.BINOP.PLUS) && 
			(((tree.BINOP)mem.exp).left instanceof tree.CONST)) {
		    tree.BINOP bop = (tree.BINOP)mem.exp;
		    long cst = ((tree.CONST)bop.left).value;
		    Temp src = munchExp(bop.right);		    	
		    emit(new assem.OPER("mov `d0,[`s0 + " + cst + " ]", 
					    new List<Temp>(((tree.TEMP)m.dst).temp, null),  
					    new List<Temp>(src, null)));
		} else if ((mem.exp instanceof tree.BINOP) && 
			(((tree.BINOP)mem.exp).binop == tree.BINOP.MINUS) && 
			(((tree.BINOP)mem.exp).left instanceof tree.CONST)) {
		    tree.BINOP bop = (tree.BINOP)mem.exp;
		    long cst = ((tree.CONST)bop.left).value;
		    Temp src = munchExp(bop.right);		    	
		    emit(new assem.OPER("mov `d0,[`s0 0 " + cst + " ]", 
					    new List<Temp>(((tree.TEMP)m.dst).temp, null),  
					    new List<Temp>(src, null)));
		} else if ((mem.exp instanceof tree.BINOP) && 
			(((tree.BINOP)mem.exp).binop == tree.BINOP.PLUS) && 
			(((tree.BINOP)mem.exp).right instanceof tree.CONST)) {
		    tree.BINOP bop = (tree.BINOP)mem.exp;
		    long cst = ((tree.CONST)bop.right).value;
		    Temp src = munchExp(bop.left);		    	
		    emit(new assem.OPER("mov `d0,[`s0 + " + cst + " ]", 
					    new List<Temp>(((tree.TEMP)m.dst).temp, null),  
					    new List<Temp>(src, null)));
		} else if ((mem.exp instanceof tree.BINOP) && 
			(((tree.BINOP)mem.exp).binop == tree.BINOP.MINUS) && 
			(((tree.BINOP)mem.exp).right instanceof tree.CONST)) {
		    tree.BINOP bop = (tree.BINOP)mem.exp;
		    long cst = ((tree.CONST)bop.right).value;
		    Temp src = munchExp(bop.left);		    	
		    emit(new assem.OPER("mov `d0,[`s0 - " + cst + " ]", 
					    new List<Temp>(((tree.TEMP)m.dst).temp, null),  
					    new List<Temp>(src, null)));
		} else if (mem.exp instanceof tree.CONST){
		    emit(new assem.OPER("mov `d0,[" + ((tree.CONST)mem.exp).value + "]", 
					    new List<Temp>(((tree.TEMP)m.dst).temp, null), null));
		} else {
		    Temp src = munchExp(mem.exp);		    	
		    emit(new assem.OPER("mov `d0,[`s0]", 
					    new List<Temp>(((tree.TEMP)m.dst).temp, null),  
					    new List<Temp>(src, null)));
		}
	    } else if (m.src instanceof tree.CONST) {
		emit(new assem.OPER("mov `d0," + ((tree.CONST)m.src).value, 
					new List<Temp>(((tree.TEMP)m.dst).temp, null), null)); 
	    } else {
		Temp src = munchExp(m.src);		    	
		emit(new assem.MOVE("mov `d0,`s0", ((tree.TEMP)m.dst).temp, src)); 
	    } 
	} else {
	    throw new Error("MOVE inválido.");
	}
    }

    private void munchStm (tree.SEQ s) {
        munchStm (s.left);
        munchStm (s.right);
    }

    private void munchStm (tree.Stm s) {
        if (s instanceof tree.CJUMP)
            munchStm ((tree.CJUMP) s);
        else if (s instanceof tree.EXPSTM)
            munchStm ((tree.EXPSTM) s);
        else if (s instanceof tree.LABEL)
            munchStm ((tree.LABEL) s);
        else if (s instanceof tree.JUMP)
            munchStm ((tree.JUMP) s);
        else if (s instanceof tree.MOVE)
            munchStm ((tree.MOVE) s);
        else if (s instanceof tree.SEQ)
            munchStm ((tree.SEQ) s);
        else
            throw new Error ("Sentenca inválida.");
    }

    private Temp munchExp(tree.BINOP b){
        Temp opl = munchExp(b.left);
	Temp op =  new Temp();
	switch (b.binop) {
	    case tree.BINOP.MINUS:
		emit( new assem.MOVE("mov `d0,`s0", op, opl));
		if (b.right instanceof tree.CONST) {
		    Long cst = ((tree.CONST)b.right).value;
		    emit( new assem.OPER("sub" + " `d0," + cst + "", 
					    new List<Temp>(op,null), new List<Temp>(op,null)));
		} else {
		    Temp rop = munchExp(b.right);
		    emit( new assem.OPER("sub" + " `d0,`s0", 
					    new List<Temp>(op,null), 
					    new List<Temp>(rop,new List<Temp>(op, null))));
		}	
		return op;
	    case tree.BINOP.PLUS:
		emit( new assem.MOVE("mov `d0,`s0", op, opl));
		if (b.right instanceof tree.CONST) {
		    Long cst = ((tree.CONST)b.right).value;
		    emit( new assem.OPER("add" + " `d0," + cst + "", 
					    new List<Temp>(op,null), new List<Temp>(op,null)));
		} else {
		    Temp rop = munchExp(b.right);
		    emit( new assem.OPER("add" + " `d0,`s0", 
					    new List<Temp>(op,null), 
					    new List<Temp>(rop,new List<Temp>(op, null))));
		}	
		return op;
	    case tree.BINOP.TIMES:
		emit( new assem.MOVE("mov `d0,`s0", op, opl));
		if (b.right instanceof tree.CONST) {
		    Long cst = ((tree.CONST)b.right).value;
		    emit( new assem.OPER("imul" + " `d0," + cst + "", 
					    new List<Temp>(op,null), new List<Temp>(op,null)));
		} else {
		    Temp rop = munchExp(b.right);
		    emit( new assem.OPER("imul" + " `d0,`s0", 
					    new List<Temp>(op,null), 
					    new List<Temp>(rop,new List<Temp>(op, null))));
		}	
		return op;
	    case tree.BINOP.LSHIFT:
		emit( new assem.MOVE("mov `d0, `s0", op, opl) );
		if (b.right instanceof tree.CONST) {
		    Long cst = ((tree.CONST)b.right).value;
		    emit( new assem.OPER("shl" + " `d0," + cst + "", 
					    new List<Temp>(op,null), new List<Temp>(op,null)));
		} else {
                    throw new Error("Shift inválido.");
		}
		return op;
	    default:
                throw new Error("BinOp inválida.");
        }
    }

    private Temp munchExp(tree.CALL c){
	//Temp adr = null;
        //if (!(c.func instanceof tree.NAME)) {
        //        adr = munchExp(c.func);
        //}
	
	List<Temp> targs = null;
        long nparms = 0;
        for(List<tree.Exp> iter = c.args ; iter != null; iter = iter.tail ){
                nparms++;
                Temp arg = munchExp(iter.head);
                targs = new List<Temp>(arg, targs);
        }

        for(List<Temp> iter = targs ; iter != null; iter = iter.tail )
            emit( new assem.OPER("push `s0",  
				    new List<Temp>(Frame.esp, null), 
				    new List<Temp>(iter.head, 
				    new List<Temp>(Frame.esp, null))) );
	
	long spspace = nparms * frame.wordsize();

        if (c.func instanceof tree.NAME) {
            tree.NAME name = (tree.NAME)c.func;
            emit( new assem.OPER("call " + name.label, Frame.calldefs, null));
        } else {
	    Temp adr = munchExp(c.func);
            emit( new assem.OPER("call `s0", Frame.calldefs, new List<Temp>(adr,null)));
	}

	Temp t = new Temp();
        emit( new assem.MOVE("mov `d0,`s0", t, frame.RV()) );
        emit( new assem.OPER("add `d0," + spspace, 
				new List<Temp>(Frame.esp, null), 
				new List<Temp>(Frame.esp, null)));
        return t;
    }

    private Temp munchExp(tree.CONST c){
	Temp t = new Temp();
        emit( new assem.OPER("mov `d0," + c.value, new List<Temp>(t, null), null) );
        return t;
    }

    private Temp munchExp(tree.ESEQ e){
        munchStm(e.stm);
        return munchExp(e.exp);
    }

    private Temp munchExp(tree.MEM m){
	Temp t = new Temp();
        if ((m.exp instanceof tree.BINOP) && 
		(((tree.BINOP)m.exp).binop == tree.BINOP.PLUS) && 
		(((tree.BINOP)m.exp).left instanceof tree.CONST)) {
	    tree.BINOP bop = (tree.BINOP)m.exp;
	    long cst = ((tree.CONST)bop.left).value;
	    Temp texp = munchExp(bop.right);
	    emit( new assem.OPER("mov `d0, [`s0 + " + cst + "]", 
				    new List<Temp>(t, null), 
				    new List<Temp>(texp, null)));
        }else if ((m.exp instanceof tree.BINOP) && 
		(((tree.BINOP)m.exp).binop == tree.BINOP.MINUS) && 
		(((tree.BINOP)m.exp).left instanceof tree.CONST)) {
	    tree.BINOP bop = (tree.BINOP)m.exp;
	    long cst = ((tree.CONST)bop.left).value;
	    Temp texp = munchExp(bop.right);
	    emit( new assem.OPER("mov `d0, [`s0 - " + cst + "]", 
				    new List<Temp>(t, null), 
				    new List<Temp>(texp, null)));
        }else if ((m.exp instanceof tree.BINOP) && 
		(((tree.BINOP)m.exp).binop == tree.BINOP.PLUS) &&
		(((tree.BINOP)m.exp).right instanceof tree.CONST)) {
            tree.BINOP bop = (tree.BINOP)m.exp;
            long cst = ((tree.CONST)bop.right).value;
            Temp texp = munchExp(bop.left);
            emit( new assem.OPER("mov `d0, [`s0 + " + cst + "]", 
				    new List<Temp>(t, null), 
				    new List<Temp>(texp, null)));
        }else if ((m.exp instanceof tree.BINOP) && 
		(((tree.BINOP)m.exp).binop == tree.BINOP.MINUS) && 
		(((tree.BINOP)m.exp).right instanceof tree.CONST)) {
            tree.BINOP bop = (tree.BINOP)m.exp;
            long cst = ((tree.CONST)bop.right).value;
            Temp texp = munchExp(bop.left);
            emit( new assem.OPER("mov `d0, [`s0 - " + cst + "]", 
				    new List<Temp>(t, null), 
				    new List<Temp>(texp, null)));
	}else if (m.exp instanceof tree.CONST) 
            emit( new assem.OPER("mov `d0, [" + ((tree.CONST)m.exp).value + "]", 
				    new List<Temp>(t, null), null));
        else {
            Temp texp = munchExp(m.exp);
            emit( new assem.OPER("mov `d0, [`s0]", 
				    new List<Temp>(t, null), 
				    new List<Temp>(texp, null)));
        }
        return t;
    }

    private Temp munchExp(tree.NAME n){
	Temp t = new Temp();
        emit( new assem.OPER("mov `d0," + n.label, new List<Temp>(t,null), null));
        return t;
    }

    private Temp munchExp(tree.TEMP t){
	return t.temp;
    }

    private Temp munchExp (tree.Exp e) {
        if (e instanceof tree.BINOP) {
                return munchExp((tree.BINOP) e);
        } else if (e instanceof tree.CALL) {
                return munchExp((tree.CALL) e);
        } else if (e instanceof tree.CONST) {
                return munchExp((tree.CONST) e);
        } else if (e instanceof tree.ESEQ) {
                return munchExp((tree.ESEQ) e);
        } else if (e instanceof tree.MEM) {
                return munchExp((tree.MEM) e);
        } else if (e instanceof tree.NAME) {
                return munchExp((tree.NAME) e);
        } else if (e instanceof tree.TEMP) {
                return munchExp((tree.TEMP) e);
        } else {
                throw new Error("Expressão inválida.");
        }
    }


    /*-------------------------------------------------------------*
     *                              MAIN                           *
     *-------------------------------------------------------------*/
    List<Instr> codegen(Stm s)
    {
        List<Instr> l;
        munchStm(s);
        l=ilist;
        ilist=last=null;
        return l;
    }
    
    List<Instr> codegen(List<Stm> body)
    {
        List<Instr> l = null, t = null;
        
        for( ; body != null; body = body.tail )
        {
            munchStm(body.head);
            if ( l == null )
                l = ilist;
            else
                t.tail = ilist;
            t = last;
            ilist=last=null;
        }
        return l;
    }
}
