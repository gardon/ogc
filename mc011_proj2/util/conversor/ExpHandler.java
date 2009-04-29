package util.conversor;

import syntaxtree.And;
import syntaxtree.ArrayLength;
import syntaxtree.ArrayLookup;
import syntaxtree.Call;
import syntaxtree.Equal;
import syntaxtree.Exp;
import syntaxtree.False;
import syntaxtree.Identifier;
import syntaxtree.IdentifierExp;
import syntaxtree.IntegerLiteral;
import syntaxtree.LessThan;
import syntaxtree.Minus;
import syntaxtree.NewArray;
import syntaxtree.NewObject;
import syntaxtree.Not;
import syntaxtree.Plus;
import syntaxtree.This;
import syntaxtree.Times;
import syntaxtree.True;
import util.List;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AAndExp;
import minijava.node.AArrayLengthExp;
import minijava.node.AArrayLookupExp;
import minijava.node.ACallExp;
import minijava.node.AEqualExp;
import minijava.node.AFalseExp;
import minijava.node.AIdentifierExp;
import minijava.node.AIntegerLiteralExp;
import minijava.node.ALessThanExp;
import minijava.node.AMinusExp;
import minijava.node.ANewArrayExp;
import minijava.node.ANewObjectExp;
import minijava.node.ANotExp;
import minijava.node.APlusExp;
import minijava.node.AThisExp;
import minijava.node.ATimesExp;
import minijava.node.ATrueExp;
import minijava.node.PExp;
import minijava.node.Token;

class ExpHandler extends AnalysisAdapter
{
	private Exp result;
	
	private ExpHandler()
	{
		super();
	}

	static Exp convert(PExp node)
	{
		ExpHandler h = new ExpHandler();
		
		node.apply(h);
		
		return h.result;
	}
	
	public void caseAAndExp(AAndExp node)
	{
		Exp lhs = ExpHandler.convert(node.getLhs());
		Exp rhs = ExpHandler.convert(node.getRhs());

		Token t = node.getToken();
		
		result = new And(t.getLine(), t.getPos(), lhs, rhs);
	}
	
	public void caseAEqualExp(AEqualExp node)
	{
		Exp lhs = ExpHandler.convert(node.getLhs());
		Exp rhs = ExpHandler.convert(node.getRhs());

		Token t = node.getToken();
		
		result = new Equal(t.getLine(), t.getPos(), lhs, rhs);
	}
	
	public void caseALessThanExp(ALessThanExp node)
	{
		Exp lhs = ExpHandler.convert(node.getLhs());
		Exp rhs = ExpHandler.convert(node.getRhs());

		Token t = node.getToken();
		
		result = new LessThan(t.getLine(), t.getPos(), lhs, rhs);
	}
	
	public void caseAPlusExp(APlusExp node)
	{
		Exp lhs = ExpHandler.convert(node.getLhs());
		Exp rhs = ExpHandler.convert(node.getRhs());

		Token t = node.getToken();
		
		result = new Plus(t.getLine(), t.getPos(), lhs, rhs);
	}
	
	public void caseAMinusExp(AMinusExp node)
	{
		Exp lhs = ExpHandler.convert(node.getLhs());
		Exp rhs = ExpHandler.convert(node.getRhs());

		Token t = node.getToken();
		
		result = new Minus(t.getLine(), t.getPos(), lhs, rhs);
	}
	
	public void caseATimesExp(ATimesExp node)
	{
		Exp lhs = ExpHandler.convert(node.getLhs());
		Exp rhs = ExpHandler.convert(node.getRhs());

		Token t = node.getToken();
		
		result = new Times(t.getLine(), t.getPos(), lhs, rhs);
	}

	public void caseATrueExp(ATrueExp node)
	{
		Token t = node.getToken();
		
		result = new True(t.getLine(), t.getPos());
	}
	
	public void caseAFalseExp(AFalseExp node)
	{
		Token t = node.getToken();
		
		result = new False(t.getLine(), t.getPos());
	}
	
	public void caseAThisExp(AThisExp node)
	{
		Token t = node.getToken();
		
		result = new This(t.getLine(), t.getPos());
	}
	
	public void caseANotExp(ANotExp node)
	{
		Exp e = ExpHandler.convert(node.getValue());
		Token t = node.getToken();
		
		result = new Not(t.getLine(), t.getPos(), e);
	}
	
	public void caseAIntegerLiteralExp(AIntegerLiteralExp node)
	{
		Token t = node.getValue();
		int v = Integer.valueOf(t.getText()).intValue();
		
		result = new IntegerLiteral(t.getLine(), t.getPos(), v); 
	}
	
	public void caseAIdentifierExp(AIdentifierExp node)
	{
		Token t = node.getName();
		String name = t.getText();
		
		Identifier id = new Identifier( t.getLine(), t.getPos(), name);
		result = new IdentifierExp(id.line, id.row, id);
	}
	
	public void caseACallExp(ACallExp node)
	{
		Exp object = ExpHandler.convert(node.getObject());
		List<Exp> actuals = ExpListHandler.convert(node.getActuals());
		Token method = node.getMethod();
		
		Identifier m = new Identifier(method.getLine(), method.getPos(), method.getText());
		
		result = new Call(m.line, m.row, object, m, actuals);
	}
	
	public void caseANewArrayExp(ANewArrayExp node)
	{
		Exp size = ExpHandler.convert(node.getSize());
		Token t = node.getToken();
		
		result = new NewArray(t.getLine(), t.getPos(), size);
	}
	
	public void caseANewObjectExp(ANewObjectExp node)
	{
		Token t = node.getName();
		
		Identifier n = new Identifier(t.getLine(), t.getPos(), t.getText());
		
		result = new NewObject(n.line, n.row, n);
	}
	
	public void caseAArrayLookupExp(AArrayLookupExp node)
	{
		Exp arr = ExpHandler.convert(node.getArray());
		Exp index = ExpHandler.convert(node.getIndex());
		Token t = node.getToken();
		
		result = new ArrayLookup(t.getLine(), t.getPos(), arr, index);
	}
	
	public void caseAArrayLengthExp(AArrayLengthExp node)
	{
		Exp arr = ExpHandler.convert(node.getArray());
		Token t = node.getToken();
		
		result = new ArrayLength(t.getLine(), t.getPos(), arr);
	}
}
