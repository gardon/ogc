package reg_alloc;

public class MoveList
{
    public graph.Node src;
    public graph.Node dst;
    
    public MoveList tail;
    
    public MoveList(graph.Node s, graph.Node d, MoveList t)
    {
        src=s;
        dst=d;
        tail=t;
    }
}

