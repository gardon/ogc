package frame;

import java.io.PrintStream;

import assem.Instr;
import util.List;

public abstract class Proc
{
    public Proc next;
    
    public Proc()
    {
        super();
    }

    public abstract String getHeader();
    
    public abstract List<Instr> getBody();
    
    public abstract List<Instr> getPrologue();
    
    public abstract String getFooter();
    
    public abstract List<Instr> getEpilogue();
    
    public abstract void print(PrintStream out, temp.TempMap t);
}
