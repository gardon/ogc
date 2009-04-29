package assem;

import temp.Temp;
import util.List;

public class MOVE extends Instr
{
    public List<Temp> dst;   
    public List<Temp> src;

    public MOVE(String a, Temp d, Temp s)
    {
        assem=a; 
        dst=new List<Temp>(d, null); 
        src=new List<Temp>(s, null);
    }
    
    public List<Temp> use()
    {
        return src;
    }
    
    public List<Temp> def()
    {
        return dst;
    }
    
    public Targets jumps()
    {
        return null;
    }
}
