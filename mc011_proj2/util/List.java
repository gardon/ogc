package util;

public class List<E>
{
	public E head;
	public List<E> tail;
	
	public List(E h, List<E> t)
	{
        if ( h == null )
            throw new Error();
        
		head = h;
		tail = t;
	}
    
    public int size()
    {
        if ( tail == null )
            return 1;
        
        return 1 + tail.size();
    }
}
