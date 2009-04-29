package assem;

import temp.Label;
import temp.Temp;
import util.List;

public class OPER extends Instr
{
    public List<Temp> dst;   
    public List<Temp> src;
    public Targets jump;

    public OPER(String a, List<Temp> d, List<Temp> s, List<Label> j)
    {
        assem=a; 
        dst=d; 
        src=s; 
        jump=new Targets(j);
    }
    
    public OPER(String a, List<Temp> d, List<Temp> s)
    {
        assem=a;
        dst=d;
        src=s;
        jump=null;
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
        return jump;
    }
}
