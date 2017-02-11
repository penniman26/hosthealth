# HostHealth stores your host health and presents it through an alexa skill

Please feel free to contribute. Email me if you need help setting up your environment.

## How it works

You configure your computer to send your disk space and CPU load metrics to https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod using the below commands. Then you enable the host health alexa skill, and then say "alexa, ask host health to add a host {your alphanumeric host ID of length 12}" to associate your alexa account with the published data. Then we know which host data to use when you ask host health for a report.

### System Requirements
These commands have been verified for Mac OS X Sierra and Linux RHEL 5. It should work the same on any other posix system.

### Publishing your host health

You may run the below command to post your host health once, or follow the "install it to run every x minute(s)" instructions to keep your host health data up to date for alexa to read back to you.

loadPast1Min=\`uptime | grep -ohe 'average.\*' | sed 's/,//g' | awk '{ print $2}'\` && diskSpaceUsed=\`df | sed -n '2p' | awk '{print $3}'\` && diskSpaceTotal=\`df | sed -n '2p' | awk '{print $2}'\` && curl -H "Content-Type: application/json" -d '{"HostId":"#{substitue this including the # and the brackets for your mac address or any other alphanumeric ID of length 12}","diskSpaceTotal":"'$(($diskSpaceTotal/2/1024/1024))'GB","diskSpaceUsed":"'$(($diskSpaceUsed/2/1024/1024))'GB","loadPast1Min":"'$(echo $loadPast1Min)'","friendlyName":"Home Desktop","version":"1.0"}' https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod

#### install it to run every 1 minute:

crontab -e (opens a VIM editor. just need to paste this in. if the pasting screws up the formatting, use ":set paste" before pasting in vim)

\* 1 \* \* \* \* loadPast1Min=\`uptime | grep -ohe 'average.\*' | sed 's/,//g' | awk '{ print $2}'\` && diskSpaceUsed=\`df | sed -n '2p' | awk '{print $3}'\` && diskSpaceTotal=\`df | sed -n '2p' | awk '{print $2}'\` && curl -H "Content-Type: application/json" -d '{"HostId":"#{substitue this including the # and the brackets for your mac address or any other alphanumeric ID of length 12}","diskSpaceTotal":"'$(($diskSpaceTotal/2/1024/1024))'GB","diskSpaceUsed":"'$(($diskSpaceUsed/2/1024/1024))'GB","loadPast1Min":"'$(echo $loadPast1Min)'","friendlyName":"Home Desktop","version":"1.0"}' https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod >/dev/null 2>&1

#### every 15 minutes:

crontab -e

\* 15 \* \* \* \* loadPast1Min=\`uptime | grep -ohe 'average.\*' | sed 's/,//g' | awk '{ print $2}'\` && diskSpaceUsed=\`df | sed -n '2p' | awk '{print $3}'\` && diskSpaceTotal=\`df | sed -n '2p' | awk '{print $2}'\` && curl -H "Content-Type: application/json" -d '{"HostId":"#{substitue this including the # and the brackets for your mac address or any other alphanumeric ID of length 12}","diskSpaceTotal":"'$(($diskSpaceTotal/2/1024/1024))'GB","diskSpaceUsed":"'$(($diskSpaceUsed/2/1024/1024))'GB","loadPast1Min":"'$(echo $loadPast1Min)'","friendlyName":"Home Desktop","version":"1.0"}' https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod >/dev/null 2>&1

### How do I get my host health from alexa?

Enable the host health skill. Then say "alexa, ask host health to add host 0 1 2 3 4 5 6 7 8 9 a b". Make sure you say each letter and digit instead of as a pair and make sure to skip reading the colons.

Once your hosts are linked to your alexa account you can say "alexa, ask host health for a report" to hear your host health.
