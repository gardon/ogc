package util.conversor;

import minijava.node.PProgram;
import minijava.node.Start;
import syntaxtree.Program;

public final class SyntaxTreeGenerator
{
	private SyntaxTreeGenerator()
	{
	}

	public static Program convert(PProgram node)
	{
		return ProgramHandler.convert(node);
	}
	
	public static Program convert(Start node)
	{
		return ProgramHandler.convert(node.getPProgram());
	}
}
