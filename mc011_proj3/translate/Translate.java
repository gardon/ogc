package translate;

import frame.Frame;
import semant.Env;
import syntaxtree.Program;

public class Translate
{    
    private Translate()
    {
        super();       
    }

    public static Frag translate(Frame parentFrame, Env e, Program p)
    {
        // primeiramente, geramos as vtables. Afinal, elas 'precisam' estar
        // prontas no momento da traducao do programa (na verdade, precisamos
        // apenas dos "Label's" delas).
        Frag f = VTableBuilder.build(e, p);
        Frag tail = null;
        
        // Criando os frames
        FrameBuilder.translate(parentFrame, e, p);
        
        if ( f != null )
            for ( tail = f; tail.next != null; tail = tail.next )
                ;
        
        // agora, com as vtable e os frames, jah podemos 'traduzir' a AST
        // para a IR
        if ( f != null )
            tail.next = IRBuilder.build(e, p, parentFrame);
        else
            f = IRBuilder.build(e, p, parentFrame);
        
        return f;
    }
}
