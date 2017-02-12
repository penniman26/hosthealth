package hosthealth.speechlet.speechlethelpers;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import hosthealth.speechlet.exceptions.ReadingFromDatabaseException;
import hosthealth.speechlet.prompts.GeneralPromptRenderer;
import hosthealth.speechlet.prompts.PromptLibrary;
import hosthealth.speechlet.prompts.TimeToTextModule;

import java.math.BigDecimal;
import java.util.List;

public class GetHostHealth
{
    private final DynamoDB dynamoDB;
    private final GeneralPromptRenderer generalPromptRenderer;
    private final TimeToTextModule timeToText;

    public GetHostHealth(DynamoDB dynamoDB, GeneralPromptRenderer generalPromptRenderer, TimeToTextModule timeToTextt)
    {
        this.dynamoDB = dynamoDB;
        this.generalPromptRenderer = generalPromptRenderer;
        this.timeToText = timeToTextt;
    }

    public SpeechletResponse getHostHealthResponse(List<String> hostIds) throws ReadingFromDatabaseException
    {
        String speechText = getHostHealthResponseString(hostIds.toArray(new String[hostIds.size()]));

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech);
    }

    public String getHostHealthResponseString(String... hostIds) throws ReadingFromDatabaseException
    {
        String responseString;
        if (hostIds.length < 1)
        {
            responseString = PromptLibrary.NoHostAssociated.getPrompt();
        } else
        {
            StringBuilder responseText = new StringBuilder();
            int numberOfHostsSeenWithoutData = 0;
            for (String hostId : hostIds)
            {
                Item item;
                try
                {
                    Table table = dynamoDB.getTable("HostHealth");
                    item = table.getItem("HostId", hostId.replaceAll("\\s", "").replaceAll(",", ""));
                }
                catch (Exception e)
                {
                    throw new ReadingFromDatabaseException(e);
                }

                if (item == null)
                {
                    System.out.println("There is no host health for host with ID: " + hostId);
                    responseText.append("No data has been published for host ID " + hostId + ". "
                            + messageWhenIterateToHostWithNoData(numberOfHostsSeenWithoutData++));
                } else
                {
                    BigDecimal lastUpdatedInMillis = (BigDecimal) item.get("TimeOfLastUpdateInMillis");
                    String timeSinceLastUpdateFromHost = timeToText.millisecondsAsTime(lastUpdatedInMillis.longValue());

                    String usedSpace = (String) item.get("UsedSpace");
                    String existingSpace = (String) item.get("ExistingSpace");
                    String cpuLoad = (String) item.get("LoadPast1Min");
                    String friendlyName = (String) item.get("FriendlyName");

                    String fragmentForConsumedDisk;
                    try
                    {
                        fragmentForConsumedDisk = getFragmentForConsumedDisk(usedSpace, existingSpace);
                    } catch (Exception | Error e)
                    {
                        e.printStackTrace();

                        return PromptLibrary.ErrorCalculatingPercentDiskSpaceUsed.getPrompt();
                    }

                    responseText.append(friendlyName + " reported its health " + timeSinceLastUpdateFromHost + " ago and is using "
                            + fragmentForConsumedDisk + " of " + existingSpace + " and the CPU load avg over 1 minutes is " + cpuLoad + ". ");
                }
            }

            responseString = responseText.toString();
        }

        return responseString;
    }

    private String getFragmentForConsumedDisk(String usedSpace, String existingSpace)
    {
        int gbsConsumed = Integer.valueOf(usedSpace.replaceAll("GB",""));
        int gbsExisting = Integer.valueOf(existingSpace.replaceAll("GB",""));

        double percentageUsed = ((double)gbsConsumed) / ((double)gbsExisting) * 100;

        String percentageUsedAsString =  String.valueOf(percentageUsed);
        String percentageUsedShorten = percentageUsedAsString.substring(0, percentageUsedAsString.indexOf(".") == 2 ? 2 : 3);

        return percentageUsedShorten + " percent of the disk space";
    }

    private String messageWhenIterateToHostWithNoData(int numberOfHostsSeenWithoutData)
    {
        if (numberOfHostsSeenWithoutData == 0)
        {
            return "To disassociate a host ID from your account, ask host health to remove a host from your fleet. If this is a valid host ID, open your" +
                    " web browser and visit penny sydney dot dog, p, e, n, n, y, s, y, d, n, e, y, dot, d, o, g, for the commands to run on your host to publish host health. ";
        }

        return "";
    }
}