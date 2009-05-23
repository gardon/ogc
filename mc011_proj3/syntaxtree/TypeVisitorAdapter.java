package syntaxtree;

import visitor.TypeVisitor;

public class TypeVisitorAdapter implements TypeVisitor
{

	public TypeVisitorAdapter()
	{
		super();
	}

	public Type visit(Program n)
	{
		return null;
	}

	public Type visit(MainClass n)
	{
		return null;
	}

	public Type visit(ClassDeclSimple n)
	{
		return null;
	}

	public Type visit(ClassDeclExtends n)
	{
		return null;
	}

	public Type visit(VarDecl n)
	{
		return null;
	}

	public Type visit(MethodDecl n)
	{
		return null;
	}

	public Type visit(Formal n)
	{
		return null;
	}

	public Type visit(IntArrayType n)
	{
		return null;
	}

	public Type visit(BooleanType n)
	{
		return null;
	}

	public Type visit(IntegerType n)
	{
		return null;
	}

	public Type visit(IdentifierType n)
	{
		return null;
	}

	public Type visit(Block n)
	{
		return null;
	}

	public Type visit(If n)
	{
		return null;
	}

	public Type visit(While n)
	{
		return null;
	}

	public Type visit(Print n)
	{
		return null;
	}

	public Type visit(Assign n)
	{
		return null;
	}

	public Type visit(ArrayAssign n)
	{
		return null;
	}

	public Type visit(And n)
	{
		return null;
	}

	public Type visit(LessThan n)
	{
		return null;
	}

	public Type visit(Plus n)
	{
		return null;
	}

	public Type visit(Minus n)
	{
		return null;
	}

	public Type visit(Times n)
	{
		return null;
	}

	public Type visit(ArrayLookup n)
	{
		return null;
	}

	public Type visit(ArrayLength n)
	{
		return null;
	}

	public Type visit(Call n)
	{
		return null;
	}

	public Type visit(IntegerLiteral n)
	{
		return null;
	}

	public Type visit(True n)
	{
		return null;
	}

	public Type visit(False n)
	{
		return null;
	}

	public Type visit(IdentifierExp n)
	{
		return null;
	}

	public Type visit(This n)
	{
		return null;
	}

	public Type visit(NewArray n)
	{
		return null;
	}

	public Type visit(NewObject n)
	{
		return null;
	}

	public Type visit(Not n)
	{
		return null;
	}

	public Type visit(Equal n)
	{
		return null;
	}

	public Type visit(Identifier n)
	{
		return null;
	}
}
