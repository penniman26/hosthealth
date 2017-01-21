package hosthealth.speechlet.prompts;

/**
 * Used for converting time into speakable text.
 */
public class TimeToTextModule
{
    public String millisecondsAsTime(long lastUpdatedTimeInMillis)
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
