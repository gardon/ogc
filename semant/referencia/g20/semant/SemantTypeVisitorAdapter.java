/*
 * MC011 - 1o sem / 2007 - Professor Sandro Rigo
 * Segunda entrega
 * SemantTypeVisitorAdapter.java
 *
 * Grupo 20 : Andreia Silva Donalisio    ra026898
 *            Julia Martinez Perdigueiro ra024158
 */

package semant;

import java.util.Collection;
import java.util.LinkedHashSet;

import errors.ErrorEchoer;
import symbol.ClassInfo;
import symbol.MethodInfo;
import symbol.Symbol;
import symbol.VarInfo;
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
import syntaxtree.Type;
import syntaxtree.VarDecl;
import syntaxtree.While;
import util.List;
import visitor.TypeVisitor;

/**
 * This class is responsible for type checking the program.
 * It reports all error messages to the ErrorEchoer.
 *
 * @author Andreia Silva Donalisio
 * @author Julia Martinez Perdigueiro
 *
 * @version 1.0
 */
public class SemantTypeVisitorAdapter implements TypeVisitor
{
	private ErrorEchoer err;
	private Env env;
	private ClassInfo classInfo = null;
	private MethodInfo methodInfo = null;

	public SemantTypeVisitorAdapter(ErrorEchoer err, Env env)
	{
		super();
		
		this.err = err;
		this.env = env;
	}

	public Type visit(Program n)
	{
		// flag para retornar null em qualquer caso de falha
		boolean errorFound = false;
		
		// pega a lista de classes para visitar
		List<ClassDecl> classes = n.classList;
		
		MainClass mainClass = n.mainClass;
		
		Type type = mainClass.accept(this);
		if ( type == null ) {
			errorFound = true;
		}
		
		while ( classes != null ) {
			// pega primeiro item da lista
			ClassDecl classDecl = classes.head;
			
			type = classDecl.accept(this);

			if ( type == null ) {
				errorFound = true;
			}
			
			// anda na lista
			classes = classes.tail;
		}
		
		if ( errorFound ) {
			type = null;
		} else {
			// instancia alguma coisa para nao retornar null
			type = new IntegerType(n.line, n.row);
		}
		
		return type;
	}

	public Type visit(MainClass n)
	{
		return n.s.accept(this);
	}

	public Type visit(ClassDeclSimple n)
	{
		// flag para retornar null em qualquer caso de falha
		boolean errorFound = false;
		
		// guarda referencia para a classe atual
		Symbol symbol = Symbol.symbol(n.name.toString());
		classInfo = env.classes.get(symbol);
		
		// valor de retorno
		Type type = null;
		
		if ( classInfo == null ) {
			err.Error(n, new Object[] {"Unidentified class " + n.name.toString() + "."});
			errorFound = true;
		} else {			
			// pega lista de metodos para visitar
			List<MethodDecl> methods = n.methodList;
			
			while ( methods != null ) {
				MethodDecl methodDecl = methods.head;
				
				type = methodDecl.accept(this);
				
				if ( type == null ) {
					errorFound = true;
				}
				
				// anda na lista de metodos
				methods = methods.tail;
			}
			
		}
		
		// limpa classInfo
		classInfo = null;
		
		if ( errorFound ) {
			type = null;
		} else {
			// instancia alguma coisa para nao retornar null
			type = new IntegerType(n.line, n.row);
		}
		
		return type;
	}

