package canon;

import util.List;

public class TraceSchedule
{
    public List<tree.Stm> stms;
  
    BasicBlocks theBlocks;
    java.util.Dictionary table = new java.util.Hashtable();

    List<tree.Stm> getLast(List<tree.Stm> block)
    {
        List<tree.Stm> l=block;
     
        while (l.tail.tail!=null) 
            l=l.tail;
     
        return l;
    }

    void trace(List<tree.Stm> l)
    {
        for(;;)
        {
            tree.LABEL lab = (tree.LABEL)l.head;
            table.remove(lab.label);
            List<tree.Stm> last = getLast(l);
            tree.Stm s = last.tail.head;
            
            if (s instanceof tree.JUMP)
            {
                tree.JUMP j = (tree.JUMP)s;
                List<tree.Stm> target = (List<tree.Stm>)table.get(j.targets.head);
                if (j.targets.tail==null && target!=null)
                {
                    last.tail=target;
                    l=target;
                }
                else
                {
                    last.tail.tail=getNext();
                    return;
                }
            }            
            else if (s instanceof tree.CJUMP)
            {
                tree.CJUMP j = (tree.CJUMP)s;
                List<tree.Stm> t = (List<tree.Stm>)table.get(j.ifTrue);
                List<tree.Stm> f = (List<tree.Stm>)table.get(j.ifFalse);
                
                if ( f != null)
                {
                    last.tail.tail=f; 
                    l=f;
                }
                else if (t!=null)
                {
                    last.tail.head=new tree.CJUMP(tree.CJUMP.notRel(j.op),
                            j.left,j.right,
                            j.ifFalse,j.ifTrue
                    );
                    last.tail.tail=t;
                    l=t;
                }
                else
                {
                    temp.Label ff = new temp.Label();
                    last.tail.head=new tree.CJUMP(j.op,j.left,j.right,
                    j.ifTrue,ff);
                    last.tail.tail=new List<tree.Stm>(new tree.LABEL(ff),
                            new List<tree.Stm>(
                                    new tree.JUMP(j.ifFalse),
                                    getNext()
                            )
                    );
                    return;
                }
            }
            else
                throw new Error("Bad basic block in TraceSchedule");
        }
    }

    List<tree.Stm> getNext()
    {
        if (theBlocks.blocks==null) 
            return new List<tree.Stm>(new tree.LABEL(theBlocks.done), null);
        else
        {
            List<tree.Stm> s = theBlocks.blocks.head;
            tree.LABEL lab = (tree.LABEL)s.head;
            if (table.get(lab.label) != null)
            {
                trace(s);
                return s;
            }
            else
            {
                theBlocks.blocks = theBlocks.blocks.tail;
                return getNext();
            }
        }
    }

    public TraceSchedule(BasicBlocks b)
    {
        theBlocks=b;
        for(List<List<tree.Stm>> l = b.blocks; l!=null; l=l.tail)
            table.put(((tree.LABEL)l.head.head).label, l.head);
        stms=getNext();
        table=null;
    }        
}
