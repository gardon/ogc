package diesel;



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
        compute DFA();

    }

    public void computeDefs() {

        for (List<Node> nodes = graph.nodes(), nodes != null; nodes = nodes.tail) {
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
            for (List<Temp> aux = graph.def(nodes.head); aux != null; aux = aux;tail) {
                k = (HashSet<Node>)(defs.get(aux.head)).clone();
                k.remove(nodes.head);                
            }
            if (graph.def(nodes.head) != null)
                g.add(nodes.head);

            kill.put(nodes.head, k);
            gen.put(nodes.head, g);
        }
    }

    public void computeDFA() {
        Hashtable<Node, HashSet<Node>> inl = null;
        Hashtable<Node, HashSet<Node>> oldl = null;

        inl = new Hashtable<Node, HashSet<Node>>();
        outl = new Hashtable<Node, HashSet<Node>>();
        in = new Hashtable<Node, HashSet<Node>>();
        out = new Hashtable<Node, HashSet<Node>>();

        for (List<Node> nodes = graph.nodes(); nodes != null; nodes = nodes.tail) {
            in.put(nodes.head, new HashSet<Node>());
            out.put(nodes.head, new HashSet<Node>());
            inl.put(nodes.head, new HashSet<Node>());
            outl.put(nodes.head, new HashSet<Node>());
        }
        do {
            for (List<Node> nodes = graph.nodes(); nodes != null; nodes = nodes.tail) {
            inl.remove(nodes.head);
            inl.put(nodes.head, (HashSet<Node>)in.get(nodes.head).clone());
            outl.remove(nodes.head);
            outl.put(nodes.head, (HashSet<Node>)out.get(nodes.head).clone());

            HashSet<Node> i = new HashSet<Node>();

            for (List<Node> s = nodes.head.pred(); s != null; s = s.tail)
                i.addAll((HashSet<Node>)out.get(s.head).clone());

            in.remove(nodes.head);
            in.put(nodes.head, (HashSet<Node>)i.clone());

            HashSet<Node> o = new HashSet<Node>();
            o.addAll((HashSet<Node>)i.clone());
            o.removeAll((Collection<Node>)kill.get(nodes.head));
            o.addAll(gen.get(nodes.head));
            out.remove(nodes.head);
            out.put(nodes.head, (HashSet<Node>)o.clone());
            }
        } while ((!inl.equals(in)) || (!outl.equals(out)));
    }
}
