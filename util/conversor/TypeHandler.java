package util.conversor;

import syntaxtree.BooleanType;
import syntaxtree.IdentifierType;
import syntaxtree.IntArrayType;
import syntaxtree.IntegerType;
import syntaxtree.Type;
import minijava.analysis.AnalysisAdapter;
import minijava.node.AArrayType;
import minijava.node.ABooleanType;
import minijava.node.AIntType;
import minijava.node.AObjectType;
import minijava.node.PType;
import minijava.node.Token;

class TypeHandler extends AnalysisAdapter
{
	private Type result;
	
	private TypeHandler()
	{
		super();
	}
	
	static Type convert(PType node)
	{
		TypeHandler h = new TypeHandler();
		
		node.apply(h);
		
		return h.result;
	}
	
	public void caseAIntType(AIntType node)
	{
		Token t = node.getToken();
		
		result = new IntegerType(t.getLine(), t.getPos());
	}
	
	public void caseAArrayType(AArrayType node)
	{
		Token t = node.getToken();
		
		result = new IntArrayType(t.getLine(), t.getPos());
	}

	public void caseABooleanType(ABooleanType node)
	{
		Token t = node.getToken();
		
		result = new BooleanType(t.getLine(), t.getPos());
	}
	
	public void caseAObjectType(AObjectType node)
	{
		Token t = node.getName();
		
		result = new IdentifierType(t.getLine(), t.getPos(), t.getText());
	}
}
