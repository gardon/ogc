package translate;

import temp.Label;

public class VtableFrag extends Frag
{
    public Label name;
    public String[] vtable;
    
    public VtableFrag(String n, String[] v)
    {
        super();
        
        vtable = v;
        name = new Label("vtable_" + n);
    }
    
    public String toString()
    {
        StringBuffer b = new StringBuffer();
        
        b.append(name);
        b.append(":");
        
        for ( String s : vtable )
        {
            b.append("\n\tdw\t");
            b.append(s);
        }
        
        return b.toString();
    }
}
