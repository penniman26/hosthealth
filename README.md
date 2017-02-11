# HostHealth stores your host health and presents it through an alexa skill

Please feel free to contribute. Email me if you need help setting up your environment.

## How it works

### Publishing your host health

You publish your host health with a https restful post call to this endpoint and with this json format:
https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod

{
  "version":"1.0"
  
  , "HostId":"1234567890ab"
  
  , "diskSpaceTotal":"1000GB"
  
  , "diskSpaceUsed":"150GB"
  
  , "loadPast1Min":"1.2"
  
  , "friendlyName":"Home Desktop"
  
 }
 
These commands have been verified for Mac OS X Sierra and Linux RHEL 5. It should work the same on any other posix system.

loadPast1Min=\`uptime | grep -ohe 'average.\*' | sed 's/,//g' | awk '{ print $2}'\` && diskSpaceUsed=\`df | sed -n '2p' | awk '{print $3}'\` && diskSpaceTotal=\`df | sed -n '2p' | awk '{print $2}'\` && curl -H "Content-Type: application/json" -d '{"HostId":"#{substitue this including the # and the brackets for your mac address or any other alphanumeric ID of length 12}","diskSpaceTotal":"'$(($diskSpaceTotal/2/1024/1024))'GB","diskSpaceUsed":"'$(($diskSpaceUsed/2/1024/1024))'GB","loadPast1Min":"'$(echo $loadPast1Min)'","friendlyName":"Home Desktop","version":"1.0"}' https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod

#### install it to run every 1 minute:

crontab -e (opens a VIM editor. just need to paste this in. if the pasting screws up the formatting, use ":set paste" before pasting in vim)

\*/1 \* \* \* \* loadPast1Min=\`uptime | grep -ohe 'average.\*' | sed 's/,//g' | awk '{ print $2}'\` && diskSpaceUsed=\`df | sed -n '2p' | awk '{print $3}'\` && diskSpaceTotal=\`df | sed -n '2p' | awk '{print $2}'\` && curl -H "Content-Type: application/json" -d '{"HostId":"#{substitue this including the # and the brackets for your mac address or any other alphanumeric ID of length 12}","diskSpaceTotal":"'$(($diskSpaceTotal/2/1024/1024))'GB","diskSpaceUsed":"'$(($diskSpaceUsed/2/1024/1024))'GB","loadPast1Min":"'$(echo $loadPast1Min)'","friendlyName":"Home Desktop","version":"1.0"}' https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod >/dev/null 2>&1

#### every 15 minutes:

crontab -e

\*/15 \* \* \* \* loadPast1Min=\`uptime | grep -ohe 'average.\*' | sed 's/,//g' | awk '{ print $2}'\` && diskSpaceUsed=\`df | sed -n '2p' | awk '{print $3}'\` && diskSpaceTotal=\`df | sed -n '2p' | awk '{print $2}'\` && curl -H "Content-Type: application/json" -d '{"HostId":"#{substitue this including the # and the brackets for your mac address or any other alphanumeric ID of length 12}","diskSpaceTotal":"'$(($diskSpaceTotal/2/1024/1024))'GB","diskSpaceUsed":"'$(($diskSpaceUsed/2/1024/1024))'GB","loadPast1Min":"'$(echo $loadPast1Min)'","friendlyName":"Home Desktop","version":"1.0"}' https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod >/dev/null 2>&1

You may have to tweak the commands based on your operating system. If you do, it'd be great if you submit a pull request to update this file.

### How do I get my host health from alexa

Enable the host health skill. then say "alexa, ask host health to add mac address 0 1 2 3 4 5 6 7 8 9 a b". Make sure you say each digit instead of as a pair and skip over the colons. If it misinterprets your ID then just ask to remove a host from your fleet.

Once your hosts are linked to your alexa account you can ask for a report to hear your host health of your fleet.
