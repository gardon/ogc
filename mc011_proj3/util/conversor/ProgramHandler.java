package util.conversor;

import syntaxtree.ClassDecl;
import syntaxtree.MainClass;
import syntaxtree.Program;
import util.List;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AProgram;
import minijava.node.PProgram;

class ProgramHandler extends AnalysisAdapter
{
	private Program result;
	
	private ProgramHandler()
	{
	}

	static Program convert(PProgram node)
	{
		ProgramHandler h = new ProgramHandler();
		
		node.apply(h);
		
		return h.result;
	}
	
	public void caseAProgram(AProgram node)
	{
		MainClass m = MainClassHandler.convert(node.getMainClass());
		List<ClassDecl> c = ClassListHandler.convert(node.getClasses());
		
		result = new Program(1, 1, m, c);
	}
}
