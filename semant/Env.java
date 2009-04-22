package semant;

import symbol.ClassInfo;
import symbol.Table;
import errors.ErrorEchoer;

public class Env
{
	public ErrorEchoer err;
	public Table<ClassInfo> classes; 
	
	public Env(ErrorEchoer e)
	{
		super();
		
		err = e;
		classes = new Table<ClassInfo>();
	}
}
