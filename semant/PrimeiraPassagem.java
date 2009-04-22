package semant;

import syntaxtree.*;
import symbol.*;
import visitor.Visitor;
import util.List;

public class PrimeiraPassagem implements Visitor {
   
    private Table<ClassInfo> tabsimb;    // tabela de simbolos
    private ClassInfo classeAtual;        // classe sendo parseada
    private MethodInfo metodoAtual;    // metodo sendo parseado
    private Env env;
   
    public PrimeiraPassagem (Table<ClassInfo> tabsimb, Env env) {
        // recebe a tabela de simbolos, inicialmente vazia
        this.tabsimb = tabsimb;
        this.classeAtual = null;
        this.metodoAtual = null;
        this.env = env;
    }
   
   
    public void visit(Program node) {
       
        Symbol simbolo = Symbol.symbol(node.mainClass.className.s);
        ClassInfo InfoClasse = new ClassInfo(simbolo);
        tabsimb.put(simbolo, InfoClasse);

        // Parser das classes do programa       
        List<syntaxtree.ClassDecl> IndexClasse = node.classList;
        List<syntaxtree.ClassDecl> ListaDeClasses = null;
      
        // Insere classe na tabela 
        for(;IndexClasse != null; IndexClasse = IndexClasse.tail) {                        
            simbolo = Symbol.symbol(IndexClasse.head.name.s);

            // Verifica redeclaração de classe
            if (tabsimb.get(simbolo) == null) {
                InfoClasse = new ClassInfo(simbolo);
                tabsimb.put(simbolo, InfoClasse);
               
                ListaDeClasses = new List<syntaxtree.ClassDecl>(IndexClasse.head, ListaDeClasses);
               
            }
            else {
                this.env.err.Error(node, new Object[] {"Redeclaração de classe", "'" + simbolo + "' foi declarada mais de uma vez."});
               
            }
           
        }

        // Parser do metodo principal
        node.mainClass.accept(this);
       
        // Parser das classes do programa, hierarquicamente
        List<syntaxtree.ClassDecl> ClassesParseadas = null;
        List<syntaxtree.ClassDecl> IndexPars = null;
        int parseado;
       
        do {
            parseado = 0;
            IndexClasse = ListaDeClasses;
           
            for(;IndexClasse != null; IndexClasse = IndexClasse.tail) {            
                // Verifican se pode parsear a classe
                ClassDecl ClasseA = IndexClasse.head;
                IndexPars = ClassesParseadas;
               
                int parseada = 0;
               
                for(;IndexPars != null; IndexPars = IndexPars.tail)
                    if (IndexPars.head.name.s.compareTo(ClasseA.name.s) == 0) {
                        parseada = 1;
                        break;
                    }
                   
                if (parseada == 0) {
                    if (ClasseA instanceof ClassDeclSimple) {
                        // Parser da classe, somente se for órfã (vide relatório).
                        ClasseA.accept(this);
                        parseado++;
                        ClassesParseadas = new List<syntaxtree.ClassDecl>(ClasseA, ClassesParseadas);
                    }
                    else if (ClasseA instanceof ClassDeclExtends) {
                        // Verifica se a classe herda de alguém
                        ClassDeclExtends classeExAtual = (ClassDeclExtends)ClasseA;
                       
                        IndexPars = ClassesParseadas;
                       
                        for(;IndexPars != null; IndexPars = IndexPars.tail) {
                            if (IndexPars.head.name.s.compareTo(classeExAtual.superClass.s) == 0) {
                                // Se o pai já foi parsed, posso parsear a classe.
                                ClasseA.accept(this);
                                parseado++;
                                ClassesParseadas = new List<syntaxtree.ClassDecl>(ClasseA, ClassesParseadas);
                                break;
                            }
                        }
                    }
                }
            }
        }
        while (parseado > 0);
       
        IndexClasse = ListaDeClasses;
       
        for(;IndexClasse != null; IndexClasse = IndexClasse.tail) {
            ClassDecl ClasseA = IndexClasse.head;
           
            IndexPars = ClassesParseadas;
           
            int parseada = 0;
           
            for(;IndexPars != null; IndexPars = IndexPars.tail) {
                if (IndexPars.head.name.s.compareTo(ClasseA.name.s) == 0) {
                    parseada = 1;
                    break;
                }
            }
           
            if (parseada == 0)
                ClasseA.accept(this);
        }
    }

    public void visit(MainClass node) {

        // Busca classe atual na tabela de simbolos
        Symbol simbolo = Symbol.symbol(node.className.s);
        this.classeAtual = tabsimb.get(simbolo);
       
        // Visita metodos e atributos da classe
        node.className.accept(this);
        node.mainArgName.accept(this);
        node.s.accept(this);
       
        this.classeAtual = null;

    }

