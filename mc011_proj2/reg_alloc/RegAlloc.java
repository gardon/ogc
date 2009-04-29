package reg_alloc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

import canon.Canon;

import assem.Instr;
import flow_graph.AssemFlowGraph;
import frame.Frame;
import graph.Node;
import temp.CombineMap;
import temp.DefaultMap;
import temp.Temp;
import temp.TempMap;
import util.List;

public class RegAlloc implements TempMap
{
    public List<Instr> instrs;
    
    private Frame frame;
    
    private int iter = 0;
    
    private HashSet<Node> precolored = new HashSet<Node>();
    private HashSet<Node> initial = new HashSet<Node>();
    private HashSet<Node> simplifyWorklist = new HashSet<Node>();
    private HashSet<Node> freezeWorklist = new HashSet<Node>();
    private HashSet<Node> spillWorklist = new HashSet<Node>();
    private HashSet<Node> spilledNodes = new HashSet<Node>();
    private HashSet<Node> coalescedNodes = new HashSet<Node>();
    private HashSet<Node> coloredNodes = new HashSet<Node>();
    private Stack<Node> selectStack = new Stack<Node>();
    
    private HashSet<Node> coalescedMoves = new HashSet<Node>();
    private HashSet<Node> constrainedMoves = new HashSet<Node>();
    private HashSet<Node> frozenMoves = new HashSet<Node>();
    private HashSet<Node> worklistMoves = new HashSet<Node>();
    private HashSet<Node> activeMoves = new HashSet<Node>();
    
    private Hashtable<Node, Integer> spillCost = new Hashtable<Node, Integer>();
    
    private HashSet<Edge> adjSet = new HashSet<Edge>();
    
    private Hashtable<Node, HashSet<Node>> adjList = new Hashtable<Node, HashSet<Node>>();    
    private HashSet<Node> adjList(Node n)
    {
        HashSet<Node> ret = adjList.get(n);
        
        if ( ret == null )
            adjList.put(n, ret = new HashSet<Node>());
        
        return ret;
    }
    
    private Hashtable<Node, Integer> degree = new Hashtable<Node, Integer>();
    private Integer degree(Node n)
    {       
        return degree.get(n);
    }
    
    private Hashtable<Node, HashSet<Node>> moveList = new Hashtable<Node, HashSet<Node>>();
    private HashSet<Node> moveList(Node n)
    {
        if ( n == null )
            throw new NullPointerException();
        
        HashSet<Node> ret = moveList.get(n);
        
        if ( ret == null )
            moveList.put(n, ret = new HashSet<Node>());
        
        return ret;
    }
    
    private Hashtable<Node, Node> alias = new Hashtable<Node, Node>();
    private Node alias(Node n)
    {
        return alias.get(n);
    }
    
    private Hashtable<Node, Node> color = new Hashtable<Node, Node>();
    private Node color(Node n)
    {
        return color.get(n);
    }
    
    private HashSet<Temp> spillGenerated = new HashSet<Temp>();
    
    private AssemFlowGraph cfg;
    private Liveness dfa;
    
    public RegAlloc(Frame f, List<Instr> i)
    {
        frame = f;
        instrs = i;
        
        regAlloc();
    }
    
    private void regAlloc()
    {
        boolean success;
               
        do
        {
            iter++;
            
            success = true;
            
            LivenessAnalysis();
            Init();
            Build();
            MakeWorklist();
            
            do
            {
                if ( simplifyWorklist.size() != 0 )
                    Simplify();
                else if (worklistMoves.size() != 0 )
                    Coalesce();
                else if (freezeWorklist.size() != 0)
                    Freeze();
                else if (spillWorklist.size() != 0)
                    SelectSpill();
            } while ( simplifyWorklist.size() != 0 || 
                    worklistMoves.size() != 0 ||
                    freezeWorklist.size() != 0 ||
                    spillWorklist.size() != 0);
            
            AssignColors();
            
            if (spilledNodes.size() != 0)
            {
                RewriteProgram();
                success = false;
            }
        } while (!success);
        
        FinalStep();
    }
    
