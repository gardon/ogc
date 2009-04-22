package syntaxtree;

import java.io.PrintStream;

import syntaxtree.And;
import syntaxtree.ArrayAssign;
import syntaxtree.ArrayLength;
import syntaxtree.ArrayLookup;
import syntaxtree.Assign;
import syntaxtree.Block;
import syntaxtree.BooleanType;
import syntaxtree.Call;
import syntaxtree.ClassDecl;
import syntaxtree.ClassDeclExtends;
import syntaxtree.ClassDeclSimple;
import syntaxtree.Equal;
import syntaxtree.Exp;
import syntaxtree.False;
import syntaxtree.Formal;
import syntaxtree.Identifier;
import syntaxtree.IdentifierExp;
import syntaxtree.IdentifierType;
import syntaxtree.If;
import syntaxtree.IntArrayType;
import syntaxtree.IntegerLiteral;
import syntaxtree.IntegerType;
import syntaxtree.LessThan;
import syntaxtree.MainClass;
import syntaxtree.MethodDecl;
import syntaxtree.Minus;
import syntaxtree.NewArray;
import syntaxtree.NewObject;
import syntaxtree.Not;
import syntaxtree.Plus;
import syntaxtree.Print;
import syntaxtree.Program;
import syntaxtree.Statement;
import syntaxtree.This;
import syntaxtree.Times;
import syntaxtree.True;
import syntaxtree.VarDecl;
import syntaxtree.While;
import util.List;
import visitor.Visitor;

public class PrettyPrint implements Visitor
{
    PrintStream out;
    
	private int ident;
	private boolean printSpace;
	
	private void beginNest()
	{
		ident += 4;
	}
	
	private void endNest()
	{
		ident -= 4;
	}
	
	private void print(String s)
	{
		if ( printSpace )
			for (int i = 0; i < ident; i++ )
				out.print(" ");
		out.print(s);
		
		printSpace = false;
	}
	
	private void println(String s)
	{
		if ( printSpace )
			for (int i = 0; i < ident; i++ )
				out.print(" ");
		out.println(s);
		
		printSpace = true;
	}
	
	public PrettyPrint(PrintStream p)
	{
		super();
        out = p;
		ident = 0;
		printSpace = true;
	}
    
    public PrettyPrint()
    {
        this(System.out);
    }

	public void visit(Program node)
	{
		node.mainClass.accept(this);
		for ( List<ClassDecl> aux = node.classList; aux != null; aux = aux.tail )
			aux.head.accept(this);
	}

	public void visit(MainClass node)
	{
		print("class ");
		node.className.accept(this);
		println("");
		println("{");
		beginNest();
		
		print( "public static void main(String[] " );
		node.mainArgName.accept(this);
		println( ")");
		println("{");
		beginNest();
		
		node.s.accept(this);
		
		endNest();
		println("}");
		endNest();
		println("}");
	}

	public void visit(ClassDeclSimple node)
	{
		print("class ");
		node.name.accept(this);
		println("");
		println( "{" );
		beginNest();
		
		for ( List<VarDecl> vars = node.varList; vars != null; vars = vars.tail )
			vars.head.accept(this);

		for ( List<MethodDecl> methods = node.methodList; methods != null; methods = methods.tail )
			methods.head.accept(this);
		
		endNest();
		println("}");
	}

	public void visit(ClassDeclExtends node)
	{
		print("class ");
		node.name.accept(this);
		print( " extends " );
		node.superClass.accept(this);
		println("");
		println( "{" );
		beginNest();
		
		for ( List<VarDecl> vars = node.varList; vars != null; vars = vars.tail )
		{
			vars.head.accept(this);
		}
		
		for ( List<MethodDecl> methods = node.methodList.tail; methods != null; methods = methods.tail )
			methods.head.accept(this);
		
		endNest();
		println("}");
	}

	public void visit(VarDecl node)
	{
		node.type.accept(this);
		print( " " );
		node.name.accept(this);
		println( ";" );
	}

