package semant;

import syntaxtree.*;
import symbol.*;
import visitor.Visitor;
import util.List;

public class FirstPassVisitor implements Visitor {
    
    private static int DEBUG_TABS = 0;
    private static final boolean DEBUG = true;    // Essa variavel deve estar true apenas durante o desenvolvimento do compilador
    private Table<ClassInfo> table;
    private ClassInfo currentClass;        // Variavel que guarda a classe que está sendo parseada
    private MethodInfo currentMethod;    // Variavel que guarda o método que está sendo parseado
    private Env env;
    
    public FirstPassVisitor(Table<ClassInfo> table, Env env)
    {
        // Recebe a tabela de simbolos para ser preenchida
        this.table = table;
        this.currentClass = null;
        this.currentMethod = null;
        this.env = env;
    }
    
    
    public void visit(Program node) {
        
        
        Symbol sym = Symbol.symbol(node.mainClass.className.s);
        ClassInfo cinfo = new ClassInfo(sym);
        table.put(sym, cinfo);
        
        // Parseando as classes deste programa
        List<syntaxtree.ClassDecl> classIter = node.classList;
        List<syntaxtree.ClassDecl> goodClassList = null;
        
        while (classIter != null)
        {                        
            // Inserindo a classe na tabela// Adicionando a tabela de simbolos a classe
            sym = Symbol.symbol(classIter.head.name.s);

            // Checando se a classe ja foi declarada
            if (table.get(sym) == null)
            {
                cinfo = new ClassInfo(sym);
                table.put(sym, cinfo);
                
                goodClassList = new List<syntaxtree.ClassDecl>(classIter.head, goodClassList);
                
            }
            else
            {
                // Ja existe uma classe com esse nome
                this.env.err.Error(node, new Object[] {"Classe redeclarada!", "'" + sym + "' ja foi declarada anteriormente neste programa."});
                
            }
            
            classIter = classIter.tail;
        }

        
        // Parseando o metodo principal
        node.mainClass.accept(this);
        
        // Parseando as classes deste programa na ordem correta
        List<syntaxtree.ClassDecl> parsedClassList = null;
        List<syntaxtree.ClassDecl> parsedIter = null;
        int parsed;
        
        do
        {
            parsed = 0;
            classIter = goodClassList;
            
            while (classIter  != null)
            {            
                // Verificando viabilidade parsear a classe
                ClassDecl theClass = classIter.head;
                // Já parseei?!
                parsedIter = parsedClassList;
                
                boolean hasParsed = false;
                
                while (parsedIter != null)
                {
                    if (parsedIter.head.name.s.compareTo(theClass.name.s) == 0)
                    {
                        // Já foi parseado  
                        hasParsed = true;
                        break;
                    }
                    
                    parsedIter = parsedIter.tail;
                }
                
                if (hasParsed == false)
                {
                    if (theClass instanceof ClassDeclSimple)
                    {
                        // Classe não tem pai, vou parsear!
                        theClass.accept(this);
                        parsed++;
                        parsedClassList = new List<syntaxtree.ClassDecl>(theClass, parsedClassList);
                    }
                    else if (theClass instanceof ClassDeclExtends)
                    {
                        // a classe tem pai
                        ClassDeclExtends currentClassExtends = (ClassDeclExtends)theClass;
                        
                        // O pai já foi parseado?
                        parsedIter = parsedClassList;
                        
                        while (parsedIter != null)
                        {
                            if (parsedIter.head.name.s.compareTo(currentClassExtends.superClass.s) == 0)
                            {
                                // O pai já foi parseado!
                                theClass.accept(this);
                                parsed++;
                                parsedClassList = new List<syntaxtree.ClassDecl>(theClass, parsedClassList);
                                break;
                            }
                            
                            parsedIter = parsedIter.tail;
                        }
                    }
                }
                
                classIter = classIter.tail;
                
            }
        }
        while (parsed > 0);
        
        classIter = goodClassList;
        
        while (classIter  != null)
        {
            // Verificando viabilidade parsear a classe
            ClassDecl theClass = classIter.head;
            
            // Já parseei?!
            parsedIter = parsedClassList;
            
            boolean hasParsed = false;
            
            while (parsedIter != null)
            {
                if (parsedIter.head.name.s.compareTo(theClass.name.s) == 0)
                {
                    // Já foi parseado  
                    hasParsed = true;
                    break;
                }
                
                parsedIter = parsedIter.tail;
            }
            
            if (hasParsed == false)
                theClass.accept(this);
            
            classIter = classIter.tail;
        }
    }

