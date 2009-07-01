package diesel;

import java.util.HashSet;
import java.util.Iterator;

import temp.Temp;
import util.List;
import assem.Instr;
import flow_graph.AssemFlowGraph;
import graph.Node;

public class ConstantPropagation {
    private AssemFlowGraph graph;
    public List<Instr> finalInstrs = null;

    public List<Instr> getInstr (){
	if (this.finalInstrs != null)
	    return this.finalInstrs;
	else
	    return null;
    }

    public ConstantPropagation(List<Instr> instrs){
	graph = new AssemFlowGraph(instrs);
	int i, indice;
	String cte;
	Node no;
	List<Instr> aux2 = null;

	/* chama o reaching definitions */
	ReachingDefinitions rc = new ReachingDefinitions(graph);

	/* Constant Propagation */
	for(List<Node> nodes = graph.nodes(); nodes != null; nodes = nodes.tail){
	    i = 0;
	    for(List<Temp> aux = graph.use(nodes.head); aux != null; aux = aux.tail){
		/* pega todos os nos (defs) em que um Temp-"use" eh definido */
		HashSet<Node> defs = rc.defs.get(aux.head);
		if (defs != null){
		    Iterator<Node> defsIterator = defs.iterator();
		    /* pega todos os nos (in) vivos na entrada do no atual */
		    HashSet<Node> in = rc.in.get(nodes.head);

		    cte = "";
		    indice = -1;

		    /* compara os nos de "defs" nos com os nos de "in" do no atual  */
		    while(defsIterator.hasNext()){
			no = defsIterator.next();

			if(in.contains(no) && (((graph.instr(no).assem.split(" "))[0]).equals("mov")) && (graph.use(no) == null) ){
			    if (indice > -1){
				indice = -1;
				break;
			    }

			    indice = i;

			    /* parse do assem para recuperar CTE */
			    String assem = graph.instr(no).assem;
			    /* assembly do tipo: MOV _t1,cte */
			    cte = (assem.split(","))[1];

			}
		    }

		    if (indice > -1){
			/* padrao do source a ser substituido */
			String padrao = " `s"+indice;

			/* subsitui */
			graph.instr(nodes.head).assem = graph.instr(nodes.head).assem.replace(padrao, "DWORD"+cte);
		    }
		}
		/* indice do aux dentro da lista de use do no atual*/
		i++;
	    } 

	    /* atualiza finalInstrs */
	    if(finalInstrs == null){
		finalInstrs = new List<Instr> (graph.instr(nodes.head), null);
	    	aux2=finalInstrs;
	    }else{
	    	aux2.tail= new List<Instr> (graph.instr(nodes.head),null);
	    	aux2=aux2.tail;
	    }

	}

    }

}
