/*
 * MC011 - 1o sem / 2007 - Professor Sandro Rigo
 * Segunda entrega
 * TypeChecker.java
 *
 * Grupo 20 : Andreia Silva Donalisio    ra026898
 *            Julia Martinez Perdigueiro ra024158
 */

package semant;

import syntaxtree.Program;
import errors.ErrorEchoer;

/**
 * This class is the entry point for the compiler semantic analysis.
 * It is responsible for building the environment table and type
 * checking the program.
 *
 * @author Andreia Silva Donalisio
 * @author Julia Martinez Perdigueiro
 *
 * @version 1.0
 */
public class TypeChecker
{
    private TypeChecker()
    {
        super();
    }

    public static Env TypeCheck(ErrorEchoer err, Program p) {		
    	Env e = new Env(err);
    	SemantTypeVisitor v = new SemantTypeVisitor(e);
        p.accept(v);
        
        SemantLinkVisitor l = new SemantLinkVisitor(err, e);
        p.accept(l);

        SemantTypeVisitorAdapter s = new SemantTypeVisitorAdapter(err, e);
        p.accept(s);
           
        return e;
    }
}