    public void visit(MainClass node) {

        // Buscando na tabela de simbolos a classe
        Symbol sym = Symbol.symbol(node.className.s);
        this.currentClass = table.get(sym);
        
        // Visitando seus metodos e atributos
        node.className.accept(this);
        node.mainArgName.accept(this);
        node.s.accept(this);
        
        this.currentClass = null;

    }

    public void visit(ClassDeclSimple node) {
        
        // Buscando na tabela de simbolos a classe
        Symbol sym = Symbol.symbol(node.name.s);
        this.currentClass = table.get(sym);
        
        // 1. Parsear os atributos
        List<syntaxtree.VarDecl> varIter = node.varList;
        
        while (varIter != null)
        {            
            // Visitando
            varIter.head.accept(this);

            varIter = varIter.tail;
        }

        // 2. Parsear os metodos        
        List<syntaxtree.MethodDecl> methIter = node.methodList;

        while (methIter != null)
        {
            // Visitando o método
            methIter.head.accept(this);

            methIter = methIter.tail;
        }
    
        this.currentClass = null;
        
    }

    public void visit(ClassDeclExtends node) {
        
        // Buscando na tabela de simbolos a classe
        Symbol sym = Symbol.symbol(node.name.s);
        this.currentClass = table.get(sym);
        
        // Buscando o pai
        sym = Symbol.symbol(node.superClass.s);
        ClassInfo parent = table.get(sym);
        
        // Verificando dependencia circular
        ClassInfo parentIter = parent;
        while (parentIter != null)
        {
            if (parentIter.name == this.currentClass.name)
            {
                // Ja existe uma classe com esse nome
                this.env.err.Error(node, new Object[] {"Herança cíclica!", "A classe '" + this.currentClass.name + "' ao herdar de '" + node.superClass.s + "' gera um ciclo de heranças."});
                
                break;
            }
            
            parentIter = parentIter.base;
        }
        
        if (parentIter == null)
        {
            this.currentClass.setBase(parent);
        }
        
        // 1. Parsear os atributos
        List<syntaxtree.VarDecl> varIter = node.varList;
        
        while (varIter != null)
        {            
            // Visitando
            varIter.head.accept(this);

            varIter = varIter.tail;
        }

        // 2. Parsear os metodos        
        List<syntaxtree.MethodDecl> methIter = node.methodList;

        while (methIter != null)
        {
            // Visitando o método
            methIter.head.accept(this);

            methIter = methIter.tail;
        }
    
        this.currentClass = null;
    }

    public void visit(VarDecl node) {
        
        Symbol sym = Symbol.symbol(node.name.s);        
        
        // Checar por redeclaracao de variaveis
        if (this.currentMethod == null)
        {    
            // Esta variavel eh um atributo de classe
            if (this.currentClass.attributes.containsKey(sym) == false){
                            
                // A variavel ainda nao foi declarada, criar varinfo            
                VarInfo vinfo = new VarInfo(node.type, sym);
                                
                // Adicionado o atributo ao ClassInfo da classe atual
                this.currentClass.addAttribute(vinfo);
            }
            else{
                // Redeclaracao de variavel na classe atual,
                
                this.env.err.Error(node, new Object[] {"Variavel redeclarada na classe!", "A variável '" + node.name.s + "' ja foi declarada na classe '" + this.currentClass.name + "'."});
            }
        }
        
        else
        {
            // Esta variavel e uma variavel local de um metodo
            
            //verificando se ja nao foi declarado como parametro ou variavel local
            if ((this.currentMethod.localsTable.containsKey(sym) == false) && 
                (this.currentMethod.formalsTable.containsKey(sym) == false)){

                // A variável ainda não foi declarada, criar varinfo
                VarInfo vinfo = new VarInfo(node.type, sym);
                    
                // Adicionado o atributo ao� methodinfo do metodo atual
                this.currentMethod.addLocal(vinfo);
            }
            
            else
            {
                // Redeclaração de variável no método
                this.env.err.Error(node, new Object[] {"Variavel redeclarada no metodo.", "A variável '" + node.name.s + "' ja foi declarada no metodo'" + this.currentMethod.name + "' da classe '" + this.currentClass.name +  "'."});
                
                if (DEBUG)
                {
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! This is a variable redeclaration!");
                }
            }
        }
        

    }

