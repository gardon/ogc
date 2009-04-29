package reg_alloc;

import java.util.HashSet;

import graph.Node;
import graph.Graph;

abstract public class InterferenceGraph extends Graph
{
    abstract public Node tnode(temp.Temp temp);
    
    abstract public temp.Temp gtemp(Node node);
    
    abstract public MoveList moves();
    
    public int spillCost(Node node)
    {
        return 1;
    }
}
