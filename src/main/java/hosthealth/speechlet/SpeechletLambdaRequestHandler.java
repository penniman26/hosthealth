package hosthealth.speechlet;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * Entrance point for the lambda from aws. All routing logic is housed in HostHealthSpeechlet.
 */
public final class SpeechletLambdaRequestHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        supportedApplicationIds.add("amzn1.ask.skill.cb070601-9202-4fb9-a868-f279e9a00652");
    }

    public SpeechletLambdaRequestHandler() {
        super(new HostHealthSpeechlet(), supportedApplicationIds);
    }
}

