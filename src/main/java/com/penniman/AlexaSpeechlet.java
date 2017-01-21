package com.penniman;

import com.amazon.speech.ui.Reprompt;
import com.amazonaws.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;

import java.math.BigDecimal;
import java.util.Map;

public class AlexaSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(AlexaSpeechlet.class);
    private DynamoDB dynamoDB;
    
    public AlexaSpeechlet()
    {
        dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new EnvironmentVariableCredentialsProvider()));
    }
    
    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("HostHealthIntent".equals(intentName))
        {
            return getHostHealthResponse(getUserId(session.getUser().getUserId()));
        } else if ("SetMacAddressIntent".equals(intentName))
        {
            return setMacAddressResponse(session.getUser().getUserId(), request.getIntent().getSlots());
        } else if ("AMAZON.HelpIntent".equals(intentName) || ("AMAZON.YesIntent".equals(intentName) && !session.isNew()))
        {
            return getHelpResponse();
        } else if ("AMAZON.CancelIntent".equals(intentName) || "AMAZON.StopIntent".equals(intentName))
        {
            return SpeechletResponse.newTellResponse(new PlainTextOutputSpeech());

        } else if ("AMAZON.NoIntent".equals(intentName))
        {
            return SpeechletResponse.newTellResponse(new PlainTextOutputSpeech());
        } else
            {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to host health. Ask for your host health or ask to set your mac address of length 12 without saying colons and with reading each digit. For example, "
                + "Alexa, ask host health to set my mac address to a b c d e f g h 1 2 3 4.";

        SimpleCard card = new SimpleCard();
        card.setTitle("HostHealth");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText("Ask for your host health or ask to set your mac address of length 12 without saying colons and with reading each digit. ");
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "This skill reads host health to you thats been published from one of your hosts. Checkout the alexa skill page to see the"
                + "script for posting your host health. Once you've setup posting your host health, ask alexa to set your mac address to link to your "
                + "host. Then whenever you ask for your host health, we know which host to report when you ask, alexa, what is my host health. Would"
                + " you like to hear this again?";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelpHostHealth");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        PlainTextOutputSpeech askIfWantToHearAgain = new PlainTextOutputSpeech();
        askIfWantToHearAgain.setText("Say, alexa, ask host health for help to hear this message again");

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(askIfWantToHearAgain);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
    
    private String getUserId(String alexaId)
    {
        Item item = null;
        try{
            Table table = dynamoDB.getTable("UserId");
            item = table.getItem("AlexaId", alexaId);
        }
        catch (Error e)
        {
            e.printStackTrace();
        }
        
        if (item != null)
        {
            return (String)item.get("UserId");
        }
        else
        {
            return null;
        }
    }
    
    private SpeechletResponse setMacAddressResponse(String alexaId, Map<String,Slot> slots)
    {
        String macAddress = "";
        String[] letters = new String[]{"A","B","C","D","E","F","G","H","I","J","K", "L"};
        for (int i = 0; i < 12; i++)
        {
            String letter = slots.get("LetterOrNumber" + letters[i]).getValue();

            if (letter == null)
            {
                 letter = "null";
            }

            macAddress = macAddress + StringUtils.lowerCase(letter.replaceAll("\\.","")) + ", ";
        }
        
        String speechText;
        try{
            Item item = new Item().withString("AlexaId", alexaId).withString("UserId",macAddress.replaceAll("\\s", "").replaceAll(",",""));
            Table table = dynamoDB.getTable("UserId");
            table.putItem(item);
            
            speechText = "Successfully set your mac address to: " + macAddress + ". To get your host health, ask for your host health.";
        }
        catch (Error e)
        {
            e.printStackTrace();
            return null;
        }
        
        SimpleCard card = new SimpleCard();
        card.setTitle("SetUserId");
        card.setContent(speechText);
        
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        PlainTextOutputSpeech repromtSpeech = new PlainTextOutputSpeech();
        repromtSpeech.setText("To get your host health, ask for your host health");

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromtSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
    
    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHostHealthResponse(String userId) {
        String speechText;
        if (userId == null)
        {
            speechText = "You must first ask host health to set your mac address of length 12 without saying colons and with reading each digit.";
        }
        else
        {
            Item item = null;
            try{
                Table table = dynamoDB.getTable("HostHealth");
                item = table.getItem("UserId", userId);
            }
            catch (Error e)
            {
                e.printStackTrace();
            }
            
            if (item == null)
            {
                System.out.println("No host health for customer: " + userId);
                speechText = "You have not published any host health yet";
            }
            else
            {
                String usedSpace = (String)item.get("UsedSpace");
                String existingSpace = (String)item.get("ExistingSpace");
                String cpuLoad = (String)item.get("LoadPast15Min");
                BigDecimal lastUpdatedInMillis = (BigDecimal)item.get("TimeOfLastUpdateInMillis");
                speechText = "Your host reported its health " + millisecondsAsTime(lastUpdatedInMillis.longValue()) + " ago and has used "+usedSpace+" out of "+existingSpace+" and the CPU load avg over 15 minutes is " + cpuLoad;
            }
        }
        
        SimpleCard card = new SimpleCard();
        card.setTitle("GetHostHealth");
        card.setContent(speechText);
        
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);
        
        return SpeechletResponse.newTellResponse(speech, card);
    }

    private String millisecondsAsTime(long lastUpdatedTimeInMillis)
    {
        long elapseMillis = System.currentTimeMillis() - lastUpdatedTimeInMillis;

        int seconds = (int)(elapseMillis / 1000);
        int minutes = seconds / 60;
        int hours  = minutes / 60;
        int days = hours / 24;

        StringBuilder stringBuilder = new StringBuilder();
        if (days != 0)
        {
            stringBuilder.append(days + " days, ");
        }
        if (hours != 0)
        {
            stringBuilder.append(hours % 24 + " hours, ");
        }
        if (minutes != 0)
        {
            stringBuilder.append(minutes % 60 + " minutes, ");
        }
        if (seconds != 0)
        {
            stringBuilder.append(seconds % 60 + " seconds");
        }

        return stringBuilder.toString();
    }
}

