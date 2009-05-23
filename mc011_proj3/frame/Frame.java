package frame;

import assem.Instr;
import temp.Label;
import temp.Temp;
import temp.TempMap;
import tree.Exp;
import tree.Stm;
import util.List;

public abstract class Frame implements TempMap
{
    public Frame()
    {
    }

    public abstract Frame newFrame(Label name, List<Boolean> formals);
    
    public Label name;
    
    public List<Access> formals;
    
    public abstract Access allocLocal(boolean escapes);
    
    public abstract int wordsize();
    
    public abstract Temp FP();
    
    public abstract Exp externalCall(String s, List<Exp> args);
    
    public abstract Temp RV();
    
    public abstract Stm procEntryExit1(tree.Exp body);
    
    public abstract List<Instr> procEntryExit2(List<Instr> body);
    
    public abstract Proc procEntryExit3(List<Instr> body);
    
    public abstract List<Instr> codegen(List<Stm> body);
    
    public abstract List<Temp> registers();
}
