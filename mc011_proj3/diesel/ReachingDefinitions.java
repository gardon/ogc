package diesel;
 
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
 
import temp.Temp;
import util.List;
import flow_graph.FlowGraph;
import graph.Node;
 
public class ReachingDefinitions {
 
    private FlowGraph graph;
 
    public Hashtable<Node, HashSet<Node>> in = new Hashtable<Node, HashSet<Node>>();
    public Hashtable<Node, HashSet<Node>> out = new Hashtable<Node, HashSet<Node>>();
    private Hashtable<Node, HashSet<Node>> gen;
    private Hashtable<Node, HashSet<Node>> kill;
    public Hashtable<Temp, HashSet<Node>> defs = new Hashtable<Temp, HashSet<Node>>();
 
    public ReachingDefinitions (FlowGraph gr) {
 
        super();
        graph = gr;
 
        computeDefs();
        computeGenKill();
        computeDFA();
 
    }
 
    public void computeDefs() {
 
        for (List<Node> nodes = graph.nodes(); nodes != null; nodes = nodes.tail) {
            for (List<Temp> aux = graph.def(nodes.head); aux != null; aux = aux.tail) {
                if (defs != null && defs.containsKey(aux.head))
                    defs.get(aux.head).add(nodes.head);
                else {
                    HashSet<Node> node = new HashSet<Node>();
                    node.add(nodes.head);
                    defs.put(aux.head, node);
                }
            }
        }
    }
 
    public void computeGenKill() {
        kill = new Hashtable<Node, HashSet<Node>>();
        gen = new Hashtable<Node, HashSet<Node>>();
 
        for (List<Node> nodes = graph.nodes(); nodes != null; nodes = nodes.tail) {
            HashSet<Node> k = new HashSet<Node>();
            HashSet<Node> g = new HashSet<Node>();

            // calcula conjunto kill
            for (List<Temp> aux = graph.def(nodes.head); aux != null; aux = aux.tail) {
                k = (HashSet<Node>)(defs.get(nodes.head)).clone();
                k.remove(nodes.head);
            }
            // calcula conjunto gen
            if (graph.def(nodes.head) != null)
                g.add(nodes.head);

            kill.put(nodes.head, k);
            gen.put(nodes.head, g);
        }
    }
 
    public void computeDFA() {
        Hashtable<Node, HashSet<Node>> inl = null;
        Hashtable<Node, HashSet<Node>> outl = null;
        inl = new Hashtable<Node, HashSet<Node>>();
        outl = new Hashtable<Node, HashSet<Node>>();
        in = new Hashtable<Node, HashSet<Node>>();
        out = new Hashtable<Node, HashSet<Node>>();
    	List<Node> inverseNodes;  	
    	List<Node> iter = null;

    	for ( List<Node> instrs = graph.nodes(); instrs != null; instrs = instrs.tail ) {
    		if (iter == null)
    			iter = new List<Node>(instrs.head, null);
    		else
    			iter = new List<Node>(instrs.head, iter);
        }
    	
    	inverseNodes = iter;
    	
    	// n é o nó do grafo
    	
    	// for each n
    	// in[n] <- {}
    	// out[n] <- {}
    	 	
    	for (List<Node> instrs = inverseNodes; instrs != null; instrs = instrs.tail) {
            in.put(instrs.head, new HashSet<Node>());
            out.put(instrs.head, new HashSet<Node>());            
        }
    	
    	// repeat
    	int hasChanged;
    	do {    		
    		// for each n
        	for (List<Node> instrs = inverseNodes; instrs != null; instrs = instrs.tail) {
        		Node n = instrs.head;
        		        		
        		// in'[n] <- in[n]
        		inl.put(n, in.get(n));
        		
        		// out'[n] <- out[n]in
        		outl.put(n, out.get(n));
        		
        		// in[n] <- use[n] U (out[n] - def[n])
        		HashSet<Node> tmp = new HashSet<Node>();
        		tmp.addAll(out.get(n));
        		tmp.removeAll(kill.get(n));
        		tmp.addAll(gen.get(n));        		
        		in.put(n, tmp);
        		
        		// out[n] <- U in[s] (s is for all successors of n)
        		tmp = new HashSet<Node>();
        		for (List<Node> s = n.succ(); s != null; s = s.tail) {
        			Node ns = s.head;        			
        			tmp.addAll(in.get(ns));        			
        		}

        		out.put(n, tmp);        		
            }
        	
        	hasChanged = 0;
        	for ( List<Node> instrs = inverseNodes; instrs != null; instrs = instrs.tail )
            {
        		Node n = instrs.head;
        		
        		if (in.get(n).equals(inl.get(n)) == false) {
        			hasChanged = 1;
        			break;
        		}
        		
        		if (out.get(n).equals(outl.get(n)) == false) {
        			hasChanged = 1;
        			break;
        		}
            }
    		
    	} while (hasChanged==1);
    	// until in'[n] = in[n] and out'[n] = out[n] for all n
 
    }
}
