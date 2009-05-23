package graph;

import util.List;

public class Node
{
    Graph mygraph;
    private Node()
    {
        
    }
    
    int mykey;
    public Node(Graph g)
    {
        mygraph=g; 
        mykey= g.nodecount++;
        List<Node> p = new List<Node>(this, null);
        if (g.mylast==null)
            g.mynodes=g.mylast=p;
        else
            g.mylast = g.mylast.tail = p;
    }

    List<Node> succs;
    List<Node> preds;
    
    public List<Node> succ()
    {
        return succs;
    }
    
    public List<Node> pred()
    {
        return preds;
    }
    
    List<Node> cat(List<Node> a, List<Node> b)
    {
        if (a==null)
            return b;
        else return new List<Node>(a.head, cat(a.tail,b));
    }
    
    public List<Node> adj()
    {
        return cat(succ(), pred());
    }

    int len(List<Node> l)
    {
        int i=0;
        for(List<Node> p=l; p!=null; p=p.tail)
            i++;
        return i;
    }

    public int inDegree()
    {
        return len(pred());
    }
    
    public int outDegree()
    {
        return len(succ());
    }
    
    public int degree()
    {
        return inDegree()+outDegree();
    } 

    public boolean goesTo(Node n)
    {
        return Graph.inList(n, succ());
    }

    public boolean comesFrom(Node n)
    {
        return Graph.inList(n, pred());
    }

    public boolean adj(Node n)
    {
        return goesTo(n) || comesFrom(n);
    }

    public String toString()
    {
        return String.valueOf(mykey);
    }
}
