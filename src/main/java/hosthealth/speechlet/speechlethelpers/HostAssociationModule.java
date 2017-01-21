package hosthealth.speechlet.speechlethelpers;

import com.amazon.speech.slu.Slot;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.util.StringUtils;
import hosthealth.speechlet.exceptions.ReadingFromDatabaseException;
import hosthealth.speechlet.exceptions.WritingToDatabaseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logic for linking an alexa user and mac addresses (or other alphanumeric IDs of length 12).
 */
public class HostAssociationModule
{
    private final DynamoDB dynamoDB;

    public HostAssociationModule(DynamoDB dynamoDB)
    {
        this.dynamoDB = dynamoDB;
    }

    public List<String> getHostIdsFromAlexaId(String alexaId) throws ReadingFromDatabaseException
    {
        try
        {
            Table table = dynamoDB.getTable("HostIdsFromAlexaId");
            ItemCollection<QueryOutcome> itemCollection = table.query("AlexaId", alexaId);

            List<String> hostIds = new ArrayList<>();
            IteratorSupport<Item, QueryOutcome> iteratorSupport = itemCollection.iterator();
            while (iteratorSupport.hasNext())
            {
                hostIds.add((String) iteratorSupport.next().get("HostId"));
            }

            return hostIds;
        }
        catch (Exception e)
        {
            throw new ReadingFromDatabaseException(e);
        }
    }

    public void removeHostAssociation(String alexaId, String hostId) throws WritingToDatabaseException
    {
        try
        {
            Table table = dynamoDB.getTable("HostIdsFromAlexaId");
            table.deleteItem("AlexaId", alexaId, "HostId", hostId);
        }
        catch (Exception e)
        {
            throw new WritingToDatabaseException(e);
        }
    }

    public boolean isValidSlotsForAddingHostIdToAlexaAccount(Map<String,Slot> slots)
    {
        // There are 12 slot values that could be any of the 26 characters or any of the 10 digits.
        String[] letters = new String[]{"A","B","C","D","E","F","G","H","I","J","K", "L"};
        for (int i = 0; i < 12; i++)
        {
            String letter = slots.get("LetterOrNumber" + letters[i]).getValue();

            if (letter == null || letter.replaceAll("\\.","").replaceAll(",","").length() != 1)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * If the association was successful, returns mac address with spaces and commas between characters and digits. Otherwise,
     * returns Optional.empty().
     */
    public String addHostAssociation(String alexaId, Map<String,Slot> slots) throws WritingToDatabaseException
    {
        StringBuilder macAddressBuilder = new StringBuilder();

        // There are 12 slot values that could be any of the 26 characters or any of the 10 digits.
        String[] letters = new String[]{"A","B","C","D","E","F","G","H","I","J","K", "L"};
        for (int i = 0; i < 12; i++)
        {
            String letter = slots.get("LetterOrNumber" + letters[i]).getValue();

            macAddressBuilder.append(StringUtils.lowerCase(letter.replaceAll("\\.","")) + ", ");
        }

        String macAddress = macAddressBuilder.toString();
        String macAddressNoSpacesOrCommas = macAddress.replaceAll("\\s", "").replaceAll(",","");

        try
        {
            Item item = new Item().withString("AlexaId", alexaId).withString("HostId", macAddressNoSpacesOrCommas);
            Table table = dynamoDB.getTable("HostIdsFromAlexaId");
            table.putItem(item);
        }
        catch (Exception e)
        {
            throw new WritingToDatabaseException(e);
        }

        return macAddress;
    }
}
