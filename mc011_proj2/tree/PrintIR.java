package tree;

import java.io.PrintStream;

import temp.Label;
import util.List;

public class PrintIR
{
    private PrintStream out;
    
    private void print(Object o)
    {
        out.print(o);
    }

    private void ident(int i)
    {
        i *= 4;
        for ( ; i > 0; i-- )
            out.print(' ');
    }
    
    private void println()
    {
        out.println();
    }
    
    public PrintIR(PrintStream o)
    {
        super();
        
        out = o;
    }

    public void printStatement(Stm s)
    {
        printStatement(s, 0);
        println();
    }
    
    private void printStatement(Stm s, int i)
    {
        if ( s == null )
            return;
        
        if ( s instanceof CJUMP )
            printStatement( (CJUMP) s, i );
        else if ( s instanceof EXPSTM )
            printStatement( (EXPSTM) s, i );
        else if ( s instanceof JUMP )
            printStatement( (JUMP) s, i );
        else if ( s instanceof LABEL )
            printStatement( (LABEL) s, i );
        else if ( s instanceof MOVE )
            printStatement( (MOVE) s, i );
        else if ( s instanceof SEQ )
            printStatement( (SEQ) s, i );
        else
            throw new Error("Unexpected: " + s.getClass());
    }
    
    /*
     * imprime
     * 
     * (cjump.**
     *     exp1,
     *     exp2,
     *     ifTrue,
     *     ifFalse
     * cjump.end) 
     */
    private void printStatement(CJUMP s, int i)
    {
        ident(i);
        switch(s.op)
        {
            case CJUMP.EQ: print("(cjump.eq"); break;
            case CJUMP.NE: print("(cjump.ne"); break;
            case CJUMP.LT: print("(cjump.lt"); break;
            case CJUMP.LE: print("(cjump.le"); break;
            case CJUMP.GT: print("(cjump.gt"); break;
            case CJUMP.GE: print("(cjump.ge"); break;
            default:       print("(cjump.??");
        }
        println();
        
        printExp(s.left, i+1);
        print(",");
        println();
        
        printExp(s.right, i+1);
        print(",");
        println();
        
        ident(i+1);
        print(s.ifTrue);
        print(",");
        println();
        
        ident(i+1);
        print(s.ifFalse);
        println();
        
        ident(i);
        print("cjump.end)");
    }
    
    /*
     * imprime
     * 
     * (expstm
     *     exp
     * expstm)
     */
    private void printStatement(EXPSTM s, int i)
    {
        ident(i);
        print("(expstm");
        println();
        
        printExp(s.exp, i+1);
        println();
        
        ident(i);
        print("expstm)");
    }
    
    /*
     * imprime
     * 
     * (jump
     *     exp,
     *     label 1,
     *     label 2,
     *     ...
     *     label n
     * jump)
     */
    private void printStatement(JUMP s, int i)
    {
        ident(i);
        print("(jump");
        println();
        
        printExp(s.exp, i+1);
        print(",");
        println();
        
        
        for ( List<Label> aux = s.targets; aux != null; aux = aux.tail )
        {
            ident(i+1);
            print(aux.head.toString());
            
            if ( aux.tail != null )
                print(",");
            
            println();
        }
        
        ident(i);
        print("jump)");
    }
    
    /*
     * imprime
     * 
     * (label
     *     label
     * label)
     */
    private void printStatement(LABEL s, int i)
    {
        ident(i);
        print("(label");
        println();
        
        ident(i+1);
        print(s.label.toString());
        println();
        
        ident(i);
        print("label)");
    }

    /*
     * imprime
     * 
     * (move
     *    destination,
     *    source
     * move)
     */
    private void printStatement(MOVE s, int i)
    {
        ident(i);
        print("(move");
        println();
        
        printExp(s.dst, i+1);
        print(",");
        println();
        
        printExp(s.src, i+1);
        println();
        
        ident(i);
        print("move)");
    }
    
    /*
     * imprime
     * 
     * (seq
     *     stm1,
     *     stm2
     * seq)
     * 
     *  OU
     *  
     *  stm1
     */
    private void printStatement(SEQ s, int i)
    {
        if ( s.right == null )
            printStatement(s.left, i);
        else
        {
            ident(i);
            print("(seq");
            println();
            
            printStatement(s.left, i+1);
            print(",");
            println();
            
            printStatement(s.right, i+1);
            println();
            
            ident(i);
            print("seq)");
        }
    }
    
