package x86;

import java.util.Hashtable;

import assem.Instr;
import assem.OPER;

import temp.Label;
import temp.Temp;
import tree.CALL;
import tree.Exp;
import tree.MOVE;
import tree.NAME;
import tree.SEQ;
import tree.Stm;
import tree.TEMP;
import util.List;
import frame.Access;

/*
 * O diagrama abaixo representa a estrutura da pilha num x86 tipico.
 * Cada 'caixa' tem tamanho de 1 palavra (32 bits).
 *  
 * |    ...    |
 * +-----------+
 * |    ...    | <- sp
 * +-----------+ 
 * |    ...    |
 * +-----------+
 * | bp antigo | <- bp
 * +-----------+
 * | end. ret. |
 * +-----------+
 * | param 1   |
 * +-----------+
 * | param 2   |
 * +-----------+
 * |    ...    |
 * 
 */
public class Frame extends frame.Frame
{    
    public Frame parent;
    
    static Temp ebp = new Temp();
    static Temp esp = new Temp();
    static Temp eax = new Temp();
    static Temp ebx = new Temp();
    static Temp ecx = new Temp();
    static Temp edx = new Temp();
    static Temp esi = new Temp();
    static Temp edi = new Temp();
    
    static List<Temp> allRegs;
    
    private static Hashtable<Temp, String> regNames;
    
    static List<Temp> specialRegs;
    
    static List<Temp> callerSave;
    
    static List<Temp> calleeSave;
    
    static List<Temp> argRegs;
    
    static List<Temp> sinkList;
    
    static List<Temp> calldefs;
    
    static
    {
        regNames = new Hashtable<Temp, String>();
        
        regNames.put(eax, "eax");
        regNames.put(ebx, "ebx");
        regNames.put(ecx, "ecx");
        regNames.put(edx, "edx");
        regNames.put(esi, "esi");
        regNames.put(edi, "edi");
        regNames.put(ebp, "ebp");
        regNames.put(esp, "esp");
        
        argRegs = null;
        
        specialRegs = new List<Temp>( eax,
                new List<Temp>(esp,
                        new List<Temp>(ebp, null)));
        
        calleeSave = new List<Temp>( esi,
                new List<Temp>(edi,
                        new List<Temp>(ebx, 
                                null)));
        
        callerSave = new List<Temp>( ecx,
                new List<Temp>(edx, null));
        
        sinkList = new List<Temp>(eax,
                new List<Temp>(esp,
                        new List<Temp>(ebp, calleeSave)));
        
        calldefs = new List<Temp>(eax, callerSave);
        
        allRegs = new List<Temp>( eax,
                new List<Temp>(ebx,
                        new List<Temp>(ecx,
                                new List<Temp>(edx,
                                        new List<Temp>(esi,
                                                new List<Temp>(edi,
                                                        new List<Temp>(ebp,
                                                                new List<Temp>(esp, null))))))));
    }
    
    private long localsCount = 4;
    
    private Frame(Label name, List<Access> formals)
    {
        super();
        
        this.name = name;
        this.formals = formals;
    }
    
    public Frame()
    {
        super();
        
        parent = null;
        name = new Label("");
    }

    public frame.Frame newFrame(Label name, List<Boolean> formals)
    {               
        InFrame tAccess;
        
        List<Access> head = null, tail = null, nn;

        // criando a lista de 'Access' que representam os parametros deste 'frame'
        // o layout da pilha no x86 está especificado acima.
        for ( int i = 8; formals != null; formals = formals.tail, i += 4)
        {
            tAccess = new InFrame();
            tAccess.offset = i;
            
            nn = new List<Access>(tAccess, null);

            if ( head == null )
                head = tail = nn;
            else
                tail = tail.tail = nn;
        }
        
        Frame f = new Frame(name, head);
        
        f.parent = this;
        
        return f;
    }

    // apesar de somente termos 6 registradores,
    // por padrao as variaveis locais vao para temporários.
    private Stm localsInit = null;
    
