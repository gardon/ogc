/*
 * MC011 - 1o sem / 2007 - Professor Sandro Rigo
 * Segunda entrega
 * SemantLinkVisitor.java
 *
 * Grupo 20 : Andreia Silva Donalisio    ra026898
 *            Julia Martinez Perdigueiro ra024158
 */

package semant;

import errors.ErrorEchoer;
import symbol.ClassInfo;
import symbol.Symbol;
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
import util.List;
import visitor.Visitor;

/**
 * This class is responsible for linking the class hierarchies.
 * It is called after the environment table has already been constructed
 * by the SemantTypeVisitor.
 *
 * @author Andreia Silva Donalisio
 * @author Julia Martinez Perdigueiro
 *
 * @version 1.0
 */
public class SemantLinkVisitor implements Visitor {
	
	private ErrorEchoer err;
	private Env env;
	
	public SemantLinkVisitor(ErrorEchoer err, Env env) {
		this.err = err;
		this.env = env;
	}

	public void visit(Program node) {
	      List<ClassDecl> classes = node.classList;
	      
	      while ( classes != null ) {
	    	  // visita cada classe para fazer os links das classes
	    	  // que extendem outras classes
	    	  ClassDecl classDecl = classes.head;
	    	  classDecl.accept(this);
	        
	    	  // anda na lista
	    	  classes = classes.tail;
	      }
	}

	public void visit(MainClass node) {

	}

	public void visit(ClassDeclSimple node) {

	}

	public void visit(ClassDeclExtends node) {
		
		Symbol symbol = Symbol.symbol(node.name.toString());
		ClassInfo classInfo = env.classes.get(symbol);

		Symbol superSymbol = Symbol.symbol(node.superClass.toString());
		ClassInfo superClassInfo = env.classes.get(superSymbol);
		
		// avisa se nao achou superclasse
		if ( superClassInfo == null ) {
			err.Error(node, new Object[] {"Class not found " + superSymbol.toString()} );
		} else {
			// faz referencia entre classe e superclasse
			classInfo.base = superClassInfo;

			// adiciona no env
			env.classes.put(symbol, classInfo);
		}
	}

	public void visit(VarDecl node) {

	}

	public void visit(MethodDecl node) {

	}

	public void visit(Formal node) {

	}

	public void visit(IntArrayType node) {

	}

	public void visit(BooleanType node) {

	}

	public void visit(IntegerType node) {

	}

	public void visit(IdentifierType node) {

	}

	public void visit(Block node) {

	}

	public void visit(If node) {

	}

	public void visit(While node) {

	}

	public void visit(Print node) {

	}

	public void visit(Assign node) {

	}

	public void visit(ArrayAssign node) {

	}

	public void visit(And node) {

	}

	public void visit(LessThan node) {

	}

	public void visit(Equal node) {

	}

	public void visit(Plus node) {
		

	}

	public void visit(Minus node) {
		

	}

	public void visit(Times node) {
		

	}

	public void visit(ArrayLookup node) {
		

	}

	public void visit(ArrayLength node) {
		

	}

	public void visit(Call node) {
		

	}

	public void visit(IntegerLiteral node) {
		

	}

	public void visit(True node) {
		

	}

	public void visit(False node) {
		

	}

	public void visit(This node) {
		

	}

	public void visit(NewArray node) {
		

	}

	public void visit(NewObject node) {
		

	}

	public void visit(Not node) {
		

	}

	public void visit(IdentifierExp node) {
		

	}

	public void visit(Identifier node) {
		

	}

}
