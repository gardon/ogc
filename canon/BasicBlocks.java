package canon;

import util.List;

public class BasicBlocks
{
    public List<List<tree.Stm>> blocks;
    public temp.Label done;

    private List<List<tree.Stm>> lastBlock;
    private List<tree.Stm> lastStm;

    private void addStm(tree.Stm s)
    {
        lastStm = lastStm.tail = new List<tree.Stm>(s,null);
    }

    private void doStms(List<tree.Stm> l)
    {
        if (l==null) 
            doStms(new List<tree.Stm>(new tree.JUMP(done), null));
        else if (l.head instanceof tree.JUMP 
                || l.head instanceof tree.CJUMP)
        {
            addStm(l.head);
            mkBlocks(l.tail);
        } 
        else if (l.head instanceof tree.LABEL)
            doStms(new List<tree.Stm>(new tree.JUMP(((tree.LABEL)l.head).label), l));
        else
        {
            addStm(l.head);
            doStms(l.tail);
        }
    }

    void mkBlocks(List<tree.Stm> l)
    {
        if (l==null) 
            return;
        else if (l.head instanceof tree.LABEL)
        {
            lastStm = new List<tree.Stm>(l.head,null);
            
            if (lastBlock==null)
                lastBlock= blocks= new List<List<tree.Stm>>(lastStm,null);
            else
                lastBlock = lastBlock.tail = new List<List<tree.Stm>>(lastStm,null);
            doStms(l.tail);
        }
        else
            mkBlocks(new List<tree.Stm>(new tree.LABEL(new temp.Label()), l));
    }
   
    public BasicBlocks(List<tree.Stm> stms)
    {
        done = new temp.Label();
        mkBlocks(stms);
    }
}
