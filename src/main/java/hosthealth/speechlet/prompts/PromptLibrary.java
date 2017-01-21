package hosthealth.speechlet.prompts;

/**
 * All prompts that don't require variable injection.
 */
public enum PromptLibrary
{
    Help("Checkout the alexa skill page to see the"
            + "script for posting your host health. The data published from your host is both the disk space used and available as" +
            " well as the CPU load. Once you've setup posting your host health, ask alexa to add your mac address to link to your "
            + "host in the format, alexa, add mac address 1 2 3 4 a b c d 1 2. You can choose your mac address or some other ID. Just make" +
            " sure it is the same one used in your host health publish script. Then whenever you ask for your host health, we know " +
            "which hosts to report. To disassociate a host from you, ask to remove the host from your fleet, and alexa will iterate " +
            "through each host in your fleet. Would you like to hear this help section again?")
    , HowToHearHelpMessageAgain("Say, help, to hear this message again.")
    , RePromptWelcomeMessage("Ask for your report or ask to add your mac address of length 12 reading each digit and skipping over the" +
        " colons. Ask host health for help to hear the correct format on adding a mac address or other ID published from your host. You" +
        "can choose your mac address or some other ID in the host health publish script.")
    , WelcomeMessage("Welcome to host health. This skill reads host health to you thats been published from a set of your hosts. Ask host" +
        " health for your host health report.")
    , NoHostAssociated("You must first ask host health to add your mac address or other I D . Ask host health for help to hear more.")
    , PleaseTryAgainOrSayStop("Please try again or say stop or nevermind.")
    , ErrorSavingToDatabase("We encountered an error saving to the database. Please try again and if problem persists, please report the problem.")
    , ErrorReadingFromDatabase("We encountered an error reading from the database. Please try again and if problem persists, please report the problem.")
    , HostDisassociated("Okay. Host successfully disassociated.")
    , ErrorCalculatingPercentDiskSpaceUsed("Please check the format of the used and existing disk space values passed in your host health report from your computer. " +
        "Visit p, e, n, n, y, s, y, d, n, e, y, dot, d, o, g, for help publishing your host health. Find this link in the host health skill's" +
                " description section. ");
    private final String prompt;

    PromptLibrary(String prompt)
    {
        this.prompt = prompt;
    }

    public String getPrompt()
    {
        return prompt;
    }
}