    private void LivenessAnalysis()
    {
        cfg = new AssemFlowGraph(instrs);
        dfa = new Liveness(cfg);        
    }
    
    private void Init()
    {
        this.activeMoves.clear();
        this.adjList.clear();
        this.adjSet.clear();
        this.alias.clear();
        this.coalescedMoves.clear();
        this.coalescedNodes.clear();
        this.color.clear();
        this.coloredNodes.clear();
        this.constrainedMoves.clear();
        this.degree.clear();
        this.freezeWorklist.clear();
        this.frozenMoves.clear();
        this.initial.clear();
        this.moveList.clear();
        this.precolored.clear();
        this.selectStack.clear();
        this.simplifyWorklist.clear();
        this.spillCost.clear();
        this.spilledNodes.clear();
        this.spillWorklist.clear();
        
        System.gc();
        
        // criando a lista de registradores
        for ( List<Temp> regs = frame.registers(); regs != null; regs = regs.tail )
        {
            Temp t = regs.head;
            
            Node reg = dfa.tnode(t);
            
            precolored.add(reg);
            
            spillCost.put(reg, Integer.MAX_VALUE);
            
            color.put(reg, reg);
            
            degree.put(reg, 0);
        }
        
        // cria a lista de temporarios nao pre-coloridos.
        for ( List<Node> temps = dfa.nodes(); temps != null; temps = temps.tail )
        {
            Node temp = temps.head;
            
            if ( !precolored.contains(temp) )
            {
                initial.add(temp);
                
                // definindo custo de spill,
                // desta forma, nunca sera escolhido um temporario
                // criado devido a um spill.
                if ( spillGenerated.contains(dfa.gtemp(temp)) )
                    spillCost.put(temp, Integer.MAX_VALUE);
                else if ( !precolored.contains(temp) )
                    spillCost.put(temp, 1);
                
                degree.put(temp, 0);
            }
        }
    }
    
    private void Build()
    {
        Temp FP = frame.FP();
        
        for ( List<Node> progNodes = cfg.nodes(); progNodes != null; progNodes = progNodes.tail )
        {
            Node I = progNodes.head;
            
            HashSet<Temp> live = (HashSet<Temp>) dfa.Out(I).clone();
            
            if ( cfg.isMove(I) /*&& cfg.use(I).head != FP*/ )
            {
                for ( List<Temp> uses = cfg.use(I); uses != null; uses = uses.tail )
                    live.remove(uses.head);
                
                for ( List<Temp> uses = cfg.use(I); uses != null; uses = uses.tail )
                    moveList(dfa.tnode(uses.head)).add(I);
                
                for ( List<Temp> defs = cfg.def(I); defs != null; defs = defs.tail )
                    moveList(dfa.tnode(defs.head)).add(I);
                
                worklistMoves.add(I);
            }
            
            for( List<Temp> defs = cfg.def(I); defs != null; defs = defs.tail )
                live.add(defs.head);
            
            for ( List<Temp> defs = cfg.def(I); defs != null; defs = defs.tail )
                for ( Temp l : live )
                {
                    Temp d = defs.head;
                    
                    AddEdge(l, d);
                }
        }
    }
    
    private void MakeWorklist()
    {
        int K = precolored.size();
        
        for ( Iterator<Node> it = initial.iterator(); it.hasNext(); )
        {
            Node n = it.next();
            it.remove();

            if ( degree(n) >= K )
                spillWorklist.add(n);
            else if ( MoveRelated(n) )
                freezeWorklist.add(n);
            else 
                simplifyWorklist.add(n);
        }
    }
    
    private void Simplify()
    {
        Iterator<Node> it = simplifyWorklist.iterator();
        
        Node n = it.next();
        it.remove();
        
        selectStack.push(n);
        
        for ( Node m : Adjacent(n) )
            DecrementDegree(m);
    }
    
