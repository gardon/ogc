package util.conversor;

import java.util.LinkedList;

import minijava.node.PMethodDecl;

import syntaxtree.MethodDecl;
import util.List;

class MethodListHandler
{
	private MethodListHandler()
	{
		super();
	}

	static List<MethodDecl> convert(LinkedList<PMethodDecl> nodes)
	{
		List<MethodDecl> result = null, tail = null, nn;
		
		for ( PMethodDecl m : nodes )
		{
			MethodDecl aux = MethodHandler.convert(m);
			
			nn = new List<MethodDecl>(aux, null);
			
			if ( result == null )
				result = tail = nn;
			else
				tail = tail.tail = nn;
		}
		
		return result;
	}
}
