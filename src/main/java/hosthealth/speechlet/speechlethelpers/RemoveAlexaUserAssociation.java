package hosthealth.speechlet.speechlethelpers;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import hosthealth.speechlet.exceptions.ReadingFromDatabaseException;
import hosthealth.speechlet.exceptions.WritingToDatabaseException;
import hosthealth.speechlet.prompts.GeneralPromptRenderer;
import hosthealth.speechlet.prompts.PromptLibrary;

import java.util.List;
import java.util.function.Function;

public class RemoveAlexaUserAssociation
{
    private final HostAssociationModule hostAssociation;
    private final GeneralPromptRenderer generalPromptRenderer;
    private final GetHostName getHostName;

    public RemoveAlexaUserAssociation(GetHostName getHostName, HostAssociationModule hostAssociation, GeneralPromptRenderer generalPromptRenderer)
    {
        this.hostAssociation = hostAssociation;
        this.generalPromptRenderer = generalPromptRenderer;
        this.getHostName = getHostName;
    }

    public SpeechletResponse removeMacAddressResponse(boolean isResponseYes, boolean isResponseNo, String alexaId, int indexOfMultiTurnIteratorOfHosts
            , Function<Void, Void> nextHostCallback) throws WritingToDatabaseException, ReadingFromDatabaseException
    {
        List<String> hostIds = hostAssociation.getHostIdsFromAlexaId(alexaId);

        if (isResponseYes)
        {
            String hostId = hostIds.get(indexOfMultiTurnIteratorOfHosts);

            hostAssociation.removeHostAssociation(alexaId, hostId);

            return generalPromptRenderer.generateSpeechletTellResponseFromPrompt(PromptLibrary.HostDisassociated);
        }
        else
        {
            if (isResponseNo)
            {
                nextHostCallback.apply(null);
                indexOfMultiTurnIteratorOfHosts = indexOfMultiTurnIteratorOfHosts + 1;
            }

            if (indexOfMultiTurnIteratorOfHosts >= hostIds.size())
            {
                return SpeechletResponse.newTellResponse(new PlainTextOutputSpeech());
            }

            String hostId = hostIds.get(indexOfMultiTurnIteratorOfHosts);

            String hostName = getHostName.getFriendlyName(hostId);

            String speechText = "Would you like to disassociate from " + (hostName != null ? hostName : hostId) + "?";

            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            PlainTextOutputSpeech repromtSpeech = new PlainTextOutputSpeech();
            repromtSpeech.setText(speechText);

            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromtSpeech);


            return SpeechletResponse.newAskResponse(speech, reprompt);
        }
    }

}