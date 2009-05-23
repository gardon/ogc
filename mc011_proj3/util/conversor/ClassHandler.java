package util.conversor;

import syntaxtree.ClassDecl;
import syntaxtree.ClassDeclExtends;
import syntaxtree.ClassDeclSimple;
import syntaxtree.Identifier;
import syntaxtree.MethodDecl;
import syntaxtree.VarDecl;
import util.List;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AClassDecl;
import minijava.node.PClassDecl;
import minijava.node.Token;

class ClassHandler extends AnalysisAdapter
{
	private ClassDecl result;
	
	private ClassHandler()
	{
		super();
	}

	static ClassDecl convert(PClassDecl node)
	{
		ClassHandler h = new ClassHandler();
		
		node.apply(h);
		
		return h.result;
	}
	
	public void caseAClassDecl(AClassDecl node)
	{
		Token cn = node.getName();
		Identifier c = new Identifier(cn.getLine(), cn.getPos(), cn.getText());
		Token sn = node.getSuper();
		Identifier s = sn == null ? null : new Identifier(sn.getLine(), sn.getPos(), sn.getText());
		List<VarDecl> a = VarListHandler.convert(node.getAttributes());
		List<MethodDecl> m = MethodListHandler.convert(node.getMethods());
		
		if (s == null)
			result = new ClassDeclSimple(c.line, c.row, c, a, m);
		else
			result = new ClassDeclExtends(c.line, c.row, c, s, a, m);
	}
}
