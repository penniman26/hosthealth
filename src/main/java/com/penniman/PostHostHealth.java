package com.penniman;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostHostHealth {
    private static final Logger log = LoggerFactory.getLogger(PostHostHealth.class);

    private DynamoDB dynamoDB;
    
    public PostHostHealth()
    {
        dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new EnvironmentVariableCredentialsProvider()));
    }
    
    public Map<String, Object> myHandler(Map<String,Object> request, Context context) {
        Map<String, Object> response = new HashMap<>();
        
        ObjectMapper mapper = new ObjectMapper();
        String body = (String)request.get("body");
        
        Map<String,Object> input;
        try
        {
            input = mapper.readValue(body, new TypeReference<Map<String, Object>>(){});
            System.out.println(context);
            System.out.println(input);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        String userId = (String)input.get("UserId");
        String diskSpaceUsed = (String)input.get("diskSpaceUsed");
        String diskSpaceTotal = (String)input.get("diskSpaceTotal");
        String loadPast15Min = (String)input.get("loadPast15Min");

        try
        {
            Validate.notBlank(userId);
            Validate.notBlank(diskSpaceUsed);
            Validate.notBlank(diskSpaceTotal);
            Validate.notBlank(loadPast15Min);
        }
        catch (NullPointerException ex)
        {
            ex.printStackTrace();

            response.put("statusCode", 400);
            response.put("body", "UserId, diskSpaceUsed, diskSpaceTotal, and loadPast15Min must not be null!");
            return response;
        }

        try
        {
            Table table = dynamoDB.getTable("HostHealth");
            
            Item item = new Item().withString("UserId", userId.toLowerCase()).withString("UsedSpace", diskSpaceUsed)
                .withString("ExistingSpace", diskSpaceTotal).withString("LoadPast15Min", loadPast15Min).withLong("TimeOfLastUpdateInMillis", System.currentTimeMillis());
            
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
