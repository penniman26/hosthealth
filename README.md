# HostHealth stores your host health and presents it through an alexa skill

Please contribute to the code if you'd like. Email me at admin@pennysydney.dog if you need help setting up your Amazon Web Services (AWS) account and development-only host health alexa skill. This really needs a windows client for posting host health data; at the moment it only works with Mac and Linux.

## What is the user experience?

The user configures their computer to send your disk space and CPU load metrics to https://hosthealth.pennysydney.dog. Then they say "alexa, ask host health to add a host {your alphanumeric host ID of length 12}" to associate their alexa account with the host's published data. Then we know which host data to use when they ask host health for a report.

See https://github.com/penniman26/hosthealth/wiki/Publishing-your-host-health-to-the-alexa-host-health-skill for user setup instructions

## What technologies are used?

The host makes an https post request with their host health to an AWS API Gateway endpoint (https://hosthealth.pennysydney.dog) which routes that request to an AWS Lambda https://aws.amazon.com/lambda/ instance. See https://github.com/penniman26/hosthealth/tree/mainline/src/main/java/hosthealth/receivepublishedhealth for the code that is uploaded to the AWS Lambda instance. The code receives the request, validates the input, then saves the data to an AWS DynamoDB table.

Alexa Skills are created through the developer portal at https://developer.amazon.com/alexa-skills-kit. You specify a set of requests and the phrases used to invoke those requests, and then you give the amazon resource name (ARN) of your AWS Lambda instance for handling the alexa skill utterances. The code uploaded to this AWS Lambda instance is at https://github.com/penniman26/hosthealth/tree/mainline/src/main/java/hosthealth/speechlet. The code recieves the utterance request from alexa, if necessary updates or queries the DynamoDB table(s), and returns what to say back to the user.
