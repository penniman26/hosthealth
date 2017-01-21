package hosthealth.speechlet.exceptions;

public class ReadingFromDatabaseException extends Exception
{
    public ReadingFromDatabaseException(Exception e)
    {
        super(e);
    }
}
