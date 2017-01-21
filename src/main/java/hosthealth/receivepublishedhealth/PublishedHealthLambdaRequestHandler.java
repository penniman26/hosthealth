package hosthealth.receivepublishedhealth;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Entrance class for lambda from aws when we receive POST calls over HTTPS from the hosts publishing their health.
 */
public class PublishedHealthLambdaRequestHandler
{
    private DynamoDB dynamoDB;
    
    public PublishedHealthLambdaRequestHandler()
    {
        dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new EnvironmentVariableCredentialsProvider()));
    }
    
    public Map<String, Object> myHandler(Map<String,Object> request, Context context) {

        context.getLogger().log("request:\n" + request.toString());

        Map<String, Object> response = handle(request, context);

        return response;
    }

    private Map<String, Object> handle(Map<String,Object> request, Context context)
    {
        Map<String, Object> response = new HashMap<>();

        String hostId = (String)request.get("HostId");
        String diskSpaceUsed = (String)request.get("diskSpaceUsed");
        String diskSpaceTotal = (String)request.get("diskSpaceTotal");
        String loadPast1Min = (String)request.get("loadPast1Min");
        String friendlyName = (String)request.get("friendlyName");
        String version = (String)request.get("version");

        try
        {
            Validate.notBlank(hostId);
            Validate.notBlank(diskSpaceUsed);
            Validate.notBlank(diskSpaceTotal);
            Validate.notBlank(loadPast1Min);
            Validate.notBlank(friendlyName);
        }
        catch (NullPointerException ex)
        {
            ex.printStackTrace();

            response.put("statusCode", 400);
            response.put("body", "HostId, diskSpaceUsed, diskSpaceTotal, loadPast1Min, and friendlyName must not be null!");
            return response;
        }

        try
        {
            Table table = dynamoDB.getTable("HostHealth");

            Item item = new Item().withString("HostId", hostId.toLowerCase().replaceAll(":", "").replaceAll("\\s", ""))
                    .withString("UsedSpace", diskSpaceUsed).withString("ExistingSpace", diskSpaceTotal).withString("LoadPast1Min", loadPast1Min)
                    .withLong("TimeOfLastUpdateInMillis", System.currentTimeMillis()).withString("FriendlyName", friendlyName);

            if (!StringUtils.isBlank(version))
            {
                item.withString("version", version);
            }

            table.putItem(item);
        }
        catch (Error e)
        {
            e.printStackTrace();

            response.put("statusCode", 500);
            response.put("body", "Error saving to DB");
            return response;
        }

        response.put("statusCode", 200);
        response.put("body", "Success");

        return response;
    }
}
