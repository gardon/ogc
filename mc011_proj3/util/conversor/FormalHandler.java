package util.conversor;

import syntaxtree.Formal;
import syntaxtree.Identifier;
import syntaxtree.Type;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AFormal;
import minijava.node.PFormal;
import minijava.node.Token;

class FormalHandler extends AnalysisAdapter
{
	private Formal result;
	
	public FormalHandler()
	{
	}

	static Formal convert(PFormal node)
	{
		FormalHandler h = new FormalHandler();
		
		node.apply(h);
		
		return h.result;
	}
	
	public void caseAFormal(AFormal node)
	{
		Type t = TypeHandler.convert(node.getType());
		
		Token tt = node.getName();
		
		Identifier id = new Identifier(tt.getLine(), tt.getPos(), tt.getText());
		
		result = new Formal(t.line, t.row, t, id);
	}
}
