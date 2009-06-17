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
    private List<Instr> finalInstrs = null;

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

	/* chama o reaching definitions */
	ReachingDefinitions rc = new ReachingDefinitions(graph);

	/* começa a percorrer os nós do grafo e tenta substituir as ctes */
	for(List<Node> nodes = graph.nodes(); nodes != null; nodes = nodes.tail){
	    /* para cada nó do grafo: percorre todos os uses e 
	     * verifica se ele é cadidato a substituição */
	    i = 0;
	    for(List<Temp> aux = graph.use(nodes.head); aux != null; aux = aux.tail){
		/* pega todos os nós (defs) em que um Temp-"use" é definido */
		HashSet<Node> defs = rc.defs.get(aux.head);
		if (defs != null){
		    Iterator<Node> defsIterator = defs.iterator();
		    /* pega todos os nós (in) vivos na entrada do nó atual */
		    HashSet<Node> in = rc.in.get(nodes.head);

		    cte = "";
		    indice = -1;

		    /* compara os nós de "defs" nós com os nós de "in" do nó atual  */
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
	    } /* fim for */

	    /* atualiza finalInstrs, ORDEM INVERSA!!!! */
	    finalInstrs = new List<Instr> (graph.instr(nodes.head), finalInstrs);

	}

	/* inverte a lista invertida =D */
	List<Instr> lAux = finalInstrs;
	int tam = finalInstrs.size();
	finalInstrs = null;
	for (i = 0; i < tam; i++){
	    finalInstrs = new List<Instr> (lAux.head, finalInstrs);
	    lAux = lAux.tail;
	}
    }

}
