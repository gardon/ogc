package util.conversor;

import java.util.LinkedList;

import minijava.node.PClassDecl;

import syntaxtree.ClassDecl;
import util.List;

class ClassListHandler
{
	private ClassListHandler()
	{
		super();
	}

	static List<ClassDecl> convert(LinkedList<PClassDecl> nodes)
	{
		List<ClassDecl> result = null, tail = null, nn;
		
		for ( PClassDecl c : nodes )
		{
			ClassDecl aux = ClassHandler.convert(c);
			
			nn = new List<ClassDecl>(aux, null);
			
			if ( result == null )
				result = tail = nn;
			else
				tail = tail.tail = nn;
		}
		return result;
	}
}
