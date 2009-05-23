package syntaxtree;

import util.List;

public abstract class ClassDecl extends Absyn
{
    public Identifier name;
    public List<VarDecl> varList;
    public List<MethodDecl> methodList;

	public ClassDecl(int l, int r, Identifier n, List<VarDecl> v, List<MethodDecl> m)
	{
		super(l, r);
        
        name = n;
        varList = v;
        methodList = m;
	}
}