	public void visit(MethodDecl node)
	{
		print("public ");
		node.returnType.accept(this);
		print( " " );
		node.name.accept(this);
		print( "(");
		
		for ( List<Formal> f = node.formals; f != null; f = f.tail )
		{
			f.head.accept(this);
			
			if ( f.tail != null )
				print(", ");
		}
		println(")");
		
		println("{");
		beginNest();
		
		for ( List<VarDecl> l = node.locals; l != null; l = l.tail )
			l.head.accept(this);
		
		for ( List<Statement> s = node.body; s != null; s = s.tail )
			s.head.accept(this);
		
		print( "return " );
		
		node.returnExp.accept(this);
		
		println("");
		
		endNest();
		println("}");
	}

	public void visit(Formal node)
	{
		node.type.accept(this);
		print( " " );
		node.name.accept(this);
	}

	public void visit(IntArrayType node)
	{
		print( "int[]" );
	}

	public void visit(BooleanType node)
	{
		print( "boolean" );
	}

	public void visit(IntegerType node)
	{
		print( "int" );
	}

	public void visit(IdentifierType node)
	{
		print( node.name );
	}

	public void visit(Block node)
	{
		println("{");
		beginNest();
		
		for ( List<Statement> aux = node.body; aux != null; aux = aux.tail )
			aux.head.accept(this);
		
		endNest();
		println("}");
	}

	public void visit(If node)
	{
		print( "if ( " );
		node.condition.accept(this);
		println( ")" );
		beginNest();
		node.thenClause.accept(this);
		endNest();
		if ( node.elseClause != null )
		{
			println( "else" );
			beginNest();
			node.elseClause.accept(this);
			endNest();
		}
	}

	public void visit(While node)
	{
		print( "while ( " );
		node.condition.accept(this);
		println( ")" );
		beginNest();
		node.body.accept(this);
		endNest();
	}

	public void visit(Print node)
	{
		print( "System.out.println( " );
		node.exp.accept(this);
		println( ");" );
	}

	public void visit(Assign node)
	{
		node.var.accept(this);
		print( " = " );
		node.exp.accept(this);
		println( ";" );
	}

	public void visit(ArrayAssign node)
	{
		node.var.accept(this);
		print( "[" );
		node.index.accept(this);
		print( "] = " );
		node.value.accept(this);
		println( ";" );
	}

	public void visit(And node)
	{
		node.lhs.accept(this);
		print( " && ");
		node.rhs.accept(this);
	}

	public void visit(LessThan node)
	{
		node.lhs.accept(this);
		print( " < ");
		node.rhs.accept(this);
	}

	public void visit(Equal node)
	{
		node.lhs.accept(this);
		print( " == ");
		node.rhs.accept(this);
	}

	public void visit(Plus node)
	{
		node.lhs.accept(this);
		print( " + ");
		node.rhs.accept(this);
	}

	public void visit(Minus node)
	{
		node.lhs.accept(this);
		print( " - ");
		node.rhs.accept(this);
	}

	public void visit(Times node)
	{
		node.lhs.accept(this);
		print( " * ");
		node.rhs.accept(this);
	}

	public void visit(ArrayLookup node)
	{
		node.array.accept(this);
		print( "[" );
		node.index.accept(this);
		print( "]" );
	}

	public void visit(ArrayLength node)
	{
		node.array.accept(this);
		print( ".length" );
	}

	public void visit(Call node)
	{
		node.object.accept(this);
		print(".");
		
		node.method.accept(this);
		
		print( "(" );
		
		for ( List<Exp> aux = node.actuals; aux != null; aux = aux.tail )
		{
			aux.head.accept(this);
			
			if ( aux.tail != null )
				print( ", " );
		}
		
		print( ")" );
	}

	public void visit(IntegerLiteral node)
	{
		print( "" + node.value );
	}

	public void visit(True node)
	{
		print( "true" );
	}

	public void visit(False node)
	{
		print( "false" );
	}

	public void visit(This node)
	{
		print("this");
	}

	public void visit(NewArray node)
	{
		print( "new int[" );
		node.size.accept(this);
		print( "]" );
	}

	public void visit(NewObject node)
	{
		print( "new " );
		node.className.accept(this);
		print( "()" );
	}

	public void visit(Not node)
	{
		print( "!" );
		node.exp.accept(this);
	}

	public void visit(IdentifierExp node)
	{
		node.name.accept(this);
	}

	public void visit(Identifier node)
	{
		print( node.s );
	}

}
