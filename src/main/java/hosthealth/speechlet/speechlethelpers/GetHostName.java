package hosthealth.speechlet.speechlethelpers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import hosthealth.speechlet.exceptions.ReadingFromDatabaseException;

public class GetHostName
{
    private final DynamoDB dynamoDB;

    public GetHostName(DynamoDB dynamoDB)
    {
        this.dynamoDB = dynamoDB;
    }

    public String getFriendlyName(String hostID) throws ReadingFromDatabaseException
    {
        try
        {
            Table table = dynamoDB.getTable("HostHealth");
            Item item = table.getItem("HostId", hostID.replaceAll("\\s", "").replaceAll(",", ""));

            if (item == null)
            {
                return null;
            }

            return item.getString("FriendlyName");
        }
        catch (Exception e)
        {
            throw new ReadingFromDatabaseException(e);
        }
    }
}
