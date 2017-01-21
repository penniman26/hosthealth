package hosthealth.speechlet;

import hosthealth.speechlet.exceptions.ReadingFromDatabaseException;
import hosthealth.speechlet.exceptions.WritingToDatabaseException;
import hosthealth.speechlet.prompts.GeneralPromptRenderer;
import hosthealth.speechlet.speechlethelpers.GetHostHealth;
import hosthealth.speechlet.speechlethelpers.GetHostName;
import hosthealth.speechlet.speechlethelpers.HostAssociationModule;
import hosthealth.speechlet.prompts.PromptLibrary;
import hosthealth.speechlet.speechlethelpers.RemoveAlexaUserAssociation;
import hosthealth.speechlet.speechlethelpers.AddHostHostIdToAlexaAccount;
import hosthealth.speechlet.prompts.TimeToTextModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;

/**
 * Receives utterances to the host health alexa skill in the form of an Intent. Registered to the lambda by the SpeechletLambdaRequestHandler.
 */
public class HostHealthSpeechlet implements Speechlet
{
    private static final Logger log = LoggerFactory.getLogger(HostHealthSpeechlet.class);
    private final DynamoDB dynamoDB;
    private final HostAssociationModule hostAssociation;
    private final TimeToTextModule timeToText;
    private final GeneralPromptRenderer generalPromptRenderer;
    private final GetHostHealth getHostHealth;
    private final RemoveAlexaUserAssociation removeAlexaUserAssociation;
    private final AddHostHostIdToAlexaAccount addHostHostIdToAlexaAccount;
    private final GetHostName getHostName;

    public HostHealthSpeechlet()
    {
        dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new EnvironmentVariableCredentialsProvider()));
        hostAssociation = new HostAssociationModule(dynamoDB);
        timeToText = new TimeToTextModule();
        generalPromptRenderer = new GeneralPromptRenderer();
        getHostHealth = new GetHostHealth(dynamoDB, generalPromptRenderer, timeToText);
        getHostName = new GetHostName(dynamoDB);
        removeAlexaUserAssociation = new RemoveAlexaUserAssociation(getHostName, hostAssociation, generalPromptRenderer);
        addHostHostIdToAlexaAccount = new AddHostHostIdToAlexaAccount(hostAssociation, getHostHealth, getHostName);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        String previousIntentName = session.getAttribute("prevSlotValue") != null ? ((String)session.getAttribute("prevSlotValue")) : null;
        Integer indexOfMultiTurnIteratorOfHosts = session.getAttribute("indexOfMultiTurnIteratorOfHosts") != null
                ? (int)session.getAttribute("indexOfMultiTurnIteratorOfHosts") : 0;
        session.removeAttribute("prevSlotValue");
        session.removeAttribute("indexOfMultiTurnIteratorOfHosts");

        try
        {
            if ("HostHealthIntent".equals(intentName))
            {
                return getHostHealth.getHostHealthResponse(hostAssociation.getHostIdsFromAlexaId(session.getUser().getUserId()));
            } else if ((("AMAZON.YesIntent".equals(intentName) || "AMAZON.NoIntent".equals(intentName)) && "RemoveMacAddressIntent".equals(previousIntentName))
                    || "RemoveMacAddressIntent".equals(intentName))
            {
                session.setAttribute("indexOfMultiTurnIteratorOfHosts", indexOfMultiTurnIteratorOfHosts);
                session.setAttribute("prevSlotValue", "RemoveMacAddressIntent");

                return removeAlexaUserAssociation.removeMacAddressResponse("AMAZON.YesIntent".equals(intentName), "AMAZON.NoIntent".equals(intentName),
                        session.getUser().getUserId(), indexOfMultiTurnIteratorOfHosts
                        , (noArgCallback) ->
                        {
                            session.setAttribute("indexOfMultiTurnIteratorOfHosts", indexOfMultiTurnIteratorOfHosts + 1);

                            return null;
                        });
            } else if ("AddMacAddressIntent".equals(intentName))
            {
                return addHostHostIdToAlexaAccount.addMacAddressResponse(session.getUser().getUserId(), request.getIntent().getSlots());
            } else if ("AMAZON.HelpIntent".equals(intentName) || (("AMAZON.YesIntent".equals(intentName) && "Amazon.HelpIntent".equals(previousIntentName))))
            {
                session.setAttribute("prevSlotValue", "Amazon.HelpIntent");

                return generalPromptRenderer.generateSpeechletAskResponseFromPrompt(PromptLibrary.Help, PromptLibrary.HowToHearHelpMessageAgain);
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
        catch (WritingToDatabaseException e)
        {
            e.printStackTrace();

            return generalPromptRenderer.generateSpeechletTellResponseFromPrompt(PromptLibrary.ErrorSavingToDatabase);

        }
        catch (ReadingFromDatabaseException e)
        {
            e.printStackTrace();

            return generalPromptRenderer.generateSpeechletTellResponseFromPrompt(PromptLibrary.ErrorReadingFromDatabase);
        }
    }

    //
    // Unused
    //

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException
    {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException
    {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        return generalPromptRenderer.generateSpeechletAskResponseFromPrompt(PromptLibrary.WelcomeMessage, PromptLibrary.RePromptWelcomeMessage);
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException
    {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }
}

