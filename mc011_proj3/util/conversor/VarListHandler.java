package util.conversor;

import java.util.LinkedList;

import minijava.node.PVarDecl;

import syntaxtree.VarDecl;
import util.List;

class VarListHandler
{
	private VarListHandler()
	{
		super();
	}

	static List<VarDecl> convert(LinkedList<PVarDecl> nodes)
	{
		List<VarDecl> result = null, tail = null, nn;
		
		for ( PVarDecl v : nodes )
		{
			VarDecl d = VarHandler.convert(v);
			
			nn = new List<VarDecl>(d, null);
			
			if ( result == null )
				result = tail = nn;
			else
				tail = tail.tail = nn;
		}
		
		return result;
	}
}
