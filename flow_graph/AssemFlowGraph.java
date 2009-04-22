package flow_graph;

import java.util.Hashtable;

import assem.Instr;
import assem.MOVE;
import graph.Node;
import temp.Label;
import temp.Temp;
import util.List;

public class AssemFlowGraph extends FlowGraph
{
    private Hashtable<Node, Instr> map;
    private Hashtable<Instr, Node> revMap;
        
    public AssemFlowGraph(List<Instr> list)
    {
        super();
        
        map = new Hashtable<Node, Instr>();
        revMap = new Hashtable<Instr, Node>();
        
        buildGraph(list);
    }
        
    private void buildGraph(List<Instr> ilist)
    { 
        Hashtable<Label, Instr> map1 = new Hashtable<Label, Instr>();
        // construindo os nos
        for( List<Instr> a = ilist ; a != null; a = a.tail )
        {
            Node n = this.newNode();
            
            map.put(n, a.head);
            
            revMap.put(a.head, n);
            
            if ( a.head instanceof assem.LABEL )
            {
                map1.put(((assem.LABEL)a.head).label, a.head );
            }
        }
        
        for ( List<Instr> aux = ilist; aux != null; aux = aux.tail )
        {
            assem.Targets jmps = aux.head.jumps(); 
            if ( jmps == null ) // instrucao 'fall through'
            {
                if (aux.tail != null)
                    this.addEdge(revMap.get(aux.head), revMap.get(aux.tail.head));
            }
            else // branches
            {
                for ( List<Label> a = jmps.labels; a != null; a = a.tail )
                {
                    this.addEdge(revMap.get(aux.head),
                            revMap.get(map1.get(a.head)));
                }
            }
        }        
    }
    
    public Instr instr(Node node)
    {
        return map.get(node);
    }
    
    public Node node(Instr instr)
    {
        return revMap.get(instr);
    }

    public List<Temp> def(Node node)
    {
        Instr i = map.get(node);
        
        if ( i == null )
            return null;
        
        return i.def();
    }

    public List<Temp> use(Node node)
    {
        Instr i = map.get(node);
        
        if ( i == null )
            return null;
        
        return i.use();
    }

    public boolean isMove(Node node)
    {
        Instr i = map.get(node);
        
        if ( i == null )
            return false;
        
        return i instanceof MOVE;
    }
}
