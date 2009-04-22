/*
 * MC011 - 1o sem / 2007 - Professor Sandro Rigo
 * Segunda entrega
 * SemantTypeVisitor.java
 *
 * Grupo 20 : Andreia Silva Donalisio    ra026898
 *            Julia Martinez Perdigueiro ra024158
 */

package semant;

import util.List;
import symbol.*;
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
import syntaxtree.This;
import syntaxtree.Times;
import syntaxtree.True;
import syntaxtree.VarDecl;
import syntaxtree.While;
import visitor.Visitor;

/**
 * This class is responsible for building the environment table,
 * except for the class hierarchies (this is done by the SemantLinkVisitor).
 *
 * @author Andreia Silva Donalisio
 * @author Julia Martinez Perdigueiro
 *
 * @version 1.0
 */
public class SemantTypeVisitor implements Visitor { 
	
	private Env env;
	private ClassInfo classInfo;
	private MethodInfo methodInfo;
	private VarInfo varInfo;
	private VarInfo formal;
		
	public SemantTypeVisitor(Env e) {
		super();
		env = e;
	}

	public SemantTypeVisitor() {
		super();
	}
	
	public Env getEnv(){
        return env;
    }
	
	public void visit(Program node) {
		node.mainClass.accept(this);
	    List<ClassDecl> listaClasse = node.classList;
		for(;listaClasse != null; listaClasse = listaClasse.tail){
	        listaClasse.head.accept(this);
		env.classes.put(classInfo.name, classInfo);
		}
	}

	public void visit(MainClass node) {
		classInfo = new ClassInfo(Symbol.symbol(node.className.s)); 
		if(!env.classes.put(classInfo.name, classInfo)){
            String erro = "Redeclaration of class "+ node.className.s + ") on line "+node.line;
            env.err.Error(node, new Object[]{erro});
        }
	
	}

	public void visit(ClassDeclSimple node) {
		classInfo = new ClassInfo(Symbol.symbol(node.name.s));
	    if(!env.classes.put(classInfo.name, classInfo)){
            String erro = "Redeclaration of class "+ node.name.s + " on line "+node.line;
            env.err.Error(node, new Object[]{erro});
        }
		
	    methodInfo = null;
	    
		//attributes
		List<VarDecl> listaVar = node.varList;
		for(;listaVar != null; listaVar = listaVar.tail){
			listaVar.head.accept(this);
			classInfo.addAttribute(varInfo);
		}
		
		//methods
		List<MethodDecl> listaMetodo = node.methodList;
		for(;listaMetodo != null; listaMetodo = listaMetodo.tail){
			listaMetodo.head.accept(this);
			classInfo.addMethod(methodInfo);
		}
	}

	public void visit(ClassDeclExtends node) {
		// faz visit da classe como se fosse ClassDeclSimple para colher informacoes
		// (coloca informacao de extends depois, no SemantLinkVisitor)
		ClassDeclSimple classSimple = new ClassDeclSimple(node.line,node.row,node.name,node.varList,node.methodList);
		visit(classSimple);
	}

	public void visit(VarDecl node) {
		varInfo = new VarInfo(node.type, Symbol.symbol(node.name.toString())); 
		
		if(methodInfo != null){
            if(!methodInfo.addLocal(varInfo)){
                String erro = "Redeclaration of variable "+ node.name.s + ") on line "+node.line;
                env.err.Error(node, new Object[]{erro});
            }
        }
        else
            if(!classInfo.addAttribute(varInfo)){
                String erro = "Redeclaration of attribute "+ node.name.s + ") on line "+node.line;
                env.err.Error (node, new Object[]{erro});
            }
	
	}

	public void visit(MethodDecl node) {
		methodInfo = new MethodInfo(node.returnType, Symbol.symbol(node.name.toString()), classInfo.name);
		methodInfo.type = node.returnType;
	
        if(!classInfo.addMethod(methodInfo)){
            String erro = "Redeclaration of method "+ node.name.s + ") on line "+node.line;
            env.err.Error(node, new Object[]{erro});
        }
        		
		//local variables
		List<VarDecl> listaVar = node.locals;
		for(;listaVar != null; listaVar = listaVar.tail){
			listaVar.head.accept(this);
			methodInfo.addLocal(varInfo);
		}
		
		//parameters
		List<Formal> listaFormal = node.formals;
		for(;listaFormal != null; listaFormal = listaFormal.tail){
			listaFormal.head.accept(this);
			methodInfo.addFormal(formal);
		}	
	}

	public void visit(Formal node) {
		formal = new VarInfo(node.type,Symbol.symbol(node.name.s)); 
        if(!methodInfo.addFormal(formal)){
            String erro = "Redeclaration of parameter "+ node.name.s + ") on line "+node.line;
            env.err.Error(node, new Object[]{erro});
        }
	
	}

	public void visit(IntArrayType node)
	{
	}

	public void visit(BooleanType node)
	{
	}

	public void visit(IntegerType node)
	{
	}

	public void visit(IdentifierType node)
	{
	}

	public void visit(Block node)
	{
	}

	public void visit(If node)
	{
	}

	public void visit(While node)
	{
	}

	public void visit(Print node)
	{
	}

	public void visit(Assign node)
	{
	}

	public void visit(ArrayAssign node)
	{
	}

	public void visit(And node)
	{
	}

	public void visit(LessThan node)
	{
	}

	public void visit(Equal node)
	{
	}

	public void visit(Plus node)
	{
	}

	public void visit(Minus node)
	{
	}

	public void visit(Times node)
	{
	}

	public void visit(ArrayLookup node)
	{
	}

	public void visit(ArrayLength node)
	{
	}

	public void visit(Call node)
	{
	}

	public void visit(IntegerLiteral node)
	{
	}

	public void visit(True node)
	{
	}

	public void visit(False node)
	{
	}

	public void visit(This node)
	{
	}

	public void visit(NewArray node)
	{
	}

	public void visit(NewObject node)
	{
	}

	public void visit(Not node)
	{
	}

	public void visit(IdentifierExp node)
	{
	}

	public void visit(Identifier node)
	{
	}
}
