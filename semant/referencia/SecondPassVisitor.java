package semant;

import syntaxtree.*;
import symbol.*;
import visitor.TypeVisitor;
import util.List;


public class SecondPassVisitor implements TypeVisitor {
    
    private static int DEBUG_TABS = 0;
    private static final boolean DEBUG = true;    // Essa variavel deve estar true apenas durante o desenvolvimento do compilador
    private Table<ClassInfo> table;
    private Env env;
    private ClassInfo currentClass;        // Variavel que guarda a classe que está sendo parseada
    private MethodInfo currentMethod;    // Variavel que guarda o método que está sendo parseado
    
    public SecondPassVisitor(Table<ClassInfo> table, Env env)
    {
        // Recebe a tabela de simbolos para ser preenchida
        this.table = table;
        this.env = env;
        this.currentClass = null;
        this.currentMethod = null;
        
        if (DEBUG)
        {
            System.out.println("Second Pass:");
        }
    }
    

    public Type visit(Program n) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Program...");
            DEBUG_TABS++;
        }
        
        // Parsear Mainclass
        n.mainClass.accept(this);
        
        // Parseando as classes deste programa
        List<syntaxtree.ClassDecl> classIter = n.classList;
        
        while (classIter != null)
        {                        
            // Visitando a classe
            classIter.head.accept(this);
            
            classIter = classIter.tail;
        }

        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Program");
        }
        return null;
    }

    public Type visit(MainClass n) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing MainClass(" + n.className.s + ")...");    
            DEBUG_TABS++;
        }
        
        // Parse statement
        n.s.accept(this);

        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing MainClass(" + n.className.s + ")");
        }        
        
        return null;
    }

    public Type visit(ClassDeclSimple n) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing ClassDeclSimple(" + n.name.s + ")...");
            DEBUG_TABS++;
        }
        
        this.currentClass = table.get(Symbol.symbol(n.name.s));
        
        // Parsear os métodos        
        List<syntaxtree.MethodDecl> methIter = n.methodList;
        
        while (methIter != null)
        {
            // Visitando o método
            methIter.head.accept(this);
            
            methIter = methIter.tail;
        }

        this.currentClass = null;
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing ClassDeclSimple(" + n.name.s + ")");
        }
        
        return null;
    }

    public Type visit(ClassDeclExtends n) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing ClassDeclExtends(" + n.name.s + ")...");
            DEBUG_TABS++;
        }
        
        this.currentClass = table.get(Symbol.symbol(n.name.s));
        
        // Parsear os métodos        
        List<syntaxtree.MethodDecl> methIter = n.methodList;
        
        while (methIter != null)
        {
            // Visitando o método
            methIter.head.accept(this);
            
            methIter = methIter.tail;
        }

        this.currentClass = null;
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing ClassDeclExtends(" + n.name.s + ")");
        }
        
        return null;
    }

    public Type visit(VarDecl n) {
        return null;
    }

    public Type visit(MethodDecl n) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing MethodDecl(" + n.name.s + ")...");
            DEBUG_TABS++;
        }
        
        this.currentMethod = this.currentClass.methods.get(Symbol.symbol(n.name.s));
        
        // Parseia cada statement
        List<Statement> bodyIter = n.body;
        
        while (bodyIter != null)
        {
            bodyIter.head.accept(this);
            
            bodyIter = bodyIter.tail;
        }
        
        // Parseia o valor de retorno
        Type tret = n.returnExp.accept(this);
        
        boolean isTheType = false;
        
        if (tret instanceof IdentifierType)
        {
            ClassInfo classoftype = this.table.get(Symbol.symbol(tret.toString()));
            
            if (classoftype != null)
            {
                if (classoftype.base != null)
                {
                    ClassInfo cparent = classoftype.base;
                    
                    while (cparent != null)
                    {
                        Type partype = new IdentifierType(n.line, n.row, cparent.name.toString());
                        
                        isTheType = partype.isComparable(n.returnType);
                        
                        if (isTheType)
                            break;
                        
                        cparent = cparent.base;
                    }
                }
                else
                {
                    isTheType = n.returnType.isComparable(tret);
                }
            }
            else
            {
                isTheType = n.returnType.isComparable(tret);
            }
        }
        else
        {
            isTheType = n.returnType.isComparable(tret);
        }
        
        if (isTheType == false)
        {
            this.env.err.Error(n, new Object[] {"A expressão de retorno não é do mesmo tipo do método",
                "Esperando um tipo " + n.returnType + ", encontrado o tipo " + tret + "."});
        
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Wrong return type!");
            }
        }
        
        
        
        this.currentMethod = null;

        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished MethodDecl MethodDecl(" + n.name.s + ")");
        }
        return null;
    }

    public Type visit(Formal n) {
        return null;
    }

    public Type visit(IntArrayType n) {
        return null;
    }

    public Type visit(BooleanType n) {
        return null;
    }

    public Type visit(IntegerType n) {
        return null;
    }

    public Type visit(IdentifierType n) {
        return null;
    }

    public Type visit(Block n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Block...");    
            DEBUG_TABS++;
        }        
        
        // Parseia cada statement
        List<Statement> bodyIter = n.body;
        
        while (bodyIter != null)
        {
            bodyIter.head.accept(this);
            
            bodyIter = bodyIter.tail;
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Block");
        }
        
        return null;
    }

    public Type visit(If n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing If...");    
            DEBUG_TABS++;
        }
        
        // Parsea a condição
        Type tcond = n.condition.accept(this);

        
        if (!(tcond instanceof BooleanType))
        {
            this.env.err.Error(n, new Object[] {"A condição do If não é booleana", 
                "O operador If necessita como condição uma expressão do tipo boolean",
                "Esperando um tipo boolean, encontrado o tipo " + tcond + "."});
        
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Wrong parameter for print!");
            }
        }
        
        // Parsea o then
        if (n.thenClause != null)
        {
            n.thenClause.accept(this);
        }
        
        // Parsea o else
        if (n.elseClause != null)
        {
            n.elseClause.accept(this);
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing If");
        }    
        
        return null;
    }

    public Type visit(While n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing While...");    
            DEBUG_TABS++;
        }
        
        // Parsea a condição
        Type tcond = n.condition.accept(this);
        
        if (!(tcond instanceof BooleanType))
        {
            this.env.err.Error(n, new Object[] {"A condição do While não é booleana", 
                "O operador While necessita como condição uma expressão do tipo boolean",
                "Esperando um tipo boolean, encontrado o tipo " + tcond + "."});
        
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Wrong parameter for print!");
            }
        }
        
        // Parsea o then
        if (n.body != null)
        {
            n.body.accept(this);
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing While");
        }    
        
        return null;
    }

    public Type visit(Print n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Print...");    
            DEBUG_TABS++;
        }
        
        // Parse Expression and check Type
        Type texp = n.exp.accept(this);
        
        if (!(texp instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"System.out.println necessita como parametro um inteiro", 
                "A chamada ao método System.out.println possui o parâmetro com tipo incoerente.",
                "Esperando um tipo int, encontrado o tipo " + texp + "."});
        
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Wrong parameter for print!");
            }
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Print");
        }    
        return null;
    }

    public Type visit(Assign n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Assign...");    
            DEBUG_TABS++;
        }
        
        // Verificar se a variavel existe
        Type tvar = n.var.accept(this);
        
        // verificar se o tipo da expressão equivale ao tipo
        Type texp = n.exp.accept(this);
        
        if ((tvar != null) && (tvar.isComparable(texp) == false))
        {
            this.env.err.Error(n, new Object[] {"Atribuição de um tipo a uma variável de outro tipo",
                "Esperando um tipo " + tvar + ", mas a expressão gera um tipo " + texp + "."});
        
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Wrong return type!");
            }
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Assign");
        }
        return null;
    }

    public Type visit(ArrayAssign n) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing ArrayAssign...");    
            DEBUG_TABS++;
        }
        
        // Verifica se o array é um array mesmo
        Type tarray = n.var.accept(this);
        
        if (!(tarray instanceof IntArrayType))
        {
            this.env.err.Error(n, new Object[] {"A expressão não é um array",
                    "O operador [] deve operar sobre um array",
                    "Esperando um tipo int[], encontrado o tipo " + tarray + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Not an array");
                }            
        }
        
        // Verifica se o índice é um inteiro
        Type tindex = n.index.accept(this);
        
        if (!(tindex instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"O indice não é um inteiro",
                    "O operador [] deve possuir um indice inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tindex + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Not an integer index");
                }            
        }
        
        // Verifica se o índice é um inteiro
        Type texp = n.value.accept(this);
        
        if (!(texp instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"O valor atribuido não é um inteiro",
                    "O array int[] deve possuir apenas inteiros",
                    "Esperando um tipo int, encontrado o tipo " + texp + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Not an integer asignment");
                }            
        }

        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing ArrayAssign");
        }    
        
        return null;
    }

    public Type visit(And n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing And...");    
            DEBUG_TABS++;
        }
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
        if (!(tipoesq instanceof BooleanType))
        {
            this.env.err.Error(n, new Object[] {"Operação && não possui o lado esquerdo uma expressão com boolean",
                    "Esperando um tipo boolean, encontrado o tipo " + tipoesq + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Left side not boolean");
                }
            
        }
        
        if (!(tipodir instanceof BooleanType))
        {
            this.env.err.Error(n, new Object[] {"Operação && não possui o lado direito uma expressão com boolean",
                    "Esperando um tipo boolean, encontrado o tipo " + tipodir + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Right side not boolean");
                }
            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing And");
        }    
        return new BooleanType(n.line, n.row);
    }

    public Type visit(LessThan n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing LessThan...");    
            DEBUG_TABS++;
        }
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
        if (!(tipoesq instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"Operação < não possui o lado esquerdo uma expressão com inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tipoesq + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Left side not int");
                }
            
        }
        
        if (!(tipodir instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"Operação < não possui o lado direito uma expressão com inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tipodir + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Right side not int");
                }
            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing LessThan");
        }    
        return new BooleanType(n.line, n.row);
    }

    public Type visit(Plus n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Plus...");    
            DEBUG_TABS++;
        }
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
        if (!(tipoesq instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"Operação + não possui o lado esquerdo uma expressão com inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tipoesq + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Left side not int");
                }
            
        }
        
        if (!(tipodir instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"Operação + não possui o lado direito uma expressão com inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tipodir + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Right side not int");
                }
            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Plus");
        }            
        
        return new IntegerType(n.line, n.row);
    }

    public Type visit(Minus n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Minus...");    
            DEBUG_TABS++;
        }
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
        if (!(tipoesq instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"Operação - não possui o lado esquerdo uma expressão com inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tipoesq + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Left side not int");
                }
            
        }
        
        if (!(tipodir instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"Operação - não possui o lado direito uma expressão com inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tipodir + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Right side not int");
                }
            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Minus");
        }    
        return new IntegerType(n.line, n.row);
    }

    public Type visit(Times n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Times...");    
            DEBUG_TABS++;
        }
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
        if (!(tipoesq instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"Operação * não possui o lado esquerdo uma expressão com inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tipoesq + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Left side not int");
                }
            
        }
        
        if (!(tipodir instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"Operação * não possui o lado direito uma expressão com inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tipodir + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Right side not int");
                }
            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Times");
        }    
        return new IntegerType(n.line, n.row);
    }

    public Type visit(ArrayLookup n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing ArrayLookup...");    
            DEBUG_TABS++;
        }
        
        // Verifica se o array é um array mesmo
        Type tarray = n.array.accept(this);
        
        if (!(tarray instanceof IntArrayType))
        {
            this.env.err.Error(n, new Object[] {"A expressão não é um array",
                    "O operador [] deve operar sobre um array",
                    "Esperando um tipo int[], encontrado o tipo " + tarray + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Not an array");
                }            
        }
        
        // Verifica se o índice é um inteiro
        Type tindex = n.index.accept(this);
        
        if (!(tindex instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"O indice não é um inteiro",
                    "O operador [] deve possuir um indice inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tindex + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Not an integer index");
                }            
        }
        

        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing ArrayLookup");
        }    
        
        return new IntegerType(n.line, n.row);
    }

    public Type visit(ArrayLength n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing ArrayLength...");    
            DEBUG_TABS++;
        }
        
        // Verifica se o array é um array mesmo
        Type tarray = n.array.accept(this);
        
        if (!(tarray instanceof IntArrayType))
        {
            this.env.err.Error(n, new Object[] {"A expressão não é um array",
                    "O operador [] deve operar sobre um array",
                    "Esperando um tipo int[], encontrado o tipo " + tarray + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Not an array");
                }            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing ArrayLength");
        }    
        
        return new IntegerType(n.line, n.row);
    }

    public Type visit(Call n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Call(" + n.method.s + ")...");    
            DEBUG_TABS++;
        }
        
        Type tiporetorno = null;
        
        // Parseia a parte do objeto, que vem antes do ponto
        Type tobj = n.object.accept(this);
        
        // Verifica se a parte do objeto retorna uma classe
        if (tobj instanceof IdentifierType)
        {
            ClassInfo cinfo = table.get(Symbol.symbol(((IdentifierType)tobj).name));
            
            if (cinfo != null)
            {
                // Verifica se a classe contem o método atual
                MethodInfo minfo = null;
                
                // Verifica se está numa classe pai (talvez isso seja desnecessário)
                // TODO: Verificar necessidade de iterar
                while (cinfo != null)
                {                    
                    minfo = cinfo.methods.get(Symbol.symbol(n.method.s));
                    if (minfo != null)
                        break;
                    
                    // Sobe pro pai
                    cinfo = cinfo.base;
                }
                
                if (minfo != null)
                {
                    // Verificar se os parâmetros coincidem, numero e tipo
                    List<VarInfo> formalsIter = minfo.formals;
                    List<Exp> actualsIter = n.actuals;
                    
                    while ((formalsIter != null) && (actualsIter  != null))
                    {
                        Type actualType = actualsIter.head.accept(this);
                        if (formalsIter.head.type.isComparable(actualType) == false)
                        {
                            // Parametro não bate o tipo
                            this.env.err.Error(n, new Object[] {"Tipo do parâmetro inválido.", 
                                    "A chamada ao método " + cinfo.name + "." + minfo.name + " possui o parâmetro " + formalsIter.head.name + " com tipo incoerente.",
                                    "Esperando um tipo " + formalsIter.head.type + ", encontrado o tipo " + actualType + "."});
                            
                            if (DEBUG){            
                                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                                System.out.println("ERROR! Wrong parameter!");
                            }
                        }
                        
                        formalsIter = formalsIter.tail;
                        actualsIter = actualsIter.tail;
                    }
                    
                    if (formalsIter != null)
                    {
                        // Faltou parâmetros
                        this.env.err.Error(n, new Object[] {"Parâmetros faltando.", "A chamada ao método " + cinfo.name + "." + minfo.name + " possui menos parâmetros que o necessário"});
                        
                        if (DEBUG){            
                            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                            System.out.println("ERROR! Missing parameters!");
                        }                        
                    }
                    
                    if (actualsIter != null)
                    {
                        // Sobrou parâmetros
                        this.env.err.Error(n, new Object[] {"Parâmetros sobrando.", "A chamada ao método " + cinfo.name + "." + minfo.name + " possui mais parâmetros que o necessário"});
                        
                        if (DEBUG){            
                            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                            System.out.println("ERROR! Exceding parameters!");
                        }                        
                    }
                    
                    tiporetorno = minfo.type;
                }
                else
                {
                    // Metodo não encontrado
                    this.env.err.Error(n, new Object[] {"Método não encontrado!", "A classe " + cinfo.name + " não possui o método " + n.method.s + "."});
                    
                    if (DEBUG){            
                        for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                        System.out.println("ERROR! Method not found!");
                    }
                }
                
            }
            else
            {
                // Classe não encontrada
                this.env.err.Error(n, new Object[] {"Classe não encontrada!", "A classe " + ((IdentifierType)tobj).name + " não existe."});
                
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Class not found!");
                }
            }
        }
        else
        {
            // Não é um objeto
            if (tobj != null)
                this.env.err.Error(n, new Object[] {"Esperado um objeto do tipo classe!", "Esperado um objeto do tipo classe, porém encontrado um objeto do tipo " + tobj.toString()});
            else
                this.env.err.Error(n, new Object[] {"Esperado um objeto do tipo classe!"});
            
            
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Not an object!");
            }
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Call(" + n.method.s + ")");
        }    
        return tiporetorno;
    }

    public Type visit(IntegerLiteral n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing IntegerLiteral(" + n.value + ")...");    
            DEBUG_TABS++;
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing IntegerLiteral(" + n.value + ")");
        }    
        return new IntegerType(n.line, n.row);
    }

    public Type visit(True n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing True...");    
            DEBUG_TABS++;
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing True");
        }    
        return new BooleanType(n.line, n.row);
    }

    public Type visit(False n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing False...");    
            DEBUG_TABS++;
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing False");
        }    
        return new BooleanType(n.line, n.row);
    }

    public Type visit(IdentifierExp n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing IdentifierExp(" + n.name.s + ")...");    
            DEBUG_TABS++;
        }
        
        Type tiporetorno = null;
        
        // Ordem da busca: Locais, parametro, classe, superclasse
        Symbol sym = Symbol.symbol(n.name.s);        
        
        VarInfo vinfo = this.currentMethod.localsTable.get(sym);
        if (vinfo != null)
        {
            tiporetorno = vinfo.type;
        }
        else
        {
            vinfo = this.currentMethod.formalsTable.get(sym);
            
            if (vinfo != null)
            {
                tiporetorno = vinfo.type;
            }
            else
            {
                ClassInfo cinfo = this.currentClass;

                // TODO: Verificar se é necessário iterar na classe base    
                while (cinfo != null)
                {
                    vinfo = cinfo.attributes.get(sym);
                    
                    if (vinfo != null)
                        break;
                    
                    cinfo = cinfo.base;
                }
                
                if (vinfo != null)
                {
                    tiporetorno = vinfo.type;
                }
                else
                {

                    this.env.err.Error(n, new Object[] {"Usando variável não declarada",
                            "Variavel não declarada " + sym + "."});
                    
                    if (DEBUG){            
                        for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                        System.out.println("ERROR! Var not declared");
                    }    
                }
            }
        }
        
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing IdentifierExp(" + n.name.s + ")");
        }    
        
        return tiporetorno;
    }

    public Type visit(This n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing This...");    
            DEBUG_TABS++;
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing This");
        }    
        
        if (this.currentClass != null)
        {
            return new IdentifierType(n.line, n.row, this.currentClass.name.toString());
        }
        else
        {
            this.env.err.Error(n, new Object[] {"Operação this não é valida em um método estático."});
            
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Static method");
            }
            return null;
        }
    }

    public Type visit(NewArray n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing NewArray...");    
            DEBUG_TABS++;
        }
        
        // Verifica se o tamanho é um inteiro
        Type tsize = n.size.accept(this);
        
        if (!(tsize instanceof IntegerType))
        {
            this.env.err.Error(n, new Object[] {"O tamanho do array não é um inteiro",
                    "O operador new int[] deve possuir um tamanho inteiro",
                    "Esperando um tipo int, encontrado o tipo " + tsize + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Not an integer size");
                }            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing NewArray");
        }    
        return new IntArrayType(n.line, n.row);
    }

    public Type visit(NewObject n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing NewObject(" + n.className.s + ")...");    
            DEBUG_TABS++;
        }
        
        Type tiporetorno = null;
        
        // Verifica se a classe desse objeto existe
        ClassInfo cinfo = this.table.get(Symbol.symbol(n.className.s));
        tiporetorno = new IdentifierType(n.line, n.row, n.className.s);
        
        if (cinfo == null)
        {
            this.env.err.Error(n, new Object[] {"Classe a ser instanciada não existe",
                    "A classe " + n.className.s + " não foi declarada."});
            
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Var not declared");
            }
            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing NewObject(" +  n.className.s + ")");
        }    
        return tiporetorno;
    }

    public Type visit(Not n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Not...");    
            DEBUG_TABS++;
        }
        
        Type texp = n.exp.accept(this);
        
        if (!(texp instanceof BooleanType))
        {
            this.env.err.Error(n, new Object[] {"Operação ! não possui uma expressão boleana",
                    "Esperando um tipo boolean, encontrado o tipo " + texp + "."});
            
                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! Expression not boolean");
                }
            
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Not");
        }    
        return new BooleanType(n.line, n.row);
    }

    public Type visit(Equal n) {
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Equal...");    
            DEBUG_TABS++;
        }
        
        // Verificar se a variavel existe
        Type tesq = n.lhs.accept(this);
        
        // verificar se o tipo da expressão equivale ao tipo
        Type tdir = n.rhs.accept(this);
        
        if ((tesq != null) && (tdir != null) && (tesq.isComparable(tdir) == false))
        {
            this.env.err.Error(n, new Object[] {"Operador == não é executado entre tipos iguais",
                "O lado esquerdo é do tipo " + tesq + " e o lado direito " + tdir + "."});
        
            if (DEBUG){            
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! Wrong types comparation!");
            }
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Equal");
        }
        
        return new BooleanType(n.line, n.row);
    }

    public Type visit(Identifier n) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Identifier(" + n.s + ")...");    
            DEBUG_TABS++;
        }
        
        Type tiporetorno = null;
        
        // Ordem da busca: Locais, parametro, classe, superclasse
        Symbol sym = Symbol.symbol(n.s);        
        
        VarInfo vinfo = this.currentMethod.localsTable.get(sym);
        if (vinfo != null)
        {
            tiporetorno = vinfo.type;
        }
        else
        {
            vinfo = this.currentMethod.formalsTable.get(sym);
            
            if (vinfo != null)
            {
                tiporetorno = vinfo.type;
            }
            else
            {
                ClassInfo cinfo = this.currentClass;
                
                // TODO: Verificar se é necessário iterar na classe base                
                while (cinfo != null)
                {
                    vinfo = cinfo.attributes.get(sym);
                    
                    if (vinfo != null)
                        break;
                    
                    cinfo = cinfo.base;
                }
                
                if (vinfo != null)
                {
                    tiporetorno = vinfo.type;
                }
                else
                {

                    this.env.err.Error(n, new Object[] {"Atribuição a variável não declarada",
                            "Variavel não declarada " + sym + "."});
                    
                    if (DEBUG){            
                        for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                        System.out.println("ERROR! Var not declared");
                    }    
                }
            }
        }
        
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Identifier(" + n.s + ")");
        }    
        
        return tiporetorno;
    }

}
