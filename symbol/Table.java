package symbol;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

public class Table<B>
{
	private Stack<Hashtable<Symbol,B>> env;
	
	public Table()
	{
		super();
		
		env = new Stack<Hashtable<Symbol,B>>();
		
		//criando o ambiente inicial
		env.push(new Hashtable<Symbol,B>());
	}

	public boolean put(Symbol key, B value)
	{
		if (env.peek().containsKey(key))
			return false;
		
		env.peek().put(key, value);
		
		return true;
	}
	
	public B get(Symbol key)
	{
		for (int i = env.size() - 1; i >= 0; i--)
			if (env.get(i).containsKey(key))
				return env.peek().get(key);
		
		return null;
	}
	
	public void beginScope()
	{
		env.push(new Hashtable<Symbol,B>());
	}
	
	public void endScope()
	{
		env.pop();
	}
	
	public Enumeration<Symbol> keys()
	{
		return env.peek().keys();
	}
}
