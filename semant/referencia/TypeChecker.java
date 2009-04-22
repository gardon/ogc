package semant;

import symbol.ClassInfo;
import symbol.Table;
import syntaxtree.Program;
import errors.ErrorEchoer;
import semant.FirstPassVisitor;
import visitor.*;

public class TypeChecker
{
    private TypeChecker()
    {
        super();
    }

    public static Env TypeCheck(ErrorEchoer err, Program p)
    {	    	
    	// Cria um ambiente que vai conter os erros
    	Env env = new Env(err);
    	
    	// Cria uma tabela que vai conter as classes do programa
    	Table<ClassInfo> table = new Table<ClassInfo>();
    	
    	// Primeira Passagem
    	Visitor v1 = new FirstPassVisitor(table, env);    	
    	p.accept(v1);
    	
    	// Segunda passagem
    	TypeVisitor v2 = new SecondPassVisitor(table, env);
    	p.accept(v2);
    	
    	return null;
    }
}
