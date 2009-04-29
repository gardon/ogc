package temp;

public class DefaultMap implements TempMap
{
    public DefaultMap()
    {
        super();
    }

    public String tempMap(Temp t)
    {
        return t.toString();
    }

}
