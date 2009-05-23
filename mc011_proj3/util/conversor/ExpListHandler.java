package util.conversor;

import java.util.LinkedList;

import syntaxtree.Exp;
import util.List;
import minijava.node.PExp;

class ExpListHandler
{
	private ExpListHandler()
	{
		super();
	}

	static List<Exp> convert(LinkedList<PExp> nodes)
	{
		List<Exp> result = null, tail = null, nn;
				
		for ( PExp e : nodes )
		{
			Exp converted = ExpHandler.convert(e);
			
			nn = new List<Exp>(converted, null);
			
			if ( result == null )
				result = tail = nn;
			else
				tail = tail.tail = nn;
			
		}
		
		return result;
	}
}
