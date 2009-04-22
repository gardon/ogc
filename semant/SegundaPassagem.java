package semant;

import syntaxtree.*;
import symbol.*;
import visitor.TypeVisitor;
import util.List;


public class SegundaPassagem implements TypeVisitor {
    
    private Table<ClassInfo> tabsimb;
    private Env env;
    private ClassInfo classeAtual;        // Variavel que guarda a classe que está sendo parseada
    private MethodInfo metodoAtual;    // Variavel que guarda o método que está sendo parseado
    
    public SegundaPassagem(Table<ClassInfo> tabsimb, Env env){
        // Recebe a tabela de simbolos para ser preenchida
        this.tabsimb = tabsimb;
        this.env = env;
        this.classeAtual = null;
        this.metodoAtual = null;
    } 

    public Type visit(Program n) {
        
        // Parsear Mainclass
        n.mainClass.accept(this);
        
        // Parseando as classes deste programa
        List<syntaxtree.ClassDecl> IndexClasse = n.classList;
        
        for (;IndexClasse != null;IndexClasse = IndexClasse.tail) IndexClasse.head.accept(this);
       
	return null; 
    }

    public Type visit(MainClass n) {

        // Parse statement
        n.s.accept(this);

        return null;
    }

    public Type visit(ClassDeclSimple n) {
        
        this.classeAtual = tabsimb.get(Symbol.symbol(n.name.s));
        
        // Parsear os métodos        
        List<syntaxtree.MethodDecl> IndexMetodo = n.methodList;
        
        for (;IndexMetodo != null;IndexMetodo = IndexMetodo.tail) IndexMetodo.head.accept(this);

        this.classeAtual = null;
        
        return null;
    }

    public Type visit(ClassDeclExtends n) {
        
        this.classeAtual = tabsimb.get(Symbol.symbol(n.name.s));
        
        // Parsear os métodos        
        List<syntaxtree.MethodDecl> IndexMetodo = n.methodList;
        
        for (;IndexMetodo != null;IndexMetodo = IndexMetodo.tail) IndexMetodo.head.accept(this);

        this.classeAtual = null;
        
        return null;
    }

    public Type visit(VarDecl n) {
        return null;
    }

    public Type visit(MethodDecl n) {
        
        this.metodoAtual = this.classeAtual.methods.get(Symbol.symbol(n.name.s));
        
        // Parseia cada statement
        List<Statement> bodyIter = n.body;
        
        for (;bodyIter != null;bodyIter = bodyIter.tail) bodyIter.head.accept(this);
        
        // Parseia o valor de retorno
        Type tret = n.returnExp.accept(this);
        
        boolean isTheType = false;
        
        if (tret instanceof IdentifierType){
            ClassInfo classoftype = this.tabsimb.get(Symbol.symbol(tret.toString()));
            
            if (classoftype != null){
                if (classoftype.base != null){
                    ClassInfo classeMae = classoftype.base;
                    
                    for (;classeMae != null;classeMae = classeMae.base){
                        Type partype = new IdentifierType(n.line, n.row, classeMae.name.toString());
                        
                        isTheType = partype.isComparable(n.returnType);
                        
                        if (isTheType)
                            break;
                    }
                }
                else{
                    isTheType = n.returnType.isComparable(tret);
                }
            }
            else{
                isTheType = n.returnType.isComparable(tret);
            }
        }
        else{
            isTheType = n.returnType.isComparable(tret);
        }
       
	//Valor de retorno inconsistente
        if (isTheType == false){
            this.env.err.Error(n, new Object[] {"Inconsistência do valor retornado pelo método",
                "Declarado o tipo " + n.returnType + ", encontrado o tipo " + tret + "."});
	} 
        
        
        this.metodoAtual = null;

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
        // Parseia cada statement
        List<Statement> bodyIter = n.body;
        
        for (;bodyIter != null;bodyIter = bodyIter.tail) bodyIter.head.accept(this);
        
        return null;
    }

