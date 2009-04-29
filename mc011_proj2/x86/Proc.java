package x86;

import java.io.PrintStream;

import assem.Instr;
import util.List;

class Proc extends frame.Proc
{
    String name;
    List<Instr> body;
    List<Instr> prologue;
    List<Instr> epilogue;
    
    public Proc(String n, List<Instr> b, List<Instr> p, List<Instr> e)
    {
        super();
        
        name = n;
        body = b;
        prologue = p;
        epilogue = e;
    }

    public String getHeader()
    {
        return name + ":";
    }

    public String getFooter()
    {
        return "";
    }

    public List<Instr> getBody()
    {
        return body;
    }

    public List<Instr> getPrologue()
    {
        return prologue;
    }

    public List<Instr> getEpilogue()
    {
        return epilogue;
    }

    public void print(PrintStream out, temp.TempMap t)
    {
        out.println();
        out.print(name);
        out.print(":");
        
        for ( List<Instr> aux = prologue; aux != null; aux = aux.tail )
        {
            out.println();
            out.print("    ");
            out.print(aux.head.format(t));
        }
        
        for ( List<Instr> aux = body; aux != null; aux = aux.tail )
        {
            out.println();
            if ( !( aux.head instanceof assem.LABEL ) )
                out.print("    ");
            
            out.print(aux.head.format(t));
        }
        
        for ( List<Instr> aux = epilogue; aux != null; aux = aux.tail )
        {
            out.println();
            out.print("    ");
            out.print(aux.head.format(t));
        }
        
        out.println();
        out.println("    ; end of " + name );
    }
}
