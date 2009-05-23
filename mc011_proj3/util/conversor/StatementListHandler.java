package util.conversor;

import java.util.LinkedList;

import minijava.node.PStatement;

import syntaxtree.Statement;
import util.List;

class StatementListHandler
{
	private StatementListHandler()
	{
	}

	static List<Statement> convert(LinkedList<PStatement> nodes)
	{
		List<Statement> result = null, tail = null, nn;
		
		for ( PStatement s : nodes )
		{
			Statement aux = StatementHandler.convert(s);
			
			nn = new List<Statement>(aux, null);
			
			if ( result == null )
				result = tail = nn;
			else
				tail = tail.tail = nn;
		}
		
		return result;
	}
}