    private void Coalesce()
    {
        Iterator<Node> it = worklistMoves.iterator();
        Node m = null;
        for ( ; it.hasNext(); )
        {
            m = it.next();
            
            //Node x = GetAlias(dfa.tnode(cfg.instr(m).def().head));
            //Node y = GetAlias(dfa.tnode(cfg.instr(m).use().head));
            
            //if ( x != dfa.tnode(frame.FP()) && y != dfa.tnode(frame.FP()) )
            //{
                break;
            //}
        }
        
        it.remove();
        
        Node x = GetAlias(dfa.tnode(cfg.instr(m).def().head));
        Node y = GetAlias(dfa.tnode(cfg.instr(m).use().head));
        
        Node u;
        Node v;
        
        if ( precolored.contains(y) )
        {
            u = y;
            v = x;
        }
        else
        {
            u = x;
            v = y;
        }
        
        Edge e = Edge.getEdge(u, v);
        worklistMoves.remove(m);
                
        if ( u == v )
        {
            coalescedMoves.add(m);
            AddWorklist(u);
        }
        else if ( precolored.contains(v) || adjSet.contains(e) )
        {
            constrainedMoves.add(m);
            AddWorklist(u);
            AddWorklist(v);
        }
        else if ( CoalesceCond1(u, v) || CoalesceCond2(u, v) )
        {
            coalescedMoves.add(m);
            Combine(u, v);
            AddWorklist(u);
        }
        else
            activeMoves.add(m);
    }
    
    private void Freeze()
    {
        Iterator<Node> it = freezeWorklist.iterator();
        
        Node u = it.next();
        it.remove();
        
        freezeWorklist.remove(u);
        simplifyWorklist.add(u);
        FreezeMoves(u);
    }
    
    private void SelectSpill()
    {
        Node n = spillWorklist.iterator().next();
        int v = spillCost.get(n);
        
        for ( Node a : spillWorklist )
        {
            if (spillCost.get(a) < v )
                n = a;
        }
        
        spillWorklist.remove(n);
        simplifyWorklist.add(n);
        FreezeMoves(n);
    }
    
    private void AssignColors()
    {
        while ( !selectStack.isEmpty() )
        {
            Node n = selectStack.pop();
            HashSet<Node> okColors = (HashSet<Node>) precolored.clone();
            
            if ( precolored.contains(GetAlias(n)))
                continue;
            
            okColors.remove(dfa.tnode(frame.FP()));
            for ( Node w : adjList(n) )
            {
                HashSet<Node> used = (HashSet<Node>) precolored.clone();
                used.addAll(coloredNodes);
                
                if (used.contains(GetAlias(w)))
                    okColors.remove(color(GetAlias(w)));
            }
            
            if ( okColors.isEmpty() )
            {
                spilledNodes.add(n);
            }
            else
            {
                coloredNodes.add(n);
                Iterator<Node> it = okColors.iterator();
                Node c = it.next();

                color.put(n, c);
            }
        }
        
        for ( Node n : coalescedNodes )
        {
            Node alias = GetAlias(n);
            
            Node a = color(alias);
            
            if ( a != null)
                color.put(n, a);
        }
    }
    
    private void RewriteProgram()
    {
        Hashtable<Temp, frame.Access> accessTable = new Hashtable<Temp, frame.Access>();
        
        // gerando 'spills'
        for ( Node v : spilledNodes )
            accessTable.put(dfa.gtemp(v), frame.allocLocal(true));
        
        List<Instr> oldInstrs = instrs;
        List<Instr> tail = instrs = null;
        
        for ( ; oldInstrs != null; oldInstrs = oldInstrs.tail )
        {
            // atualizando os usos da instrucao (precisam de FETCH)
            for ( List<Temp> uses = oldInstrs.head.use(); uses != null; uses = uses.tail )
            {
                Temp t = uses.head;
                frame.Access a = accessTable.get(t);
                
                // spill
                if ( a != null )
                {
                    RegAlloc.MemHeadTailTemp result = genFetch(a, uses.head);
                    
                    if ( instrs == null )
                        instrs = result.head;
                    else
                        tail.tail = result.head;
                    
                    tail = result.tail;
                }
            }
            
            // colocando a instrucao
            List<assem.Instr> nn = new List<assem.Instr>(oldInstrs.head, null);
            
            if ( instrs == null )
                instrs = tail = nn;
            else
                tail = tail.tail = nn;            
            
            // atualizando as definicoes da instrucao (precisam de STORE)
            for ( List<Temp> defs = oldInstrs.head.def(); defs != null; defs = defs.tail )
            {
                Temp t = defs.head;
                frame.Access a = accessTable.get(t);
                
                // spill
                if ( a != null )
                {
                    RegAlloc.MemHeadTailTemp result = genStore(a, defs.head);
                    
                    if ( instrs == null )
                        instrs = result.head;
                    else
                        tail.tail = result.head;
                    
                    tail = result.tail;
                    
                    defs.head = result.temp;
                }
            }
        }
    }
    