    public void visit(ClassDeclSimple node) {
       
        // Busca classe atual na tabela de simbolos
        Symbol simbolo = Symbol.symbol(node.name.s);
        this.classeAtual = tabsimb.get(simbolo);
       
        // Parsea atributos
        List<syntaxtree.VarDecl> IndexVar = node.varList;
       
        for(;IndexVar != null; IndexVar = IndexVar.tail)
            IndexVar.head.accept(this);

        // Parsea metodos        
        List<syntaxtree.MethodDecl> IndexMetodo = node.methodList;

        for(;IndexMetodo != null; IndexMetodo = IndexMetodo.tail)
            IndexMetodo.head.accept(this);
   
        this.classeAtual = null;
       
    }

    public void visit(ClassDeclExtends node) {
       
        // Busca classe atual na tabela de simbolos
        Symbol simbolo = Symbol.symbol(node.name.s);
        this.classeAtual = tabsimb.get(simbolo);
       
        // Busca o pai
        simbolo = Symbol.symbol(node.superClass.s);
        ClassInfo pai = tabsimb.get(simbolo);
       
        // Verifica heranca ciclica
        ClassInfo IndexPais = pai;
        for(;IndexPais != null; IndexPais = IndexPais.base) {
            if (IndexPais.name == this.classeAtual.name) {
                this.env.err.Error(node, new Object[] {"Herança cíclica", "As classes '" + this.classeAtual.name + "' e '" + node.superClass.s + "' possuem herança cíclica."});
                break;
            }
           
        }
       
        if (IndexPais == null)
            this.classeAtual.setBase(pai);
       
        // Parsea atributos
        List<syntaxtree.VarDecl> IndexVar = node.varList;
       
        for(;IndexVar != null; IndexVar = IndexVar.tail)
            IndexVar.head.accept(this);


        // Parsea metodos        
        List<syntaxtree.MethodDecl> IndexMetodo = node.methodList;

        for(;IndexMetodo != null; IndexMetodo = IndexMetodo.tail)
            IndexMetodo.head.accept(this);

        this.classeAtual = null;
    }

    public void visit(VarDecl node) {
       
        Symbol simbolo = Symbol.symbol(node.name.s);        
       
        // Procura variaveis redeclaradas
        if (this.metodoAtual == null) {    
            if (this.classeAtual.attributes.containsKey(simbolo) == false) {
                           
                // Variavel nao declarada, cria varinfo            
                VarInfo InfoVar = new VarInfo(node.type, simbolo);
                               
                this.classeAtual.addAttribute(InfoVar);
            }
            else
                this.env.err.Error(node, new Object[] {"Redeclaração de variável", "A classe '" + this.classeAtual.name + "' já possui uma declaração de '" + node.name.s + "'."});
        }
       
        else {
           
            if ((this.metodoAtual.localsTable.containsKey(simbolo) == false) &&
                (this.metodoAtual.formalsTable.containsKey(simbolo) == false)) {

                VarInfo InfoVar = new VarInfo(node.type, simbolo);
                   
                this.metodoAtual.addLocal(InfoVar);
            }
           
            else
                // Redeclaração de variável no método
                this.env.err.Error(node, new Object[] {"Redeclaração de variável", "O método '" + this.metodoAtual.name + "' já possui uma declaração de '" + node.name.s + "'."});               
        }

    }

