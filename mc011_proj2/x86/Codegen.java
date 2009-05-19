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
        munchCmp (cj.left, cj.right);

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
        // munchMove
    }

    private void munchStm (tree.SEQ s) {
        munchStm (s.left);
        munchStm (s.right);
    }

    private void munchStm (tree.Stm s) {
        if (s instanceof tree.CJUMP)
            munchStm ((tree.CJUMP) s);
        if (s instanceof tree.EXPSTM)
            munchStm ((tree.EXPSTM) s);
        if (s instanceof tree.LABEL)
            munchStm ((tree.LABEL) s);
        if (s instanceof tree.JUMP)
            munchStm ((tree.JUMP) s);
        if (s instanceof tree.MOVE)
            munchStm ((tree.MOVE) s);
        if (s instanceof tree.SEQ)
            munchStm ((tree.SEQ) s);
        else
            throw new Error ("Sentenca inválida.");
    }

    private void munchCmp (tree.Exp l, tree.Exp r) {
    }

    private Temp munchExp(tree.BINOP e){
    return null;
    }
    private Temp munchExp(tree.CALL e){
    return null;
    }
    private Temp munchExp(tree.CONST e){
    return null;
    }
    private Temp munchExp(tree.ESEQ e){
    return null;
    }
    private Temp munchExp(tree.MEM e){
    return null;
    }
    private Temp munchExp(tree.NAME e){
    return null;
    }
    private Temp munchExp(tree.TEMP e){
    return null;
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