    private void AddEdge(Node u, Node v)
    {
        Edge e = Edge.getEdge(u, v);
        Edge e_prime = Edge.getEdge(v, u);
        
        if ( e != e_prime )
            throw new Error();
        
        if ( !adjSet.contains(e) )
        {
            adjSet.add(e);
            
            if ( !precolored.contains(u) )
            {
                adjList(u).add(v);
                degree.put(u, degree(u)+1);
            }
            
            if ( !precolored.contains(v) )
            {
                adjList(v).add(u);
                degree.put(v, degree(v)+1);
            }
        }        
    }
    
    private void AddEdge(Temp uu, Temp vv)
    {
        if ( uu == vv )
            return;
        
        Node u = dfa.tnode(uu);
        Node v = dfa.tnode(vv);
        
        AddEdge(u, v);
    }
    
    private boolean MoveRelated(Node n)
    {
        return NodeMoves(n).size() != 0;
    }

    private HashSet<Node> NodeMoves(Node n)
    {
        HashSet<Node> aux = (HashSet<Node>) activeMoves.clone();
        aux.addAll(worklistMoves);
        
        HashSet<Node> ret = (HashSet<Node>) moveList(n).clone();
        
        ret.retainAll(aux);
        
        return ret;
    }
    
    private HashSet<Node> Adjacent(Node n)
    {
        HashSet<Node> aux = new HashSet<Node>();
        
        aux.addAll(selectStack);
        aux.addAll(coalescedNodes);
        
        HashSet<Node> ret = new HashSet<Node>();
        
        ret.addAll(adjList(n));
        ret.removeAll(aux);
        
        return ret;
    }
    
    private void DecrementDegree(Node n)
    {
        if ( precolored.contains(n) )
            return;
               
        int d = degree(n);
        
        degree.put(n, d-1);
        
        if ( d == precolored.size() )
        {
            HashSet<Node> adjs = Adjacent(n);
            adjs.add(n);
            
            for( Node a : adjs )
                EnableMoves(a);
            
            spillWorklist.remove(n);
            
            if ( MoveRelated(n) )
                freezeWorklist.add(n);
            else
                simplifyWorklist.add(n);
        }
    }
    
    private Node GetAlias(Node n)
    {
        if ( !coalescedNodes.contains(n) )
            return n;
        
        return GetAlias(alias(n));
    }
    
    private void AddWorklist(Node n)
    {
        if ( !precolored.contains(n) && !MoveRelated(n) && degree(n) < precolored.size() )
        {
            freezeWorklist.remove(n);
            simplifyWorklist.add(n);
        }
    }
    
    private boolean CoalesceCond1(Node u, Node v)
    {
        if ( !precolored.contains(u) )
            return false;
        
        for ( Node t : Adjacent(v) )
            if ( !OK(t,u) )
                return false;
        
        return true;
    }
    
    private boolean CoalesceCond2(Node u, Node v)
    {
        if ( precolored.contains(u) )
            return false;
        
        HashSet<Node> adj = Adjacent(u);
        adj.addAll(Adjacent(v));
        
        return Conservative(adj);
    }
    
    private boolean OK(Node t, Node r)
    {
        int K = precolored.size();
        
        return precolored.contains(t) || degree(t) < K || adjSet.contains(Edge.getEdge(t,r));
    }
    
