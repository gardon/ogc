package util.conversor;

import syntaxtree.ArrayAssign;
import syntaxtree.Assign;
import syntaxtree.Block;
import syntaxtree.Exp;
import syntaxtree.Identifier;
import syntaxtree.If;
import syntaxtree.Print;
import syntaxtree.Statement;
import syntaxtree.While;
import util.List;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AArrayAssignStatement;
import minijava.node.AAssignStatement;
import minijava.node.ABlockStatement;
import minijava.node.AIfStatement;
import minijava.node.APrintStatement;
import minijava.node.AWhileStatement;
import minijava.node.PStatement;
import minijava.node.Token;

class StatementHandler extends AnalysisAdapter
{
	private Statement result;
	
	public StatementHandler()
	{
		super();
	}
	
	static Statement convert(PStatement node)
	{
		StatementHandler h = new StatementHandler();
		
		node.apply(h);
		
		return h.result;
	}

	public void caseABlockStatement(ABlockStatement node)
	{
		Token t = node.getToken();
		List<Statement> ss = StatementListHandler.convert(node.getStatements());
		
		result = new Block(t.getLine(), t.getPos(), ss);
	}
	
	public void caseAWhileStatement(AWhileStatement node)
	{
		Exp c = ExpHandler.convert(node.getCondition());
		Statement b = StatementHandler.convert(node.getBody());
		Token t = node.getToken();
		
		result = new While(t.getLine(), t.getPos(), c, b);
	}
	
	public void caseAIfStatement(AIfStatement node)
	{
		Exp c = ExpHandler.convert(node.getCondition());
		Statement t = StatementHandler.convert(node.getThenClause());
		Statement e = node.getElseClause() == null ? null : StatementHandler.convert(node.getElseClause());
		Token tt = node.getToken();
		
		result = new If(tt.getLine(), tt.getPos(), c, t, e);
	}
	
	public void caseAPrintStatement(APrintStatement node)
	{
		Exp value = ExpHandler.convert(node.getValue());
		Token t = node.getToken();
		
		result = new Print(t.getLine(), t.getPos(), value);
	}
	
	public void caseAAssignStatement(AAssignStatement node)
	{
		Token t = node.getTarget();
		Exp value = ExpHandler.convert(node.getValue());
		
		Identifier id = new Identifier(t.getLine(), t.getPos(), t.getText());
		
		result = new Assign(id.line, id.row, id, value);
	}
	
	public void caseAArrayAssignStatement(AArrayAssignStatement node)
	{
		Token t = node.getTarget();
		Exp index = ExpHandler.convert(node.getIndex());
		Exp value = ExpHandler.convert(node.getValue());
		
		Identifier id = new Identifier(t.getLine(), t.getPos(), t.getText());
		
		result = new ArrayAssign(id.line, id.row, id, index, value);
	}
}