    private void printExp(Exp e, int i)
    {
        if ( e == null )
            return;
        
        if ( e instanceof BINOP )
            printExp( (BINOP) e, i );
        else if ( e instanceof CALL )
            printExp( (CALL) e, i );
        else if ( e instanceof CONST )
            printExp( (CONST) e, i );
        else if ( e instanceof ESEQ )
            printExp( (ESEQ) e, i );
        else if ( e instanceof MEM )
            printExp( (MEM) e, i );
        else if ( e instanceof NAME )
            printExp( (NAME) e, i );
        else if ( e instanceof TEMP )
            printExp( (TEMP) e, i );
        else
            throw new Error("Unexpected: " + e.getClass());
    }
    
    /*
     * imprime
     * 
     * (binop.**
     *     exp1,
     *     exp2
     * binop.end)
     */
    private void printExp(BINOP e, int i)
    {
        ident(i);
        switch(e.binop)
        {
            case BINOP.AND:     print("(binop.and");     break;
            case BINOP.ARSHIFT: print("(binop.arshift"); break;
            case BINOP.DIV:     print("(binop.div");     break;
            case BINOP.LSHIFT:  print("(binop.lshift");  break;
            case BINOP.MINUS:   print("(binop.minus");   break;
            case BINOP.OR:      print("(binop.or");      break;
            case BINOP.PLUS:    print("(binop.plus");    break;
            case BINOP.RSHIFT:  print("(binop.rshift");  break;
            case BINOP.TIMES:   print("(binop.times");   break;
            case BINOP.XOR:     print("(binop.xor");     break;
            default:            print("(binop.?");       break;
        }
        println();
        
        printExp(e.left, i+1);
        print(",");
        println();
        
        printExp(e.right, i+1);
        println();
        
        ident(i);
        print("binop.end)");
    }
    
    /*
     * imprime
     * 
     * (call
     *     method,
     *     param1,
     *     param2,
     *     ...
     *     paramN
     * call)
     */
    private void printExp(CALL e, int i)
    {
        ident(i);
        print("(call");
        println();
        
        printExp(e.func, i+1);
        print(",");
        println();
        
        for( List<Exp> aux = e.args; aux != null; aux = aux.tail )
        {
            printExp(aux.head, i+1);
            
            if ( aux.tail != null )
                print(",");
            
            println();
        }
        
        ident(i);
        print("call)");
    }
    
    /*
     * imprime
     * 
     * (const
     *     const
     * const)
     */
    private void printExp(CONST e, int i)
    {
        ident(i);
        print("(const");
        println();
        
        ident(i+1);
        print("" + e.value);
        println();
        
        ident(i);
        print("const)");
    }
    
    /*
     * imprime
     * 
     * (eseq
     *     stm,
     *     exp
     * eseq)
     */
    private void printExp(ESEQ e, int i)
    {
        ident(i);
        print("(eseq");
        println();
        
        printStatement(e.stm, i+1);
        print(",");
        println();
        
        printExp(e.exp, i+1);
        println();
        
        ident(i);
        print("eseq)");
    }
    
    /*
     * imprime
     * 
     * (mem
     *     exp
     * mem)
     */
    private void printExp(MEM e, int i)
    {
        ident(i);
        print("(mem");
        println();
        
        printExp(e.exp, i+1);
        println();
        
        ident(i);
        print("mem)");
    }
    
    /*
     * imprime
     * 
     * (name
     *     label
     * name)
     */
    private void printExp(NAME e, int i)
    {
        ident(i);
        print("(name");
        println();
        
        ident(i+1);
        print(e.label);
        println();
        
        ident(i);
        print("name)");
    }
    
    /*
     * imprime
     * 
     * (temp
     *     temp
     * temp)
     */
    private void printExp(TEMP e, int i)
    {
        ident(i);
        print("(temp");
        println();
        
        ident(i+1);
        print(e.temp);
        println();
        
        ident(i);
        print("temp)");
    }
}
