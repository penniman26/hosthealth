package hosthealth.speechlet.speechlethelpers;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import hosthealth.speechlet.exceptions.ReadingFromDatabaseException;
import hosthealth.speechlet.exceptions.WritingToDatabaseException;
import hosthealth.speechlet.prompts.PromptLibrary;

import java.util.Map;
import java.util.stream.Collectors;

public class AddHostHostIdToAlexaAccount
{
    private final HostAssociationModule hostAssociation;
    private final GetHostHealth getHostHealth;
    private final GetHostName getHostName;

    public AddHostHostIdToAlexaAccount(HostAssociationModule hostAssocation, GetHostHealth getHostHealth, GetHostName getHostName)
    {
        this.hostAssociation = hostAssocation;
        this.getHostHealth = getHostHealth;
        this.getHostName = getHostName;
    }

    public SpeechletResponse addMacAddressResponse(String alexaId, Map<String,Slot> slots) throws ReadingFromDatabaseException, WritingToDatabaseException
    {
        boolean isValidSlotsForAddingHostIdToAlexaAccount = hostAssociation.isValidSlotsForAddingHostIdToAlexaAccount(slots);

        if (isValidSlotsForAddingHostIdToAlexaAccount)
        {
            String hostID = hostAssociation.addHostAssociation(alexaId, slots);

            String friendlyName = getHostName.getFriendlyName(hostID);
            String hostName = friendlyName != null ? friendlyName : hostID;

            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Successfully added " + hostName + " to your alexa account. " + getHostHealth.getHostHealthResponseString(hostID));

            return SpeechletResponse.newTellResponse(speech);
        }
        else
        {
            String speechText = "I'm sorry. We heard " + String.join(" ", slots.values().stream().map(slot -> slot.getValue()).collect(Collectors.toList()))
                    + " . Can you try again?";

            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(PromptLibrary.PleaseTryAgainOrSayStop.getPrompt());

            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt);
        }
    }
}