    public void visit(MethodDecl node) {
        
        Symbol sym = Symbol.symbol(node.name.s);
        Symbol symparent = this.currentClass.name;
        
        if (this.currentClass.methods.containsKey(sym) == false){
            
            //metodo ainda nao existe na classe atual e nem nos pais
            MethodInfo minfo = new MethodInfo(node.returnType, sym, symparent);
            this.currentMethod = minfo;
            this.currentClass.addMethod(minfo);
                                
            // Parseando os parametros
            List<syntaxtree.Formal> forIter = node.formals;
            
            while (forIter != null)
            {            
                // Visitando
                forIter.head.accept(this);
                
                forIter = forIter.tail;
            }            
            
            // Parseando as variaveis locais
            List<syntaxtree.VarDecl> varIter = node.locals;
            
            while (varIter != null)
            {            
                // Visitando
                varIter.head.accept(this);
                
                varIter = varIter.tail;
            }
            
            // Parseando o corpo do metodo
            List<syntaxtree.Statement> stIter = node.body;
            
            while(stIter != null){
                //Visitando
                stIter.head.accept(this);
                
                stIter = stIter.tail;
            }
            
            // Parseando Expression            
            node.returnExp.accept(this);
            
            this.currentMethod = null;
        }
        else
        {        
            // Checagem por redeclaracao de metodos na classe pai
            ClassInfo parent = this.currentClass.base;
            MethodInfo parentMethod = null;
            
            while (parent != null)
            {
                if (parent.methods.containsKey(sym) == true)
                {
                    parentMethod = parent.methods.get(sym);
                    break;
                }
                
                parent = parent.base;
            }
            
            // Verifica se o tipo de retorno, o tipo dos parametros e o numero de parametros bate com pai
            if (parentMethod != null)
            {
                if (parentMethod.type.isComparable(node.returnType) == false)
                {
                    //     Tipos de retorno inconsistentes
                    this.env.err.Error(node, new Object[] {"Metodo redefinido inconsistente.", "O método'" + node.name.s + "' ja foi declarado na classe pai '" + parentMethod.name + "' com outro tipo de retorno."});
                    
                    if (DEBUG)
                    {
                        for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                        System.out.println("ERROR! Wrong return type!");
                    }
                }
                
                // Verificar se os parâmetros coincidem, numero e tipo
                List<VarInfo> parformalsIter = parentMethod.formals;
                List<Formal> formalsIter = node.formals;

                while ((parformalsIter != null) && (formalsIter  != null))
                {
                    if (parformalsIter.head.type.isComparable(formalsIter.head.type) == false)
                    {
                        // Parametro não bate o tipo
                        this.env.err.Error(node, new Object[] {"Metodo redefinido inconsistente.", 
                                "O método " + this.currentClass.name + "." + node.name + " possui o parâmetro " + formalsIter.head.name + " com tipo incoerente ao do pai.",
                                "Esperando um tipo " + parformalsIter.head.type + ", encontrado o tipo " + formalsIter.head.type + "."});

                    }

                    formalsIter = formalsIter.tail;
                    parformalsIter = parformalsIter.tail;
                }

                if (formalsIter != null)
                {
                    // Faltou parâmetros
                    this.env.err.Error(node, new Object[] {"Metodo redefinido inconsistente.", "A declaração do método " + this.currentClass.name + "." + node.name + " possui mais parâmetros que a classe pai."});

                }

                if (parformalsIter != null)
                {
                    // Sobrou parâmetros
                    this.env.err.Error(node, new Object[] {"Metodo redefinido inconsistente.", "A declaração do método " + this.currentClass.name + "." + node.name + " possui menos parâmetros que a classe pai."});

                }
            }
            else
            {
                // Metodo já está declarado na classe atual
                this.env.err.Error(node, new Object[] {"Método redeclarado na classe!", "'" + this.currentClass.name + "." + node.name + "' ja foi declarado nesta classe."});

            }
        }
        
    }

