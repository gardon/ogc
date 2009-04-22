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
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Program...");
            DEBUG_TABS++;
        }
        
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
                
                if (DEBUG)
                {
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! This is a class redeclaration !");
                }
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
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Program");
        }
        

    }

    public void visit(MainClass node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing MainClass(" + node.className.s + ")...");    
            DEBUG_TABS++;
        }    

        // Buscando na tabela de simbolos a classe
        Symbol sym = Symbol.symbol(node.className.s);
        this.currentClass = table.get(sym);
        
        // Visitando seus metodos e atributos
        node.className.accept(this);
        node.mainArgName.accept(this);
        node.s.accept(this);
        
        this.currentClass = null;

        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing MainClass(" + node.className.s + ")");
        }
    }

    public void visit(ClassDeclSimple node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing ClassDeclSimple(" + node.name.s + ")...");
            DEBUG_TABS++;
        }

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
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing ClassDeclSimple(" + node.name.s + ")");
        }
    }

    public void visit(ClassDeclExtends node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing ClassDeclExtends(" + node.name.s + ")...");
            DEBUG_TABS++;
        }
        
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
                
                
                if (DEBUG)
                {
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! This is a class redeclaration!");
                }
                
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
        

        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing ClassDeclExtends(" + node.name.s + ")");
        }
    }

    public void visit(VarDecl node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing VarDecl(" + node.name.s + ")...");
            DEBUG_TABS++;
        }
        
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
                        
                if (DEBUG)
                    {
                        for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                        System.out.println("ERROR! This is an attribute redeclaration inside the same class!");
                    }
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
        

        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing VarDecl(" + node.name.s + ")");
        }

    }

    public void visit(MethodDecl node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing MethodDecl(" + node.name.s + ")...");
            DEBUG_TABS++;
        }
        
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

                        if (DEBUG){            
                            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                            System.out.println("ERROR! Wrong parameter!");
                        }
                    }

                    formalsIter = formalsIter.tail;
                    parformalsIter = parformalsIter.tail;
                }

                if (formalsIter != null)
                {
                    // Faltou parâmetros
                    this.env.err.Error(node, new Object[] {"Metodo redefinido inconsistente.", "A declaração do método " + this.currentClass.name + "." + node.name + " possui mais parâmetros que a classe pai."});

                    if (DEBUG){            
                        for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                        System.out.println("ERROR! Missing parameters!");
                    }                        
                }

                if (parformalsIter != null)
                {
                    // Sobrou parâmetros
                    this.env.err.Error(node, new Object[] {"Metodo redefinido inconsistente.", "A declaração do método " + this.currentClass.name + "." + node.name + " possui menos parâmetros que a classe pai."});

                    if (DEBUG){            
                        for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                        System.out.println("ERROR! Exceding parameters!");
                    }                        
                }
            }
            else
            {
                // Metodo já está declarado na classe atual
                this.env.err.Error(node, new Object[] {"Método redeclarado na classe!", "'" + this.currentClass.name + "." + node.name + "' ja foi declarado nesta classe."});

                if (DEBUG){            
                    for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                    System.out.println("ERROR! This is a method redeclaration!");
                }
            }
        }
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished MethodDecl(" + node.name.s + ")");
        }


    }

    public void visit(Formal node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing VarDecl(" + node.name.s + ")...");
            DEBUG_TABS++;
        }
        
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
            
            if (DEBUG){
                for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
                System.out.println("ERROR! This is a parameter redeclaration!");
            }
        }
                
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing VarDecl(" + node.name.s + ")");
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
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Block");
            DEBUG_TABS++;
        }
        
        List<syntaxtree.Statement> stIter = node.body;
        
        while(stIter != null){
            //Visitando
            stIter.head.accept(this);
            
            stIter = stIter.tail;
        }
                
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Block");
        }

        
    }

    public void visit(If node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing IF");
            DEBUG_TABS++;
        }
        
        //Parseando if-then-else
        node.condition.accept(this);
        
        //verificando then clause
        if(node.thenClause != null){
            node.thenClause.accept(this);
        }
        
        if (node.elseClause != null){
            node.elseClause.accept(this);
        }
                
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing IF");
        }
        
    }

    public void visit(While node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing While");
            DEBUG_TABS++;
        }
        
        node.condition.accept(this);
        node.body.accept(this);

        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing While");
        }
        
    }

    public void visit(Print node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Print");
            DEBUG_TABS++;
        }
        
        node.exp.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Print");
        }

    }

    public void visit(Assign node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Assign");
            DEBUG_TABS++;
        }
        
        node.var.accept(this);
        node.exp.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Assign");
        }

    }

    public void visit(ArrayAssign node) {
        // Parseando  
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing ArrayAssign");
            DEBUG_TABS++;
        }
        
        node.var.accept(this);
        node.index.accept(this);
        node.value.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing ArrayAssign");
        }

    }

    public void visit(And node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing And");
            DEBUG_TABS++;
        }
        
        node.lhs.accept(this);
        node.rhs.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing And");
        }

    }

    public void visit(LessThan node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Less Than");
            DEBUG_TABS++;
        }
        
        node.lhs.accept(this);
        node.rhs.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Less Than");
        }

    }

    public void visit(Equal node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Equal");
            DEBUG_TABS++;
        }
        
        node.lhs.accept(this);
        node.rhs.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Equal");
        }

    }

    public void visit(Plus node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Plus");
            DEBUG_TABS++;
        }
        
        node.lhs.accept(this);
        node.rhs.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Plus");
        }

    }

    public void visit(Minus node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Minus");
            DEBUG_TABS++;
        }
        
        node.lhs.accept(this);
        node.rhs.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Minus");
        }

    }

    public void visit(Times node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Times");
            DEBUG_TABS++;
        }
        
        node.lhs.accept(this);
        node.rhs.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Times");
        }

    }

    public void visit(ArrayLookup node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Array Look up");
            DEBUG_TABS++;
        }
        
        node.index.accept(this);
        node.array.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Array Look up");
        }

    }

    public void visit(ArrayLength node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Less Than");
            DEBUG_TABS++;
        }
        
        node.array.accept(this);
                
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Less Than");
        }

    }

    public void visit(Call node) {

        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Call");
            DEBUG_TABS++;
        }
        
        List<syntaxtree.Exp> expIter = node.actuals;
        
        //Parseando actuals
        while(expIter != null){
            expIter.head.accept(this);
            
            expIter = expIter.tail;
        }
        
        //Parseando objeto e metodo
        node.object.accept(this);
        node.method.accept(this);        
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Call");
        }

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
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing New Array");
            DEBUG_TABS++;
        }
        
        node.size.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing New Array");
        }

    }

    public void visit(NewObject node) {
        // Nada a fazer

    }

    public void visit(Not node) {
        
        if (DEBUG)
        {
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("Parsing Not");
            DEBUG_TABS++;
        }
        
        node.exp.accept(this);
        
        if (DEBUG)
        {
            DEBUG_TABS--;
            for (int i = 0; i < DEBUG_TABS; i++) System.out.print("\t");
            System.out.println("... finished parsing Not");
        }

    }

    public void visit(IdentifierExp node) {
        // Nada a fazer

    }

    public void visit(Identifier node) {
        // Nada a fazer
    }

}
