package main;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.PushbackReader;

import assem.Instr;

import canon.BasicBlocks;
import canon.Canon;
import canon.TraceSchedule;

import errors.ErrorEchoer;
import frame.Frame;
import frame.Proc;

import reg_alloc.RegAlloc;
import semant.Env;
import semant.TypeChecker;
import syntaxtree.Program;
import translate.Frag;
import translate.ProcFrag;
import translate.Translate;
import translate.VtableFrag;
import util.List;
import util.conversor.SyntaxTreeGenerator;

import minijava.lexer.Lexer;
import minijava.node.Start;
import minijava.parser.Parser;

// Uma coisa nao especificada em minijava eh se
// subclasses podem redeclarar atributos.
// Solucao adotada: podem; perdem o acesso a variavel
// da super classe se o fizerem.
public final class Main
{       
	public static void main(String[] args) 
	{
		try
		{
            String name = args.length == 0 ? "stdin" : args[0];
            
			// from here...
			InputStream is = args.length == 0 ? 
					         System.in : 
					         new FileInputStream(args[0]);
			InputStreamReader input = new InputStreamReader(is);
			PushbackReader pushback = new PushbackReader(input);
			Lexer lexer = new Lexer(pushback);
			Parser parser = new Parser(lexer);
			
			Start s = parser.parse();
			
            // the parser is correct(???); no need to print sablecc's AST.
			//System.out.println(s);
            
			// ... up until here, classes and package organization
			// are decided by SableCC
			
			// Translating from SableCC's to Appel's internal representation
			Program program = SyntaxTreeGenerator.convert(s);
			
			// by doing this we force a garbage collection to occur.
			// the entire tree structure generated by the front-end will
			// now be unreacheble, so we hope the garbage collector will release
			// its memory. :D
			s = null;
			parser = null;
			lexer = null;
			pushback = null;
			input = null;
			is = null;
			
			System.gc();
			
			// Now, we're using Appel's data structure.
			// We print it here for debugging purposes.
            
            //--------------------------------------------------
            // It seems everithing is working up until here,
            // so we'll not print the syntaxtree anymore.
			// PrettyPrint v1 = new PrettyPrint(System.err);
			// program.accept(v1);
            //--------------------------------------------------
			
			// now we've got to apply the 2-pass semant analyser.
            ErrorEchoer err = new SimpleError(name);
            Env env = TypeChecker.TypeCheck(err, program);

            if ( err.ErrorCount() != 0 )
            {
                err.Print(new Object[]{err.ErrorCount() + " erros", err.WarningCount() + " avisos"});
                return;
            }
			
			// here the AST is transformed into the Intemediate Representation
            Frame frame = new x86.Frame();
            Frag f = Translate.translate(frame, env, program);
            
            // no we're done with the syntaxtree structure. make the whole
            // structure unreacheable and gc.
            program = null;
            env = null;
            err = null;
            
            System.gc();

            // external functions and program entry point
            PrintStream out = new PrintStream("minijava.asm");
            
            out.println("    BITS 32");
            out.println("");
            out.println("    EXTERN _minijavaExit");
            out.println("    EXTERN _printInt");
            out.println("    EXTERN _newObject");
            out.println("    EXTERN _newArray");
            out.println("    EXTERN _assertPtr");
            out.println("    EXTERN _boundCheck");
            out.println("");
            out.println("    GLOBAL _minijava_main_1");
            out.println("");
            out.println("    SECTION .data");
            // outputting vtables
            for ( Frag a = f; a != null; a = a.next )
                if ( a instanceof VtableFrag )
                {
                    VtableFrag v = (VtableFrag) a;
                    
                    out.println(v.name+":");
                    for ( String ss : v.vtable )
                        out.println("    dd " + ss);
                }
            
            out.println("");
            out.println("SECTION .text");
            out.println("");
            for ( Frag a = f; a != null; a = a.next )
                if ( a instanceof ProcFrag )
                {
                    ProcFrag p = (ProcFrag) a;
            
                    // the IR is canonicalized.
                    TraceSchedule ts = new TraceSchedule(new BasicBlocks(Canon.linearize(p.body)));
                    
                    
                    // Instruction Selection is done                   
                    List<Instr> instrs = p.frame.codegen(ts.stms);
                    
                    instrs = p.frame.procEntryExit2(instrs);
                                                            
                    // allocating the registers
                    RegAlloc r = new RegAlloc(p.frame, instrs);
                    
                    // outputting the generated code.
                    Proc finalProc = p.frame.procEntryExit3(r.instrs);
                    finalProc.print(out, r);
                }
		}
		catch(Throwable e)
		{
			System.err.println(e.getMessage());
            e.printStackTrace();
		}
		
		System.exit(0);
	}

}