    public void visit(Formal node) {

        Symbol sym = Symbol.symbol(node.name.s);
        
        //verificando se ja nao foi declarado como parametro ou variavel local
        if ((this.currentMethod.localsTable.containsKey(sym) == false) && 
            (this.currentMethod.formalsTable.containsKey(sym) == false)){

                // Parametro ainda nao declarada, criar varinfo
                VarInfo finfo = new VarInfo(node.type, sym);
                    
                // Adicionando o parametro ao methodinfo do metodo atual
                this.currentMethod.addFormal(finfo);
        }
            
        else{
            // Redeclaração de parametro no método atual
            this.env.err.Error(node, new Object[] {"Método com parâmetros com o mesmo nome!", "O parâmetro '" + node.name.s + "' do método '" + this.currentMethod.name + "' é definido mais de uma vez."});
            
        }
    }

        

    public void visit(IntArrayType node) {
        // Nada a fazer        
    }

    public void visit(BooleanType node) {
        // Nada a fazer

    }

    public void visit(IntegerType node) {
        // Nada a fazer

    }

    public void visit(IdentifierType node) {
        // Nada a fazer

    }

    public void visit(Block node) {
        
        List<syntaxtree.Statement> stIter = node.body;
        
        while(stIter != null){
            //Visitando
            stIter.head.accept(this);
            
            stIter = stIter.tail;
        }
                
        
    }

    public void visit(If node) {

        //Parseando if-then-else
        node.condition.accept(this);
        
        //verificando then clause
        if(node.thenClause != null){
            node.thenClause.accept(this);
        }
        
        if (node.elseClause != null){
            node.elseClause.accept(this);
        }
                
    }

    public void visit(While node) {
        
        node.condition.accept(this);
        node.body.accept(this);

    }

    public void visit(Print node) {
        
        node.exp.accept(this);
        
    }

    public void visit(Assign node) {
        
        node.var.accept(this);
        node.exp.accept(this);
        
    }

    public void visit(ArrayAssign node) {
        // Parseando  
        
        node.var.accept(this);
        node.index.accept(this);
        node.value.accept(this);
        
    }

    public void visit(And node) {

        node.lhs.accept(this);
        node.rhs.accept(this);
        
    }

    public void visit(LessThan node) {
        
        node.lhs.accept(this);
        node.rhs.accept(this);
        
    }

    public void visit(Equal node) {

        node.lhs.accept(this);
        node.rhs.accept(this);
        
    }

    public void visit(Plus node) {

        node.lhs.accept(this);
        node.rhs.accept(this);
        
    }

    public void visit(Minus node) {

        node.lhs.accept(this);
        node.rhs.accept(this);
        
    }

    public void visit(Times node) {

        node.lhs.accept(this);
        node.rhs.accept(this);
        
    }

    public void visit(ArrayLookup node) {

        node.index.accept(this);
        node.array.accept(this);
        
    }

    public void visit(ArrayLength node) {

        node.array.accept(this);
                
    }

    public void visit(Call node) {

        List<syntaxtree.Exp> expIter = node.actuals;
        
        //Parseando actuals
        while(expIter != null){
            expIter.head.accept(this);
            
            expIter = expIter.tail;
        }
        
        //Parseando objeto e metodo
        node.object.accept(this);
        node.method.accept(this);        
        
    }

    public void visit(IntegerLiteral node) {
        // Nada a fazer

    }

    public void visit(True node) {
        // Nada a fazer

    }

    public void visit(False node) {
        // Nada a fazer

    }

    public void visit(This node) {
        // Nada a fazer

    }

    public void visit(NewArray node) {
        
        node.size.accept(this);
        
    }

    public void visit(NewObject node) {
        // Nada a fazer

    }

    public void visit(Not node) {
        
        node.exp.accept(this);
        
    }

    public void visit(IdentifierExp node) {
        // Nada a fazer

    }

    public void visit(Identifier node) {
        // Nada a fazer
    }

}
