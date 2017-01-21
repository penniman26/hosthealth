package hosthealth.speechlet.prompts;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;

public class GeneralPromptRenderer
{
    public SpeechletResponse generateSpeechletTellResponseFromPrompt(PromptLibrary prompt)
    {
        PlainTextOutputSpeech promptSpeech = new PlainTextOutputSpeech();
        promptSpeech.setText(prompt.getPrompt());

        return SpeechletResponse.newTellResponse(promptSpeech);
    }

    public SpeechletResponse generateSpeechletAskResponseFromPrompt(PromptLibrary prompt, PromptLibrary repromptPrompt)
    {
        PlainTextOutputSpeech promptSpeech = new PlainTextOutputSpeech();
        promptSpeech.setText(prompt.getPrompt());

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(repromptPrompt.getPrompt());

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(promptSpeech, reprompt);
    }
}