	public Type visit(ClassDeclExtends n)
	{
		// flag para retornar null em qualquer caso de falha
		boolean errorFound = false;
		
		Symbol symbol = Symbol.symbol(n.name.toString());
		classInfo = env.classes.get(symbol);
		
		// instancia alguma coisa em type soh para nao retornar null em caso de sucesso
		Type type = new IntegerType(n.line, n.row);
		
		// procura por ciclos na hierarquia colecionando as classes por onde passa
		// e testando se a proxima classe já foi passada (ciclo)
		ClassInfo auxInfo = classInfo.base;
		Collection<ClassInfo> passedClasses = new LinkedHashSet<ClassInfo>();
		passedClasses.add(classInfo);
		while ( auxInfo != null ) {
			if ( passedClasses.contains(auxInfo) ) {
				err.Error(n, new Object[] {"Cyclic inheritance detected on class " + symbol.toString() + " hierarchy."} );
				errorFound = true;
				
				// para de procurar ciclo
				break;
			}
			passedClasses.add(auxInfo);
			// pega proxima classe na hierarquia
			auxInfo = auxInfo.base;
		}
				
		// verifica redeclaracao de metodos
		List<MethodDecl> methods = n.methodList;
		while ( methods != null ) {
			MethodDecl method = methods.head;
			Symbol methodSymbol = Symbol.symbol(method.name.toString());
			
			// procura metodo na hierarquia
			auxInfo = classInfo.base;
			while ( auxInfo != null ) {
				if ( auxInfo.methods.containsKey(methodSymbol) ) {
					// achou metodo na hierarquia (para de procurar)
					break;
				}
				
				// anda na hierarquia
				auxInfo = auxInfo.base;
			}
			
			// se auxInfo != null nesse ponto, achou o metodo na
			// hierarquia (verificar redeclaracao nesse caso)
			if ( auxInfo != null ) {
				MethodInfo superMethod = auxInfo.methods.get(methodSymbol);
				
				// verifica tipo de retorno
				if ( ! superMethod.type.getClass().equals(method.returnType.getClass()) ) {
					err.Error(n, new Object[] {"Incompatible return type for inherited method."} );
					errorFound = true;
				}
				
				// verifica parametros
				List<Formal> args = method.formals;
				List<VarInfo> superArgs = superMethod.formals;
				boolean overload = false;
				while ( args != null ) {
					// sobrou parametros na redeclaracao (overload)
					if ( superArgs == null ) {
						overload = true;
						break;
					}
					
					Formal arg = args.head;
										
					// tipos diferentes na redeclaracao (overload)
					if ( ! superArgs.head.name.toString().equals(arg.name.toString()) ) {
						overload = true;
						break;
					}
					
					// anda nas listas
					args = args.tail;
					superArgs = superArgs.tail;
				}
				
				// faltou parametros na redeclaracao (overload)
				if ( (args == null) && (superArgs != null) ) {
					overload = true;
				}
				
				if ( overload ) {
					err.Error(n, new Object[] {"Method overload is not allowed."} );
					errorFound = true;
				}
			}
			
			// anda na lista
			methods = methods.tail;
		}
		
		
		// visita todos os metodos da classe
		methods = n.methodList;
		while ( methods != null ) {
			MethodDecl method = methods.head;
			
			type = method.accept(this);
			
			if ( type == null ) {
				errorFound = true;
			}
			
			// anda na lista
			methods = methods.tail;
		}
			
		// limpa classInfo
		classInfo = null;
		
		if ( errorFound ) {
			type = null;
		} else {
			// instancia alguma coisa para nao retornar null
			type = new IntegerType(n.line, n.row);
		}
				
		return type;
	}

	public Type visit(VarDecl n)
	{
		
		return null;
	}

	public Type visit(MethodDecl n)
	{
		// flag para retornar null em qualquer caso de falha
		boolean errorFound = false;
		
		// guarda o metodo atual
		Symbol symbol = Symbol.symbol(n.name.toString());
		methodInfo = classInfo.methods.get(symbol);
		
		// valor de retorno
		Type t = null;
		
		if ( methodInfo == null ) {
			err.Error(n, new Object[] {"Undefined method " + n.name.toString() + "."});
			errorFound = true;
		} else {			
			List<Statement> stms = n.body;
			
			while ( stms != null ) {
				Statement stm = stms.head;
				
				t = stm.accept(this);
				
				if ( t == null ) {
					errorFound = true;
				}
				
				// anda na lista de statements
				stms = stms.tail;
			}
		}
		
		// verifica o valor do return
		t = n.returnExp.accept(this);
		if ( (t != null) && ! (t.toString().equals(n.returnType.toString()) ) ) {
			// pode ser a super classe (procura)
			ClassInfo auxInfo = classInfo.base;
			boolean isOk = false;
			while ( auxInfo != null ) {
				if ( auxInfo.name.toString().equals(n.returnType.toString()) ) {
					isOk = true;
					break;
				}
				
				// anda na hierarquia
				auxInfo = auxInfo.base;
			}
			
			if ( ! isOk ) {
				err.Error(n, new Object[] {"Incompatible return type for method " + n.name.toString() + "."});
				errorFound = true;
			}
		}
		
		// limpa methodInfo
		methodInfo = null;
		
		if ( errorFound ) {
			t = null;
		} else {
			// instancia alguma coisa para nao retornar null
			t = new IntegerType(n.line, n.row);
		}
		
		return t;
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
		List<Statement> stms = n.body;
		boolean nullFound = false;
		
		while ( stms != null ) {
			Statement stm = stms.head;
			
			Type type = stm.accept(this);
			
			if ( type == null ) {
				nullFound = true;
			}
			
			// anda nas stms
			stms = stms.tail;
		}
		
		// valor para retorno (null significa erro)
		Type type = ( nullFound ? null : new IntegerType(n.line, n.row));
		
		return type;
	}

