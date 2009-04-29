package x86;

import assem.*;
import tree.*;
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
   
    /**
     * Maximal-munch para uma operação do tipo +,- ou *
     * 
     * @param instr mnemonico da instrução
     * @param l operando da esquerda
     * @param r operando da direita
     * @return temporario com valor de retorno
     */
    private Temp binArith (String instr, Exp l, Exp r) {
    	Temp opl = munchExp(l);
    	Temp op =  new Temp();
    	emit( new assem.MOVE("mov `d0,`s0", op, opl));
    	
    	if (r instanceof tree.CONST) {
    		/*
    		 *         OP
    		 *        /  \
    		 *     exp    CONST
    		 */
	    	Long cst = ((tree.CONST)r).value;
	    		
	    	emit( new assem.OPER("" + instr + " `d0," + cst + "", new List<Temp>(op,null), new List<Temp>(op,null)));	
    	} else {
    		/*
    		 *         OP
    		 *        /  \
    		 *     exp    exp
    		 */
	    	Temp rop = munchExp(r);   	
	    	emit( new assem.OPER("" + instr + " `d0,`s0", new List<Temp>(op,null), new List<Temp>(rop,new List<Temp>(op, null))));
    	}

    	return op;
    }
    
    private Temp binShift (String instr, Exp l, Exp r) {
    	Temp op = munchExp(l);
    	Temp t = new Temp();
    	emit( new assem.MOVE("mov `d0, `s0", t, op) );    	
    	
    	if (r instanceof tree.CONST) {
    		/*
    		 *         OP
    		 *        /  \
    		 *     exp    CONST
    		 */
	    	Long cst = ((tree.CONST)r).value;
	    		
	    	emit( new assem.OPER("" + instr + " `d0," + cst + "", new List<Temp>(t,null), new List<Temp>(t,null)));	
    	} else {
    		/*
    		 *         OP
    		 *        /  \
    		 *     exp    exp
    		 */
			throw new Error("Shift inválido na árvore intermediária.");
    	}
    	
    	return t;
    }
    
    private Temp munchBinop (tree.BINOP b) {
    	switch (b.binop) {
    	case tree.BINOP.MINUS:
    		return binArith("sub", b.left, b.right);
    	case tree.BINOP.PLUS:
    		return binArith("add", b.left, b.right);
    	case tree.BINOP.TIMES:
    		return binArith("imul", b.left, b.right);
    	case tree.BINOP.LSHIFT:
    		return binShift("shl", b.left, b.right);
    	default:
			throw new Error("Operação Binária inválida na árvore intermediária.");
    	}
    }  
    
    /**
     * Maximal-munch do call. (Essa função é o inferno na terra, mude uma virgula de 
     * lugar e se prepare para passar dias vendo seu programa capotar)
     * 
     * @param c No raiz
     * @return temporario com resultado da chamada
     */
    private Temp munchCall (tree.CALL c) {
    	// Calcula endereço de retorno
    	Temp adr = null;
    	if (!(c.func instanceof tree.NAME)) {
    		adr = munchExp(c.func);
    	}
    	
    	// Processa Parametros e conta
		List<Temp> targs = null;
    	long nparms = 0;
    	for(List<tree.Exp> iter = c.args ; iter != null; iter = iter.tail ) 
    	{
    		nparms++;
	    	Temp arg = munchExp(iter.head);
	    	targs = new List<Temp>(arg, targs);
    	}
    	
    	for(List<Temp> iter = targs ; iter != null; iter = iter.tail ) 
    	{
    		emit( new assem.OPER("push `s0",  new List<Temp>(Frame.esp, null), new List<Temp>(iter.head, new List<Temp>(Frame.esp, null))) );
    	}
    	
    	// Calcula o espaço alocado na o pilha
    	long spspace = nparms * frame.wordsize();
    	
    	if (c.func instanceof tree.NAME) {
	    	tree.NAME name = (tree.NAME)c.func;
	    	emit( new assem.OPER("call " + name.label, Frame.calldefs, null));
    	} else {
	    	emit( new assem.OPER("call `s0", Frame.calldefs, new List<Temp>(adr,null)));    		
    	}
    	
    	// Retorna o parametro
    	Temp t = new Temp();
    	emit( new assem.MOVE("mov `d0,`s0", t, frame.RV()) );
    	
    	// Desaloca espaco na pilha
    	emit( new assem.OPER("add `d0," + spspace, new List<Temp>(Frame.esp, null), new List<Temp>(Frame.esp, null)));
    	
    	return t;
    }
    
    /**
     * Maximal-Munch do Jump condicional. Esse jump condifional me tomou 3 noites
     * inteiras. Eu tinha esquecido de fazer a lista com os labels contendo os dois
     * destinos de saida e isso acabava com o liveness analisy.
     * 
     * @param cj
     */
    private void munchCJump (tree.CJUMP cj) {
    	
    	munchCmp(cj.left, cj.right);
    	
    	String op = "";
    	switch (tree.CJUMP.notRel(cj.op)) {
    	case tree.CJUMP.EQ:
    		op = "je";
    		break;
    	case tree.CJUMP.GE:
    		op = "jge";
    		break;
    	case tree.CJUMP.GT:
    		op = "jgt";
    		break;
    	case tree.CJUMP.LE:
    		op = "jle";
    		break;
    	case tree.CJUMP.LT:
    		op = "jl";
    		break;
    	case tree.CJUMP.NE:
    		op = "jne";
    		break;
    	case tree.CJUMP.UGE:
    		op = "jae";
    		break;
    	case tree.CJUMP.UGT:
    		op = "ja";
    		break;   
    	case tree.CJUMP.ULE:
    		op = "jbe";
    		break;    	
    	case tree.CJUMP.ULT:
    		op = "jb";
    		break;    	 		
    	}

		emit( new OPER("" + op + " `j0", null, null, new List<Label>(cj.ifFalse,new List<Label>(cj.ifTrue,null))));    	
    	emit( new OPER("jmp `j0", null, null, new List<Label>(cj.ifTrue,null)));;     	
    }  
    
    private Temp munchConst (tree.CONST b) {
    	/*
    	 *          |
    	 *        CONST
    	 */
    	
    	Temp t = new Temp();
    	emit( new assem.OPER("mov `d0," + b.value, new List<Temp>(t, null), null) );
    	
    	return t;
    }
    
    /**
     * Maximal munch de um compare
     * 
     * @param l operando esquerdo
     * @param r operando direito
     */
    private void munchCmp (tree.Exp l, tree.Exp r) {
    	
    	if (l instanceof tree.MEM) {
    		/*
    		 *           |
    		 *          CMP
    		 *         /   \
    		 *      MEM     exp
    		 *       |
    		 *      exp
    		 */

    		Temp lt = munchExp(((tree.MEM)l).exp);
	    	Temp rt = munchExp(r);
	    	
	    	emit( new assem.OPER("cmp [`s0], `s1", null, new List<Temp>(lt, new List<Temp>(rt, null))));  	
    	} else if (r instanceof tree.CONST) {
    		/*
    		 *           |
    		 *          CMP
    		 *         /   \
    		 *      exp     CONST
    		 */        	
	    	Temp lt = munchExp(l);
	    	
	    	emit( new assem.OPER("cmp `s0," + ((tree.CONST)r).value, null, new List<Temp>(lt, null)));
    	} else {
    		/*
    		 *           |
    		 *          CMP
    		 *         /   \
    		 *      exp     exp
    		 */
    	
	    	Temp lt = munchExp(l);
	    	Temp rt = munchExp(r);
	    	
	    	emit( new assem.OPER("cmp `s0, `s1", null, new List<Temp>(lt, new List<Temp>(rt, null))));
    	}
    }
    
    private Temp munchESeq (tree.ESEQ e) {
    	munchStm(e.stm);
    	return munchExp(e.exp);
    }
    
    private void munchExpStm (tree.EXPSTM e) {
    	munchExp(e.exp);
    }
    
    /**
     * No referente a um jmp
     * 
     * @param j
     */
    private void munchJump (tree.JUMP j) {
    	if (j.exp instanceof tree.NAME) {
    		/*
    		 *    JUMP
    		 *     |
    		 *    NAME
    		 */
    		emit( new OPER("jmp `j0", null, null, j.targets));
    	} else {
    		/*
    		 *    JUMP
    		 *     |
    		 *    exp
    		 */
    		throw new Error("Árvore intermediária inválida, os jumps devem ser feitos sempre pra NAMES.");
    	}
    }
    
    /**
     * Maximal-munch para um nó de uma Label, deve-se simplesmente gerar a Label
     * 
     * @param l No cabeça
     */
    private void munchLabel (tree.LABEL l) {
    	/*
    	 *             |
    	 *           LABEL
    	 */
    	 emit( new assem.LABEL("" + l.label + ":", l.label) );
    }  
    
    /**
     * Maximal-munch para um nó de um acesso a memória. Este nó não
     * deve ser chamado caso esteja do lado esquerdo de um move. Em todo os
     * outros caso, este nó retorna um temporário com o conteúdo da memória.
     * 
     * @param m Nó cabeça
     * @return Temporário com o conteúdo
     */
    private Temp munchMem (tree.MEM m) {
    	Temp t = new Temp();
    	if ((m.exp instanceof tree.BINOP) && (((tree.BINOP)m.exp).binop == tree.BINOP.PLUS) && (((tree.BINOP)m.exp).left instanceof tree.CONST)) {
	    	/*
	    	 *            |
	    	 *           MEM
	    	 *            |
	    	 *          BINOP
	    	 *         /  |  \
	    	 *      CONST +  exp
	    	 */
    		tree.BINOP bop = (tree.BINOP)m.exp;
    		long cst = ((tree.CONST)bop.left).value;
	    	Temp texp = munchExp(bop.right);    	
	    	emit( new assem.OPER("mov `d0, [`s0 + " + cst + "]", new List<Temp>(t, null), new List<Temp>(texp, null)));
    		
    	} else if ((m.exp instanceof tree.BINOP) && (((tree.BINOP)m.exp).binop == tree.BINOP.MINUS) && (((tree.BINOP)m.exp).left instanceof tree.CONST)) {
	    	/*
	    	 *            |
	    	 *           MEM
	    	 *            |
	    	 *          BINOP
	    	 *         /  |  \
	    	 *      CONST -  exp
	    	 */
    		tree.BINOP bop = (tree.BINOP)m.exp;
    		long cst = ((tree.CONST)bop.left).value;
	    	Temp texp = munchExp(bop.right);    	
	    	emit( new assem.OPER("mov `d0, [`s0 - " + cst + "]", new List<Temp>(t, null), new List<Temp>(texp, null)));
    		
    	} else if ((m.exp instanceof tree.BINOP) && (((tree.BINOP)m.exp).binop == tree.BINOP.PLUS) && (((tree.BINOP)m.exp).right instanceof tree.CONST)) {
	    	/*
	    	 *            |
	    	 *           MEM
	    	 *            |
	    	 *          BINOP
	    	 *         /  |  \
	    	 *       exp  +  CONST
	    	 */
    		tree.BINOP bop = (tree.BINOP)m.exp;
    		long cst = ((tree.CONST)bop.right).value;
	    	Temp texp = munchExp(bop.left);    	
	    	emit( new assem.OPER("mov `d0, [`s0 + " + cst + "]", new List<Temp>(t, null), new List<Temp>(texp, null)));
    		
    	} else if ((m.exp instanceof tree.BINOP) && (((tree.BINOP)m.exp).binop == tree.BINOP.MINUS) && (((tree.BINOP)m.exp).right instanceof tree.CONST)) {
	    	/*
	    	 *            |
	    	 *           MEM
	    	 *            |
	    	 *          BINOP
	    	 *         /  |  \
	    	 *       exp  -  CONST
	    	 */
    		tree.BINOP bop = (tree.BINOP)m.exp;
    		long cst = ((tree.CONST)bop.right).value;
	    	Temp texp = munchExp(bop.left);    	
	    	emit( new assem.OPER("mov `d0, [`s0 - " + cst + "]", new List<Temp>(t, null), new List<Temp>(texp, null)));
    		
    	} else if (m.exp instanceof tree.CONST) {
	    	/*
	    	 *            |
	    	 *           MEM
	    	 *            |
	    	 *          CONST
	    	 */
	    	emit( new assem.OPER("mov `d0, [" + ((tree.CONST)m.exp).value + "]", new List<Temp>(t, null), null));    		
    	} else {
	    	/*
	    	 *            |
	    	 *           MEM
	    	 *            |
	    	 *           exp
	    	 */
	    	Temp texp = munchExp(m.exp);    	
	    	emit( new assem.OPER("mov `d0, [`s0]", new List<Temp>(t, null), new List<Temp>(texp, null)));
    	}
    	
    	return t;
    }  
    
    /**
     * Maximal munch de moves, este aqui é o local onde mais se pode achar formas
     * de coberturas. Além disso, se analisar os códigos gerados, os movs são as
     * instruções mais usadas, portanto vale a pena gastar um pouco de tempo aqui.
     * 
     * @param m
     */
    private void munchMove (tree.MOVE m) {
    	if (m.dst instanceof tree.MEM) {
    		tree.MEM mem = (tree.MEM)m.dst;
    		if ((mem.exp instanceof tree.BINOP) && (((tree.BINOP)mem.exp).binop == tree.BINOP.PLUS) && (((tree.BINOP)mem.exp).left instanceof tree.CONST)) {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *     MEM  exp
				 *       |
				 *     BINOP
				 *    /  |  \
				 * CONST  +  exp
				 */
    			tree.BINOP bop = (tree.BINOP)mem.exp;
    			long cst = ((tree.CONST)bop.left).value;
    			Temp dst = munchExp(bop.right);
		    	Temp src = munchExp(m.src);
		    	
		    	emit(new assem.OPER("mov [`s0 + " + cst + "],`s1", null, new List<Temp>(dst, new List<Temp>(src, null))));
    		} else if ((mem.exp instanceof tree.BINOP) && (((tree.BINOP)mem.exp).binop == tree.BINOP.MINUS) && (((tree.BINOP)mem.exp).left instanceof tree.CONST)) {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *     MEM  exp
				 *       |
				 *     BINOP
				 *    /  |  \
				 * CONST -  exp
				 */
    			tree.BINOP bop = (tree.BINOP)mem.exp;
    			long cst = ((tree.CONST)bop.left).value;
    			Temp dst = munchExp(bop.right);
		    	Temp src = munchExp(m.src);
		    	
		    	emit(new assem.OPER("mov [`s0 - " + cst + "],`s1", null, new List<Temp>(dst, new List<Temp>(src, null))));
    		} else if ((mem.exp instanceof tree.BINOP) && (((tree.BINOP)mem.exp).binop == tree.BINOP.PLUS) && (((tree.BINOP)mem.exp).right instanceof tree.CONST)) {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *     MEM  exp
				 *      |
				 *    BINOP
				 *   /  |  \
				 * exp  +  CONST
				 */
    			tree.BINOP bop = (tree.BINOP)mem.exp;
    			long cst = ((tree.CONST)bop.right).value;
    			Temp dst = munchExp(bop.left);
		    	Temp src = munchExp(m.src);
		    	
		    	emit(new assem.OPER("mov [`s0 + " + cst + "],`s1", null, new List<Temp>(dst, new List<Temp>(src, null))));
    		} else if ((mem.exp instanceof tree.BINOP) && (((tree.BINOP)mem.exp).binop == tree.BINOP.MINUS) && (((tree.BINOP)mem.exp).right instanceof tree.CONST)) {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *     MEM  exp
				 *      |
				 *    BINOP
				 *   /  |  \
				 * exp  -  CONST
				 */
    			tree.BINOP bop = (tree.BINOP)mem.exp;
    			long cst = ((tree.CONST)bop.right).value;
    			Temp dst = munchExp(bop.left);
		    	Temp src = munchExp(m.src);
		    	
		    	emit(new assem.OPER("mov [`s0 - " + cst + "],`s1", null, new List<Temp>(dst, new List<Temp>(src, null))));
    		} else if (mem.exp instanceof tree.CONST) {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *     MEM  exp
				 *      |
				 *    CONST 
				 */
    			
    			Temp src = munchExp(m.src);
			    	
			    emit(new assem.OPER("mov [" + ((tree.CONST)mem.exp).value  + "],`s0", null, new List<Temp>(src, null)));    			
    		} else if (m.src instanceof tree.CONST) {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *     MEM  CONST
				 *      |
				 *     exp 
				 */
		    	Temp dst = munchExp(mem.exp);
			    	
			    emit(new assem.OPER("mov dword [`s0]," + ((tree.CONST)m.src).value, null, new List<Temp>(dst, null)));    			
    		} else {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *     MEM  exp
				 *      |
				 *     exp 
				 */
		    	Temp dst = munchExp(mem.exp);
		    	Temp src = munchExp(m.src);
		    	
		    	emit(new assem.OPER("mov [`s0],`s1", null, new List<Temp>(dst, new List<Temp>(src, null))));
    		}
		} else if (m.dst instanceof tree.TEMP) {
			if (m.src instanceof tree.MEM) 
			{
				tree.MEM mem = (tree.MEM)m.src;
				if ((mem.exp instanceof tree.BINOP) && (((tree.BINOP)mem.exp).binop == tree.BINOP.PLUS) && (((tree.BINOP)mem.exp).left instanceof tree.CONST)) {
	
					/*
					 *        |
					 *       MOVE
					 *       /  \
					 *    TEMP   MEM  
					 *            |
					 *          BINOP
					 *         /  |  \
					 *     CONST  +  exp 
					 */
	    			tree.BINOP bop = (tree.BINOP)mem.exp;
	    			long cst = ((tree.CONST)bop.left).value;
					Temp src = munchExp(bop.right);		    	
			    	emit(new assem.OPER("mov `d0,[`s0 + " + cst + " ]", new List<Temp>(((tree.TEMP)m.dst).temp, null),  new List<Temp>(src, null)));				
				} else if ((mem.exp instanceof tree.BINOP) && (((tree.BINOP)mem.exp).binop == tree.BINOP.MINUS) && (((tree.BINOP)mem.exp).left instanceof tree.CONST)) {
	
					/*
					 *        |
					 *       MOVE
					 *       /  \
					 *    TEMP   MEM  
					 *            |
					 *          BINOP
					 *         /  |  \
					 *     CONST  -  exp 
					 */
	    			tree.BINOP bop = (tree.BINOP)mem.exp;
	    			long cst = ((tree.CONST)bop.left).value;
					Temp src = munchExp(bop.right);		    	
			    	emit(new assem.OPER("mov `d0,[`s0 0 " + cst + " ]", new List<Temp>(((tree.TEMP)m.dst).temp, null),  new List<Temp>(src, null)));				
				} else if ((mem.exp instanceof tree.BINOP) && (((tree.BINOP)mem.exp).binop == tree.BINOP.PLUS) && (((tree.BINOP)mem.exp).right instanceof tree.CONST)) {
	
					/*
					 *        |
					 *       MOVE
					 *       /  \
					 *    TEMP   MEM  
					 *            |
					 *          BINOP
					 *         /  |  \
					 *       exp  +  CONST 
					 */
	    			tree.BINOP bop = (tree.BINOP)mem.exp;
	    			long cst = ((tree.CONST)bop.right).value;
					Temp src = munchExp(bop.left);		    	
			    	emit(new assem.OPER("mov `d0,[`s0 + " + cst + " ]", new List<Temp>(((tree.TEMP)m.dst).temp, null),  new List<Temp>(src, null)));					
				} else if ((mem.exp instanceof tree.BINOP) && (((tree.BINOP)mem.exp).binop == tree.BINOP.MINUS) && (((tree.BINOP)mem.exp).right instanceof tree.CONST)) {
	
					/*
					 *        |
					 *       MOVE
					 *       /  \
					 *    TEMP   MEM  
					 *            |
					 *          BINOP
					 *         /  |  \
					 *       exp  -  CONST 
					 */
	    			tree.BINOP bop = (tree.BINOP)mem.exp;
	    			long cst = ((tree.CONST)bop.right).value;
					Temp src = munchExp(bop.left);		    	
			    	emit(new assem.OPER("mov `d0,[`s0 - " + cst + " ]", new List<Temp>(((tree.TEMP)m.dst).temp, null),  new List<Temp>(src, null)));					
				} else if (mem.exp instanceof tree.CONST){
					/*
					 *        |
					 *       MOVE
					 *       /  \
					 *    TEMP   MEM  
					 *            |
					 *          CONST 
					 */		    	
			    	emit(new assem.OPER("mov `d0,[" + ((tree.CONST)mem.exp).value + "]", new List<Temp>(((tree.TEMP)m.dst).temp, null), null));
				} else {
					/*
					 *        |
					 *       MOVE
					 *       /  \
					 *    TEMP   MEM  
					 *            |
					 *           exp 
					 */
					Temp src = munchExp(mem.exp);		    	
			    	emit(new assem.OPER("mov `d0,[`s0]", new List<Temp>(((tree.TEMP)m.dst).temp, null),  new List<Temp>(src, null)));
				}
			} else if (m.src instanceof tree.CONST) {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *    TEMP   CONST 
				 */
				emit(new assem.OPER("mov `d0," + ((tree.CONST)m.src).value, new List<Temp>(((tree.TEMP)m.dst).temp, null), null)); 
			} else {
				/*
				 *        |
				 *       MOVE
				 *       /  \
				 *    TEMP   exp 
				 */
				Temp src = munchExp(m.src);		    	
		    	emit(new assem.MOVE("mov `d0,`s0", ((tree.TEMP)m.dst).temp, src)); 
			} 
		} else {
    		throw new Error("Árvore intermediária com um MOVE invalido.");
		}

    }  
    
    private Temp munchName (tree.NAME n) {
    	Temp t = new Temp();
    	emit( new assem.OPER("mov `d0," + n.label, new List<Temp>(t,null), null));    	
    	return t;
    }  
    
    private void munchSeq (tree.SEQ s) {
    	/*
    	 *            |
    	 *           SEQ
    	 *          /   \
    	 *        Stm   Stm
    	 */
    	munchStm(s.left);
    	munchStm(s.right);
    }  
    
    private Temp munchTemp (tree.TEMP t) {
    	return t.temp;
    }

    private Temp munchExp (tree.Exp e) {
    	if (e instanceof tree.BINOP) {
    		return munchBinop((tree.BINOP) e);
    	} else if (e instanceof tree.CALL) {
    		return munchCall((tree.CALL) e);
    	} else if (e instanceof tree.CONST) {
    		return munchConst((tree.CONST) e);
    	} else if (e instanceof tree.ESEQ) {
    		return munchESeq((tree.ESEQ) e);
    	} else if (e instanceof tree.MEM) {
    		return munchMem((tree.MEM) e);
    	} else if (e instanceof tree.NAME) {
    		return munchName((tree.NAME) e);
    	} else if (e instanceof tree.TEMP) {
    		return munchTemp((tree.TEMP) e);
    	} else {
    		throw new Error("Expression invalida na árvore intermediária.");
    	}
    }
    
    private void munchStm (tree.Stm s) {
    	if (s instanceof tree.CJUMP) {
    		munchCJump((tree.CJUMP) s);
    	} else if (s instanceof tree.EXPSTM) {
    		munchExpStm((tree.EXPSTM) s);
    	} else if (s instanceof tree.LABEL) {
    		munchLabel((tree.LABEL) s);
    	} else if (s instanceof tree.JUMP) {
    		munchJump((tree.JUMP) s);
    	} else if (s instanceof tree.MOVE) {
    		munchMove((tree.MOVE) s);
    	} else if (s instanceof tree.SEQ) {
    		munchSeq((tree.SEQ) s);
    	} else {
    		throw new Error("Statement invalido na árvore intermediária.");
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
