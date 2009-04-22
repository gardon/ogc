package util.conversor;

import java.util.LinkedList;

import minijava.node.PFormal;

import syntaxtree.Formal;
import util.List;

class FormalListHandler
{
	private FormalListHandler()
	{
		super();
	}

	static List<Formal> convert(LinkedList<PFormal> nodes)
	{
		List<Formal> result = null, tail = null, nn;
		
		for ( PFormal f : nodes )
		{
			Formal aux = FormalHandler.convert(f);
			
			nn = new List<Formal>(aux, null);
			
			if ( result == null )
				result = tail = nn;
			else
				tail = tail.tail = nn;
		}
		return result;
	}
}