	public Type visit(If n)
	{
		// visita a condicao do if
		Type type = n.condition.accept(this);
	
		// testa condicao do if
		if ( type == null ) {
			err.Error(n, new Object[] {"Missing if clause condition."});
		}
		if (  ! ( type instanceof BooleanType ) ) {
			err.Error(n, new Object[] {"Condition of if clause must be of boolean type."});
		}
		
		if ( (n.thenClause.accept(this)) == null ) {
			err.Error(n, new Object[] {"Missing then clause."});
			type = null;
		}
		
		if ( ( n.elseClause != null ) && ( (n.elseClause.accept(this)) == null ) ) {
			err.Error(n, new Object[] {"Error on else clause."});
			type = null;
		}
		
		return type;
	}

	public Type visit(While n)
	{
		Type type = n.condition.accept(this);
		if ( (type == null) || ! ( type instanceof BooleanType ) ) {
			err.Error(n, new Object[] {"Condition of while clause must be of boolean type."});
			type = null;
		}
		
		if ( ( n.body.accept(this) ) == null ) {
			type = null;
		}
		
		return type;
	}

	public Type visit(Print n)
	{
		Type type = n.exp.accept(this);
		if ( (type == null) || ! ( type instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Expression of print must be of integer type."});
			type = null;
		}
		
		return type;
	}

	public Type visit(Assign n)
	{
		Type varType = n.var.accept(this);
		Type expType = n.exp.accept(this);
		
		if ( (varType != null) && (expType != null) ) {
			if ( ! varType.getClass().equals(expType.getClass()) ) {
				err.Error(n, new Object[] {"Incompatible types in assignment."});
				varType = null;
			}
		} else {
			varType = null;
		}
		
		return varType;
	}

	public Type visit(ArrayAssign n)
	{
		Type type = n.index.accept(this);
		if ( ( type == null ) || ! ( type instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Array index must be of type integer."});
			type = null;
		}
		
		Type varType = n.var.accept(this);
		Type expType = n.value.accept(this);
		
		if ( (varType != null) && (expType != null) ) { 
			if ( ! varType.getClass().equals(expType.getClass()) ) {
				err.Error(n, new Object[] {"Incompatible types in assignment."});
				type = null;
			} else {
				type = new IntegerType(n.line, n.row);
			}
		}
		else
		{
			type = null;
		}
		
		return type;
	}

	public Type visit(And n)
	{
		Type type = n.lhs.accept(this);
		if ( (type == null) || ! ( type instanceof BooleanType ) ) {
			err.Error(n, new Object[] {"Left side of and operator must be of type boolean."});
		}
		
		type = n.rhs.accept(this);
		if ( (type == null) || ! ( type instanceof BooleanType ) ) {
			err.Error(n, new Object[] {"Right side of and operator must be of type boolean."});
		}
		
		return new BooleanType(n.line, n.row);
	}

	public Type visit(LessThan n)
	{
		Type type = n.lhs.accept(this);
		if ( (type == null) || ! ( type  instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Left side of less than operator must be of type integer."});
		}
		
		type = n.rhs.accept(this);
		if ( (type == null) || ! ( type instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Right side of less than operator must be of type integer."});
		}
		
		return new BooleanType(n.line, n.row);
	}

	public Type visit(Plus n)
	{
		Type type = n.lhs.accept(this);
		if ( (type == null) || ! ( type  instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Left side of plus operator must be of type integer."});
		}
		
		type = n.rhs.accept(this);
		if ( (type == null) || ! ( type instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Right side of plus operator must be of type integer."});
		}
		
		return new IntegerType(n.line, n.row);
	}

	public Type visit(Minus n)
	{
		Type type = n.lhs.accept(this);
		if ( (type == null) || ! ( type  instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Left side of minus operator must be of type integer."});
		}
		
		type = n.rhs.accept(this);
		if ( (type == null) || ! ( type instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Right side of minus operator must be of type integer."});
		}
		
		return new IntegerType(n.line, n.row);
	}

	public Type visit(Times n)
	{
		Type type = n.lhs.accept(this);
		if ( (type == null) || ! ( type  instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Left side of times operator must be of type integer."});
		}
		
		type = n.rhs.accept(this);
		if ( (type == null) || ! ( type instanceof IntegerType ) ) {
			err.Error(n, new Object[] {"Right side of times operator must be of type integer."});
		}
		
		return new IntegerType(n.line, n.row);
	}

	public Type visit(ArrayLookup n)
	{
		Type type = n.index.accept(this);
		if ( (type == null) || ! ( type instanceof IntegerType )) {
			err.Error(n, new Object[] {"Array index must be of type integer."});
		}
		
		type = n.array.accept(this);
		if ( (type == null) || ! ( type instanceof IntArrayType ) ) {
			err.Error(n, new Object[] {"Array must be of integers."});
			type = null;
		} else {
			type = new IntegerType(n.line, n.row);
		}
		
		return type;
	}

	public Type visit(ArrayLength n)
	{
		// Verificar se o lenght vai ser calculado para um vetor de inteiros
		Type type = n.array.accept(this);
		if ( (type == null) || ! (type instanceof IntArrayType) ) {
			err.Error(n, new Object[] {"Attribute lenght access on non integer array type."});
			type = null;
		} else {
			type = new IntegerType(n.line, n.row);
		}
				
		return type;
	}

	public Type visit(Call n)
	{
		Type type = n.object.accept(this);
		
		if ( type != null ) {
			// procura declaracao da classe no env
			
			Symbol symbol = Symbol.symbol(type.toString());
			ClassInfo classInfo = env.classes.get(symbol);
			
			if ( classInfo == null ) {
				type = null;
			} else {
				// procura declaracao do metodo na classe e na hierarquia
				while ( classInfo != null ) {
					type = acceptMethodCalled(n, classInfo);
				
					if ( type == null ) {
						// ainda nao achou (continua procurando na hierarquia)
						classInfo = classInfo.base;
					} else {
						break;
					}
				}
				
				if ( classInfo == null ) {
					err.Error(n, new Object[] {"Invalid method invocation."});
					type = null;
				}
				
			}
		}
		
		return type;
	}
	
	private Type acceptMethodCalled(Call n, ClassInfo classInfo) {
		Collection<Symbol> methodSymbols = classInfo.methods.keySet();
		Type type = null;
		
		for ( Symbol methodSymbol : methodSymbols ) {
			MethodInfo method = classInfo.methods.get(methodSymbol);
			
			if ( Symbol.symbol(n.method.s).equals(method.name) ) {
				// achou o metodo (verificar argumentos)
				List<VarInfo> methodArgs = method.formals;
				List<Exp> passedArgs = n.actuals;
				type = method.type;
				
				while ( passedArgs != null ) {
					if ( methodArgs == null ) {
						err.Error(n, new Object[] {"Too many arguments for method " + method.name.toString() + "."} );
						break;
					}
					
					Exp arg = passedArgs.head;
					Type argType = arg.accept(this);
					
					if ( ! methodArgs.head.type.getClass().equals(argType.getClass()) ) {
						err.Error(n, new Object[] {"Wrong argument type " + argType.toString() +
								" (expecting type " + methodArgs.head.type.toString() + ")."});
						break;
					}
					
					// anda nas listas
					passedArgs = passedArgs.tail;
					methodArgs = methodArgs.tail;
				}
				
				if ( (passedArgs == null) && (methodArgs != null) ) {
					err.Error(n, new Object[] {"Too few arguments for method " + method.name.toString() + "."} );
				}
				
				break;
			}
			
		}
		
		return type;
	}

	public Type visit(IntegerLiteral n)
	{
		return new IntegerType(n.line, n.row);
	}

	public Type visit(True n)
	{
		return new BooleanType(n.line, n.row);
	}

	public Type visit(False n)
	{
		return new BooleanType(n.line, n.row);
	}

	public Type visit(IdentifierExp n)
	{
		Symbol symbol = Symbol.symbol(n.name.toString());
		Type type = null;
		
		// tenta procurar nas variaveis locais e argumentos do metodo
		if ( methodInfo != null ) {
			// procura nos argumentos do metodo
			List<VarInfo> args = methodInfo.formals;
			
			while ( args != null ) {
				VarInfo arg = args.head;
				if ( arg.name.equals(symbol) ) {
					type = arg.type;
					break;
				}
				
				// anda na lista
				args = args.tail;
			}
			
			if ( type == null ) {
				// ainda nao achou, entao procura nas variaveis locais do metodo
				List<VarInfo> localVars = methodInfo.locals;
				
				while ( localVars != null ) {
					VarInfo localVar = localVars.head;
					if ( localVar.name.equals(symbol) ) {
						type = localVar.type;
						break;
					}
					
					// anda na lista
					localVars = localVars.tail;
				}
			}
			
		} 
		
		if ( type == null ){
			// ainda nao encontrou (tenta procurar nos atributos da
			// classe - na hierarquia inclusive)
			ClassInfo info = classInfo;
			while ( info != null ) {
				Collection<Symbol> attSymbols = info.attributes.keySet();
				
				for ( Symbol attSymbol : attSymbols ) {
					if ( attSymbol.equals(symbol) ) {
						type = info.attributes.get(symbol).type;
						break;
					}
				}
				
				if ( type == null ) {
					// anda na hierarquia
					info = info.base;
				} else {
					break;
				}
			}
		}
		
		if ( type == null ) {
			err.Error(n, new Object[] {"Identifier " + n.name.s + " undeclared."} );
		}
		
		return type;
	}

	public Type visit(This n)
	{
		Type type = null;
		if ( classInfo != null ) {
			type = new IdentifierType(n.line, n.row, classInfo.name.toString());
		} else {
			err.Error(n, new Object[] {"Modifier \'this\' used in static context."} );
		}
		
		return type;
	}

	public Type visit(NewArray n)
	{
		Type type = n.size.accept(this);
		
		if ( (type == null) || ! (type instanceof IntegerType) ) {
			err.Error(n, new Object[] {"Array size must be of type integer."});
			type = null;
		} else {
			type = new IntArrayType(n.line, n.row);
		}
		
		
		return type;
	}

	public Type visit(NewObject n)
	{
		Symbol symbol = Symbol.symbol(n.className.s);
		
		ClassInfo classInfo = env.classes.get(symbol);
		Type type = null;
		
		if ( classInfo != null ) {
			type = new IdentifierType(n.line, n.row, n.className.toString());
		} else {
			err.Error(n, new Object[] {"Unidentified class identifier " + n.className + "."});
		}
		
		return type;
	}

	public Type visit(Not n)
	{
		Type type = n.exp.accept(this);
		if ( (type == null) || ! ( type instanceof BooleanType ) ) {
			err.Error(n, new Object[] {"Expression of not operator must be boolean."});
		}
		
		return new BooleanType(n.line, n.row);
	}

	public Type visit(Equal n)
	{
		return null;
	}

	public Type visit(Identifier n)
	{		
		Symbol symbol = Symbol.symbol(n.s);
		
		// valor para retorno
		Type type = null;
		
		// procura no metodo corrente
		if ( methodInfo != null ) {
			
			// procura nos argumentos do metodo
			List<VarInfo> args = methodInfo.formals;
			while ( args != null ) {
				VarInfo arg = args.head;
				
				if ( arg.name.equals(symbol) ) {
					type = arg.type;
					break; // para de procurar (jah achou)
				}
				
				// anda na lista de argumentos
				args = args.tail;
			}
			
			// se ainda nao achou...
			if ( type == null ) {
				// procura nas variaveis locais
				List<VarInfo> localVars = methodInfo.locals;
				while ( localVars != null ) {
					VarInfo localVar = localVars.head;
					
					if ( localVar.name.equals(symbol) ) {
						type = localVar.type;
						break;
					}
					
					// anda na lista de variaveis locais
					localVars = localVars.tail;
				}
			}
		} 
		
		// se ainda nao achou...
		if ( type == null ) {
			// procura nos atributos da classe - na hierarquia inclusive
			ClassInfo info = classInfo;
			while ( info != null ) {
				Collection<Symbol> attSymbols = info.attributes.keySet();
				
				for ( Symbol attSymbol : attSymbols ) {
					if ( attSymbol.equals(symbol) ) {
						type = info.attributes.get(symbol).type;
						break;
					}
				}
				
				if ( type == null ) {
					// anda na hierarquia
					info = info.base;
				} else {
					break;
				}
			}
		}
		
		return type;
	}
}
