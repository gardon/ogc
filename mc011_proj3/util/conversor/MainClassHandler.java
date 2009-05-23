package util.conversor;

import syntaxtree.Identifier;
import syntaxtree.MainClass;
import syntaxtree.Statement;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AMainClass;
import minijava.node.PMainClass;
import minijava.node.Token;

class MainClassHandler extends AnalysisAdapter
{
	private MainClass result;
	
	private MainClassHandler()
	{
		super();
		result = null;
	}

	static MainClass convert(PMainClass node)
	{
		MainClassHandler h = new MainClassHandler();
		
		node.apply(h);
		
		return h.result;
	}
	
	public void caseAMainClass(AMainClass node)
	{
		Token cn = node.getName();
		Identifier c = new Identifier(cn.getLine(), cn.getPos(), cn.getText());
		Token ma = node.getMainArgs();
		Identifier m = new Identifier(ma.getLine(), ma.getPos(), ma.getText());
		Statement b = StatementHandler.convert(node.getStatement());
		
		result = new MainClass(c.line, c.row, c, m, b);
	}
}
