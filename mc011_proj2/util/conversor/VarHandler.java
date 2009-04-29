package util.conversor;

import syntaxtree.Identifier;
import syntaxtree.Type;
import syntaxtree.VarDecl;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AVarDecl;
import minijava.node.PVarDecl;
import minijava.node.Token;

class VarHandler extends AnalysisAdapter
{
	private VarDecl result;
	
	private VarHandler()
	{
		super();
		result = null;
	}

	static VarDecl convert(PVarDecl node)
	{
		VarHandler h = new VarHandler();
		
		node.apply(h);
		
		return h.result;
	}
	
	public void caseAVarDecl(AVarDecl node)
	{
		Type t = TypeHandler.convert(node.getType());
		Token name = node.getName();
		Identifier n = new Identifier(name.getLine(), name.getPos(), name.getText());
		
		result = new VarDecl(t.line, t.row, t, n);
	}
}
