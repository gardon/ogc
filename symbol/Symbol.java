package symbol;

import java.util.Hashtable;

public class Symbol
{
	private static Hashtable<String, Symbol> dict = new Hashtable<String, Symbol>();
	
	private String name;
	
	private Symbol(String n)
	{
		super();
		
		name = n;
	}

	public static Symbol symbol(String n)
	{
		String u = n.intern();
		
		Symbol s = dict.get(u);
		
		if ( s == null )
			dict.put(u, s = new Symbol(u) );
		
		return s;
	}
	
	public String toString()
	{
		return name;
	}
}