    public void visit(MethodDecl node) {
       
        Symbol simbolo = Symbol.symbol(node.name.s);
        Symbol simboloPai = this.classeAtual.name;
       
        if (this.classeAtual.methods.containsKey(simbolo) == false) {
           
            // Método ainda não declarado
            MethodInfo InfoMetodo = new MethodInfo(node.returnType, simbolo, simboloPai);
            this.metodoAtual = InfoMetodo;
            this.classeAtual.addMethod(InfoMetodo);
                               
            // Parser dos parametros
            List<syntaxtree.Formal> forIter = node.formals;
           
            for(;forIter != null; forIter = forIter.tail)
                forIter.head.accept(this);
           
            // Parser das variaveis locais
            List<syntaxtree.VarDecl> IndexVar = node.locals;
           
            for(;IndexVar!= null; IndexVar = IndexVar.tail)
                IndexVar.head.accept(this);
           
            // Parser do corpo do metodo
            List<syntaxtree.Statement> IndexClausula = node.body;
           
            for(;IndexClausula != null; IndexClausula = IndexClausula.tail)
                IndexClausula.head.accept(this);
           
            // Parser da Expression            
            node.returnExp.accept(this);
           
            this.metodoAtual = null;
        }
        else {        
            // Verifica redeclaração de métodos
            ClassInfo pai = this.classeAtual.base;
            MethodInfo MetodoDoPai = null;
           
            for(;pai != null; pai = pai.base) {
                if (pai.methods.containsKey(simbolo) == true) {
                    MetodoDoPai = pai.methods.get(simbolo);
                    break;
                }
            }
           
            if (MetodoDoPai != null)
            {
                if (MetodoDoPai.type.isComparable(node.returnType) == false)
                    // Tipos de retorno inconsistentes
                    this.env.err.Error(node, new Object[] {"Redefinição inconsistente de método", "O método'" + node.name.s + "' foi declarado anteriormente em '" + MetodoDoPai.name + "' com outro tipo de retorno."});
                   
               
                // Verificação de parâmetros: quantidade e tipagem
                List<VarInfo> parformalsIter = MetodoDoPai.formals;
                List<Formal> formalsIter = node.formals;

                while ((parformalsIter != null) && (formalsIter  != null)) {
                    if (parformalsIter.head.type.isComparable(formalsIter.head.type) == false)
                    {
                        // Parâmetro com problemas de tipagem
                        this.env.err.Error(node, new Object[] {"Redefinição inconsistente de método",
                                "O método " + this.classeAtual.name + "." + node.name + " possui o parâmetro " + formalsIter.head.name + " cujo tipo difere do pai.",
                                "Tipo do parâmetro no pai: " + parformalsIter.head.type + ". Tipo do parâmetro no filho: " + formalsIter.head.type + "."});

                    }

                    formalsIter = formalsIter.tail;
                    parformalsIter = parformalsIter.tail;
                }

                if (formalsIter != null)
                    // Erro no número de parâmetros
                    this.env.err.Error(node, new Object[] {"Redefinição inconsistente de método", "O método " + this.classeAtual.name + "." + node.name + " possui mais parâmetros que a classe pai."});

                if (parformalsIter != null)
                    // Erro no número de parâmetros
                    this.env.err.Error(node, new Object[] {"Redefinição inconsistente de método", "O método " + this.classeAtual.name + "." + node.name + " possui menos parâmetros que a classe pai."});

            }
            else
                // Metodo já declarado
                this.env.err.Error(node, new Object[] {"Redefinição de método", "A classe '" + this.classeAtual.name + "' já possui uma declaração de '" + node.name + "'."});

        }
       
    }

    public void visit(Formal node) {

        Symbol simbolo = Symbol.symbol(node.name.s);
       
        if ((this.metodoAtual.localsTable.containsKey(simbolo) == false) &&
            (this.metodoAtual.formalsTable.containsKey(simbolo) == false)){

                VarInfo finfo = new VarInfo(node.type, simbolo);
                   
                this.metodoAtual.addFormal(finfo);
        }
           
        else
            // Redeclaração de parâmetro no método
            this.env.err.Error(node, new Object[] {"Redefinição de parâmetro", "O parâmetro '" + node.name.s + "' do método '" + this.metodoAtual.name + "' é definido mais de uma vez."});
           
    }
       

    public void visit(IntArrayType node) {
        // Sem ação        

    }

    public void visit(BooleanType node) {
        // Sem ação

    }

    public void visit(IntegerType node) {
        // Sem ação

    }

    public void visit(IdentifierType node) {
        // Sem ação

    }

    public void visit(Block node) {
       
        List<syntaxtree.Statement> IndexClausula = node.body;
       
        for(;IndexClausula != null; IndexClausula = IndexClausula.tail)
            IndexClausula.head.accept(this);
               
    }

    public void visit(If node) {


        // Parsear if-then-else
        node.condition.accept(this);
       
        // Checar clause
        if(node.thenClause != null)
            node.thenClause.accept(this);
       
        if (node.elseClause != null)
            node.elseClause.accept(this);
               
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

        List<syntaxtree.Exp> IndexExpr = node.actuals;
       
        // Parser actuals
        for(;IndexExpr != null; IndexExpr = IndexExpr.tail)
            IndexExpr.head.accept(this);
       
        // Parser do objeto e metodo
        node.object.accept(this);
        node.method.accept(this);        
       
    }

    public void visit(IntegerLiteral node) {
        // Sem ação

    }

    public void visit(True node) {
        // Sem ação

    }

    public void visit(False node) {
        // Sem ação

    }

    public void visit(This node) {
        // Sem ação

    }

    public void visit(NewArray node) {
       
        node.size.accept(this);
       
    }

    public void visit(NewObject node) {
        // Sem ação

    }

    public void visit(Not node) {
       
        node.exp.accept(this);
       
    }

    public void visit(IdentifierExp node) {
        // Sem ação

    }

    public void visit(Identifier node) {
        // Sem ação
    }

}