    public Access allocLocal(boolean escapes)
    {
        InFrame retFrame = null;
        InReg retReg = null;
        Access ret;
        
        if (escapes)
        {
            retFrame = new InFrame();
            
            retFrame.offset = - localsCount;
            localsCount += 4;
            
            ret = retFrame;
        }
        else
        {
            retReg = new InReg();
            retReg.temp = new Temp();
            ret = retReg;
        }
        
        tree.Stm move = new tree.MOVE(ret.exp(new TEMP(this.FP())),new tree.CONST(0));
        
        if ( localsInit == null )
            localsInit = move;
        else
            localsInit = new SEQ(move, localsInit);
        
        return ret;
    }

    public int wordsize()
    {
        return 4;
    }

    public Temp FP()
    {
        return Frame.ebp;
    }

    // supondo que as rotinas externas são compiladas por um compilador
    // C que decora os nomes das funcoes geradas com um '_'
    public Exp externalCall(String s, List<Exp> args)
    {
        String name = "_" + s;
        
        return new CALL(new NAME(new Label(name)), args);
    }

    public Temp RV()
    {
        return Frame.eax;
    }

    public Stm procEntryExit1(tree.Exp body)
    {
        Temp tebx = new Temp();
        Temp tesi = new Temp();
        Temp tedi = new Temp();
        
        Stm save;
        
        if ( localsInit == null )
            save = new SEQ(new MOVE(new TEMP(tebx), new TEMP(Frame.ebx)),
                    new SEQ(new MOVE(new TEMP(tesi), new TEMP(Frame.esi)),
                        new MOVE(new TEMP(tedi), new TEMP(Frame.edi))));
        else
            save = new SEQ(new MOVE(new TEMP(tebx), new TEMP(Frame.ebx)),
                    new SEQ(new MOVE(new TEMP(tesi), new TEMP(Frame.esi)),
                        new SEQ( new MOVE(new TEMP(tedi), new TEMP(Frame.edi)),
                                localsInit)));

        Stm restore = new SEQ(new MOVE(new TEMP(Frame.ebx), new TEMP(tebx)),
                new SEQ(new MOVE(new TEMP(Frame.esi), new TEMP(tesi)),
                        new MOVE(new TEMP(Frame.edi), new TEMP(tedi))));

        
        return new SEQ(save,
                new SEQ( new MOVE(new TEMP(this.RV()), body),
                        restore));
    }

    public String tempMap(Temp t)
    {
        return Frame.regNames.get(t);
    }

    static List<Instr> append(List<Instr> a, List<Instr> b)
    {
        if ( a == null )
            return b;
        else
        {
            List<Instr> p;
            for ( p = a; p.tail != null; p = p.tail )
                /*NADA*/;
            p.tail = b;
            
            return a;
        }
    }
    public List<Instr> procEntryExit2(List<Instr> body)
    {
        return Frame.append(body,
                new List<Instr>(new OPER("",null,Frame.sinkList), null));
    }

    public List<Instr> codegen(List<Stm> body)
    {
        return new Codegen(this).codegen(body);
    }

    public List<Temp> registers()
    {
        return Frame.allRegs;
    }

    public frame.Proc procEntryExit3(List<Instr> body)
    {
        List<Instr> p = new List<Instr>(new OPER("push `s0", new List<Temp>(Frame.esp, null), new List<Temp>(Frame.ebp, new List<Temp>(Frame.esp, null))), 
                new List<Instr>(new assem.MOVE("mov `d0, `s0", Frame.ebp, Frame.esp), 
                        new List<Instr>(new OPER("sub `d0, " + (this.localsCount-4), new List<Temp>(Frame.esp, null), new List<Temp>(Frame.esp, null)),
                                null )));
        
        List<Instr> e = new List<Instr>(new OPER("add `d0, " + (this.localsCount-4), new List<Temp>(Frame.esp, null), new List<Temp>(Frame.esp, null)),
                new List<Instr>(new OPER("pop `d0", new List<Temp>(Frame.ebp, null), new List<Temp>(Frame.ebp, new List<Temp>(Frame.esp, null))),
                        new List<Instr>(new OPER("ret", new List<Temp>(Frame.esp, null), new List<Temp>(Frame.ebp, new List<Temp>(Frame.esp, null))),
                                null)));
        
        return new Proc("" + name, body, p, e);
    }

}