    public Type visit(If n) {
        // Parsea a condição
        Type tcond = n.condition.accept(this);

        
        if (!(tcond instanceof BooleanType)){
            this.env.err.Error(n, new Object[] {"Condição de IF não é uma expressão booleana!", 
                "IF requer uma expressão/condição booleana",
                "Tipo boolean esperado, encontrado tipo " + tcond + "."});
        
            
        }
        
        // Parsea o then
        if (n.thenClause != null){
            n.thenClause.accept(this);
        }
        
        // Parsea o else
        if (n.elseClause != null){
            n.elseClause.accept(this);
        }
        
        return null;
    }

    public Type visit(While n) {
        // Parsea a condição
        Type tcond = n.condition.accept(this);
        
        if (!(tcond instanceof BooleanType)){
            this.env.err.Error(n, new Object[] {"Condição de WHILE não é uma expressão booleana!", 
                "WHILE requer uma expressão/condição booleana",
                "Tipo boolean esperado, encontrado tipo " + tcond + "."});
        
        }
        
        // Parsea o then
        if (n.body != null){
            n.body.accept(this);
        }
        
        return null;
    }

    public Type visit(Print n) {
        // Parse Expression and check Type
        Type texp = n.exp.accept(this);
        
        if (!(texp instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Parâmetro incoerente em System.out.println!", 
                "Método System.out.println requer parâmetro do tipo inteiro.",
                "Tipo int esperado, encontrado tipo " + texp + "."});
        
        }
        
