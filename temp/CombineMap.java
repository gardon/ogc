package temp;

public class CombineMap implements TempMap
{
    TempMap map1;
    TempMap map2;
    
    public CombineMap(TempMap m1, TempMap m2)
    {
        super();
        
        map1 = m1;
        map2 = m2;
    }

    public String tempMap(Temp t)
    {
        String r = map1.tempMap(t);
        
        if ( r != null )
            return r;
        
        return map2.tempMap(t);
    }

}
