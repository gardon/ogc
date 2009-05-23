package translate;

import frame.Frame;
import tree.Stm;

public class ProcFrag extends Frag
{
    public Stm body;
    public Frame frame;
    
    public ProcFrag(Stm b, Frame f)
    {
        super();
        
        body = b;
        frame = f;
    }

    public String toString()
    {
        StringBuffer b = new StringBuffer();
        
        b.append("PROC ");
        b.append(frame.name);
        b.append("\n");
        // como imprimir a IR?
        b.append("\tENDP\n");
        
        return b.toString();
    }
}