        return null;
    }

    public Type visit(Assign n) {
        // Verificar se a variavel existe
        Type tvar = n.var.accept(this);
        
        // verificar se o tipo da expressão equivale ao tipo
        Type texp = n.exp.accept(this);
        
        if ((tvar != null) && (tvar.isComparable(texp) == false)){
            this.env.err.Error(n, new Object[] {"Inconsistência de tipos na atribuição!",
                "Tipo " + tvar + " esperado, mas a atribuição é do tipo " + texp + "."});
        
        }
        
        
        return null;
    }

    public Type visit(ArrayAssign n) {
        
        // Verifica se o array é um array mesmo
        Type tarray = n.var.accept(this);
        
        if (!(tarray instanceof IntArrayType)){
            this.env.err.Error(n, new Object[] {"A expressão não é do tipo 'array'",
                    "O operador [] deve ser usado apenas em variáveis do tipo array",
                    "Tipo int[] esperado, encontrado tipo " + tarray + "."});
        }
        
        // Verifica se o índice é um inteiro
        Type tindex = n.index.accept(this);
        
        if (!(tindex instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Índice não é do tipo 'int'",
                    "O índice do operador [] deve ser um inteiro",
                    "Tipo int esperado, encontrado tipo " + tindex + "."});
            
                            
        }
        
        // Verifica se o índice é um inteiro
        Type texp = n.value.accept(this);
        
        if (!(texp instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Valor atribuido não é do tipo 'int'",
                    "O array int[] deve possuir apenas valores inteiros",
                    "Tipo int esperado, encontrado tipo " + texp + "."});
            
                            
        }

        
            
        
        return null;
    }

    public Type visit(And n) {
        
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
        if (!(tipoesq instanceof BooleanType)){
            this.env.err.Error(n, new Object[] {"Lado esquerdo do operador '&&' não é expressão booleana",
                    "Tipo boolean esperado, encontrado tipo " + tipoesq + "."});
            
                
            
        }
        
        if (!(tipodir instanceof BooleanType)){
            this.env.err.Error(n, new Object[] {"Lado direito do operador '&&' não é expressão booleana",
                    "Tipo boolean esperado, encontrado tipo " + tipodir + "."});
            
                
            
        }
        
            
        return new BooleanType(n.line, n.row);
    }

    public Type visit(LessThan n) {
        
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
        if (!(tipoesq instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Lado esquerdo do operador '<' não é uma expressão do tipo 'int'",
                    "Tipo 'int' esperado, encontrado tipo " + tipoesq + "."});
            
                
            
        }
        
        if (!(tipodir instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Lado direito do operador '<' não é uma expressão do tipo 'int'",
                    "Tipo 'int' esperado, encontrado tipo " + tipodir + "."});
            
                
            
        }
        
            
        return new BooleanType(n.line, n.row);
    }

    public Type visit(Plus n) {
        
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
        if (!(tipoesq instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Lado direito do operador '+' não é uma expressão do tipo 'int'",
                    "Tipo 'int' esperado, encontrado tipo " + tipoesq + "."});
            
                
            
        }
        
        if (!(tipodir instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Lado direito do operador '+' não é uma expressão do tipo 'int'",
                    "Tipo 'int' esperado, encontrado tipo " + tipodir + "."});
            
                
            
        }
        
                    
        
        return new IntegerType(n.line, n.row);
    }

    public Type visit(Minus n) {
        
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
        
	if (!(tipoesq instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Lado direito do operador '-' não é uma expressão do tipo 'int'",
                    "Tipo 'int' esperado, encontrado tipo " + tipoesq + "."});
            
        }
        
        if (!(tipodir instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Lado direito do operador '-' não é uma expressão do tipo 'int'",
                    "Tipo 'int' esperado, encontrado tipo " + tipodir + "."});
                
            
        }
        
            
        return new IntegerType(n.line, n.row);
    }

    public Type visit(Times n) {
        
        
        Type tipoesq = n.lhs.accept(this);
        Type tipodir = n.rhs.accept(this);
         
	if (!(tipoesq instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Lado direito do operador '*' não é uma expressão do tipo 'int'",
                    "Tipo 'int' esperado, encontrado tipo " + tipoesq + "."});
            
        }
        
        if (!(tipodir instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Lado direito do operador '*' não é uma expressão do tipo 'int'",
                    "Tipo 'int' esperado, encontrado tipo " + tipodir + "."});
                
            
        }
        
            
        return new IntegerType(n.line, n.row);
    }

    public Type visit(ArrayLookup n) {
        
        
        // Verifica se o array é um array mesmo
        Type tarray = n.array.accept(this);
         
        if (!(tarray instanceof IntArrayType)){
            this.env.err.Error(n, new Object[] {"A expressão não é do tipo 'array'",
                    "O operador [] deve ser usado apenas em variáveis do tipo array",
                    "Tipo int[] esperado, encontrado tipo " + tarray + "."});
        }
        
        // Verifica se o índice é um inteiro
        Type tindex = n.index.accept(this);
        
        if (!(tindex instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"Índice não é do tipo 'int'",
                    "O índice do operador [] deve ser um inteiro",
                    "Tipo int esperado, encontrado tipo " + tindex + "."});
            
                            
        }
            
        
        return new IntegerType(n.line, n.row);
    }

    public Type visit(ArrayLength n) {
        
        
        // Verifica se o array é um array mesmo
        Type tarray = n.array.accept(this);
        
        if (!(tarray instanceof IntArrayType)){
            this.env.err.Error(n, new Object[] {"A expressão não é do tipo 'array'",
                    "O operador [] deve ser usado apenas em variáveis do tipo array",
                    "Tipo int[] esperado, encontrado tipo " + tarray + "."});
                            
        }
        
            
        
        return new IntegerType(n.line, n.row);
    }

    public Type visit(Call n) {
        
        
        Type tiporetorno = null;
        
        // Parseia a parte do objeto, que vem antes do ponto
        Type tobj = n.object.accept(this);
        
        // Verifica se a parte do objeto retorna uma classe
        if (tobj instanceof IdentifierType){
            ClassInfo InfoClasse = tabsimb.get(Symbol.symbol(((IdentifierType)tobj).name));
            
            if (InfoClasse != null){
                // Verifica se a classe contem o método atual
                MethodInfo InfoMetodo = null;
                
                // Verifica se está numa classe pai (talvez isso seja desnecessário)
                // TODO: Verificar necessidade de iterar
                for (;InfoClasse != null;InfoClasse = InfoClasse.base){                    
                    InfoMetodo = InfoClasse.methods.get(Symbol.symbol(n.method.s));
                    if (InfoMetodo != null)
                        break;
                }
                
                if (InfoMetodo != null){
                    // Verificar se os parâmetros coincidem, numero e tipo
                    List<VarInfo> IndexFormals = InfoMetodo.formals;
                    List<Exp> actualsIter = n.actuals;
                    
                    while ((IndexFormals != null) && (actualsIter  != null)){
                        Type actualType = actualsIter.head.accept(this);
                        if (IndexFormals.head.type.isComparable(actualType) == false){
                            // Parametro não bate o tipo
                            this.env.err.Error(n, new Object[] {"Tipo do parâmetro inválido.", 
                                    "Tipo do parâmetro " + IndexFormals.head.name + " inválido para método " +
				    InfoClasse.name + "." + InfoMetodo.name + ".",
				    "Tipo " + IndexFormals.head.type + " esperado, encontrado tipo " + actualType + "."});
                            
                        }
                        
                        IndexFormals = IndexFormals.tail;
                        actualsIter = actualsIter.tail;
                    }
                    
                    if (IndexFormals != null){
                        // Faltou parâmetros
                        this.env.err.Error(n, new Object[] {"Faltam parâmetros", 
			    "Parâmetros insuficientes para chamada ao método " + InfoClasse.name + "." + InfoMetodo.name + "."});
                        
                                                
                    }
                    
                    if (actualsIter != null){
                        // Sobrou parâmetros
                        this.env.err.Error(n, new Object[] {"Excesso de parâmetros.", 
			    "Parâmetros excedentes para chamada ao método " + InfoClasse.name + "." + InfoMetodo.name + "."});
                        
                                                
                    }
                    
                    tiporetorno = InfoMetodo.type;
                }
                else{
                    // Metodo não encontrado
                    this.env.err.Error(n, new Object[] {"Método não encontrado!", "Método " + n.method.s + 
			" não encontrado na classe " + InfoClasse.name + "."});
                    
                    
                }
                
            }
            else{
                // Classe não encontrada
                this.env.err.Error(n, new Object[] {"Classe inexistente!", "Não foi possível encontrar a classe " 
		    + ((IdentifierType)tobj).name + "."});
                
                
            }
        }
        else{
            // Não é um objeto
            if (tobj != null)
                this.env.err.Error(n, new Object[] {"Não é objeto do tipo classe!", 
		    "Objeto do tipo classe esperado, encontrado objeto do tipo " + tobj.toString()});
            else
                this.env.err.Error(n, new Object[] {"Não é objeto do tipo classe!"});
            
        }
        
            
        return tiporetorno;
    }

    public Type visit(IntegerLiteral n) {
        
        
            
        return new IntegerType(n.line, n.row);
    }

    public Type visit(True n) {
        
        
            
        return new BooleanType(n.line, n.row);
    }

    public Type visit(False n) {
        
        
            
        return new BooleanType(n.line, n.row);
    }

    public Type visit(IdentifierExp n) {
        
        
        Type tiporetorno = null;
        
        // Ordem da busca: Locais, parametro, classe, superclasse
        Symbol simbolo = Symbol.symbol(n.name.s);        
        
        VarInfo InfoVar = this.metodoAtual.localsTable.get(simbolo);
        if (InfoVar != null){
            tiporetorno = InfoVar.type;
        }
        else{
            InfoVar = this.metodoAtual.formalsTable.get(simbolo);
            
            if (InfoVar != null){
                tiporetorno = InfoVar.type;
            }
            else{
                ClassInfo InfoClasse = this.classeAtual;

                // TODO: Verificar se é necessário iterar na classe base    
                for (;InfoClasse != null;InfoClasse = InfoClasse.base){
                    InfoVar = InfoClasse.attributes.get(simbolo);
                    
                    if (InfoVar != null)
                        break;
                }
                
                if (InfoVar != null){
                    tiporetorno = InfoVar.type;
                }
                else{

                    this.env.err.Error(n, new Object[] {"Variável não declarada previamente",
                            "Variavel " + simbolo + " não foi declarada."});
                    
                        
                }
            }
        }
        
        
            
        
        return tiporetorno;
    }

    public Type visit(This n) {
        
        
            
        
        if (this.classeAtual != null){
            return new IdentifierType(n.line, n.row, this.classeAtual.name.toString());
        }
        else{
            this.env.err.Error(n, new Object[] {"Não é possível usar operação 'this' em um método estático."});
            
            
            return null;
        }
    }

    public Type visit(NewArray n) {
        
        
        // Verifica se o tamanho é um inteiro
        Type tsize = n.size.accept(this);
        
        if (!(tsize instanceof IntegerType)){
            this.env.err.Error(n, new Object[] {"O tamanho do 'array' não é do tipo 'int'",
                    "O operador new int[] deve ter o valor do tamanho inteiro",
                    "Tipo 'int' esperado, encontrado tipo " + tsize + "."});
            
                            
        }
        
            
        return new IntArrayType(n.line, n.row);
    }

    public Type visit(NewObject n) {
        
        
        Type tiporetorno = null;
        
        // Verifica se a classe desse objeto existe
        ClassInfo InfoClasse = this.tabsimb.get(Symbol.symbol(n.className.s));
        tiporetorno = new IdentifierType(n.line, n.row, n.className.s);
        
        if (InfoClasse == null){
            this.env.err.Error(n, new Object[] {"Instância de classe inexistente",
                    "A classe " + n.className.s + " não foi declarada previamente."});
            
            
            
        }
        
            
        return tiporetorno;
    }

    public Type visit(Not n) {
        
        
        Type texp = n.exp.accept(this);
        
        if (!(texp instanceof BooleanType)){
            this.env.err.Error(n, new Object[] {"expressão não-booleana para operação '!'",
                    "Tipo 'boolean' esperado, encontrado tipo " + texp + "."});
            
                
            
        }
        
            
        return new BooleanType(n.line, n.row);
    }

    public Type visit(Equal n) {
        
        
        // Verificar se a variavel existe
        Type tesq = n.lhs.accept(this);
        
        // verificar se o tipo da expressão equivale ao tipo
        Type tdir = n.rhs.accept(this);
        
        if ((tesq != null) && (tdir != null) && (tesq.isComparable(tdir) == false)){
            this.env.err.Error(n, new Object[] {"Operador == entre tipos diferentes!",
                "Tipo " + tesq + " e tipo " + tdir + " em comparação de apenas um tipo."});
        
            
        }
        
        
        
        return new BooleanType(n.line, n.row);
    }

    public Type visit(Identifier n) {

        
        
        Type tiporetorno = null;
        
        // Ordem da busca: Locais, parametro, classe, superclasse
        Symbol simbolo = Symbol.symbol(n.s);        
        
        VarInfo InfoVar = this.metodoAtual.localsTable.get(simbolo);
        if (InfoVar != null){
            tiporetorno = InfoVar.type;
        }
        else{
            InfoVar = this.metodoAtual.formalsTable.get(simbolo);
            
            if (InfoVar != null){
                tiporetorno = InfoVar.type;
            }
            else{
                ClassInfo InfoClasse = this.classeAtual;
                
                // TODO: Verificar se é necessário iterar na classe base                
                for (;InfoClasse != null;InfoClasse = InfoClasse.base){
                    InfoVar = InfoClasse.attributes.get(simbolo);
                    
                    if (InfoVar != null)
                        break;
                }
                
                if (InfoVar != null){
                    tiporetorno = InfoVar.type;
                }
                else{

                    this.env.err.Error(n, new Object[] {"Variável não declarada",
                            "Atribuído valor a variável " + simbolo + " não declarada."});
                    
                        
                }
            }
        }
        
        
            
        
        return tiporetorno;
    }

}
