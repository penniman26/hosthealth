# HostHealth stores your host health and presents it through an alexa skill

Please feel free to contribute. Email me if you need help setting up your environment.

## How it works

You configure your computer to send your disk space and CPU load metrics to https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod using the below commands. Then you enable the host health alexa skill, and then say "alexa, ask host health to add a host {your alphanumeric host ID of length 12}" to associate your alexa account with the published data. Then we know which host data to use when you ask host health for a report.

See https://github.com/penniman26/hosthealth/wiki/Publishing-your-host-health-to-the-alexa-host-health-skill for user setup instructions