    private boolean Conservative(HashSet<Node> nodes)
    {
        int k = 0;
        int K = precolored.size();
        for( Node n : nodes )
            if ( degree(n) >= K )
                k = k + 1;
        
        return k < K;
    }
    
    private void Combine(Node u, Node v)
    {
        if ( freezeWorklist.contains(v) )
            freezeWorklist.remove(v);
        else
            spillWorklist.remove(v);
        
        coalescedNodes.add(v);
        
        alias.put(v, u);
        
        moveList(u).addAll(moveList(v));
        
        EnableMoves(v);
        
        for ( Node t : Adjacent(v))
        {
            AddEdge(t, u);
            DecrementDegree(t);
        }
        
        if ( degree(u) >= precolored.size() && freezeWorklist.contains(u) )
        {
            freezeWorklist.remove(u);
            spillWorklist.add(u);
        }
    }
    
    private void EnableMoves(Node v)
    {
        for ( Node n : NodeMoves(v) )
            if ( activeMoves.contains(n) )
            {
                activeMoves.remove(n);
                worklistMoves.add(n);
            }
    }
    
    private void FreezeMoves(Node u)
    {
        int K = precolored.size();
        
        for ( Node m : NodeMoves(u) )
        {
            Node x = dfa.tnode(cfg.def(m).head);
            Node y = dfa.tnode(cfg.use(m).head);
            Node v;
            
            if ( GetAlias(u) == GetAlias(y) )
                v = GetAlias(x);
            else
                v = GetAlias(y);
            
            activeMoves.remove(m);
            frozenMoves.add(m);
            
            if ( NodeMoves(v).size() == 0 && degree(v) < K )
            {
                freezeWorklist.remove(v);
                simplifyWorklist.add(v);
            }
        }
    }
    
    private static class MemHeadTailTemp
    {
        public List<Instr> head;
        public List<Instr> tail;
        public Temp temp;
        
        public MemHeadTailTemp(List<Instr> h, Temp t)
        {
            head = h;
            temp = t;
            tail = tail();
        }
        
        private List<Instr> tail()
        {
            List<Instr> aux;
            
            for ( aux = head; aux != null; aux = aux.tail )
                if ( aux.tail == null )
                    break;
            
            return aux;
        }
    }
    
    private RegAlloc.MemHeadTailTemp genFetch(frame.Access a, Temp t)
    {
        spillGenerated.add(t);
        
        tree.Stm f = new tree.MOVE(new tree.TEMP(t),
                a.exp(new tree.TEMP(frame.FP())));
        
        return new RegAlloc.MemHeadTailTemp(frame.codegen(Canon.linearize(f)), t);
    }
    
    private RegAlloc.MemHeadTailTemp genStore(frame.Access a, Temp t)
    {
        spillGenerated.add(t);
        
        tree.Stm s = new tree.MOVE(a.exp(new tree.TEMP(frame.FP())),
                new tree.TEMP(t));
        
        return new RegAlloc.MemHeadTailTemp(frame.codegen(Canon.linearize(s)), t);
    }
    
    private void FinalStep()
    {
        List<Instr> aux = instrs, tail = null;
        
        for ( instrs = null; aux != null; aux = aux.tail )
        {
            Instr i = aux.head;
            
            if ( i instanceof assem.MOVE )
            {
                Temp d = i.def().head;
                Temp s = i.use().head;
                
                if ( GetAlias(dfa.tnode(d)) == GetAlias(dfa.tnode(s)) )
                    continue;
            }
            
            if ( instrs == null )
                instrs = tail = new List<Instr>(aux.head, null);
            else
                tail = tail.tail = new List<Instr>(aux.head, null);
        }
    }
    
    public String tempMap(Temp t)
    {
        String ret;
        
        ret = frame.tempMap(t);

        if ( ret == null )
            ret = frame.tempMap(dfa.gtemp(color.get(dfa.tnode(t))));
                
        if ( ret == null )
            throw new Error("" + dfa.gtemp(color.get(dfa.tnode(t))));
        
        return ret;
    }
}
