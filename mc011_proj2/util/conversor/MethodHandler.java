package util.conversor;

import syntaxtree.Exp;
import syntaxtree.Formal;
import syntaxtree.Identifier;
import syntaxtree.MethodDecl;
import syntaxtree.Statement;
import syntaxtree.Type;
import syntaxtree.VarDecl;
import util.List;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AMethodDecl;
import minijava.node.PMethodDecl;
import minijava.node.Token;

class MethodHandler extends AnalysisAdapter
{
	private MethodDecl result;
	
	private MethodHandler()
	{
		super();
		result = null;
	}

	static MethodDecl convert(PMethodDecl node)
	{
		MethodHandler h = new MethodHandler();
		
		node.apply(h);
		
		return h.result;
	}
	
	public void caseAMethodDecl(AMethodDecl node)
	{
		Type rt = TypeHandler.convert(node.getReturnType());
		Token method = node.getName();
		Identifier m = new Identifier(method.getLine(), method.getPos(), method.getText());
		List<Formal> f = FormalListHandler.convert(node.getFormals());
		List<VarDecl> l = VarListHandler.convert(node.getLocals());
		List<Statement> b = StatementListHandler.convert(node.getBody());
		Exp r = ExpHandler.convert(node.getReturnExp());
		
		result = new MethodDecl(rt.line, rt.row, rt, m, f, l, b, r);
	}
}
