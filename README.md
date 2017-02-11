# HostHealth stores your host health and presents it through an alexa skill

Please contribute to the code if you'd like. Email me at admin@pennysydney.dog if you need help setting up your Amazon Web Services (AWS) account and development-only host health alexa skill. This really needs a windows client for posting host health data; at the moment it only works with Mac and Linux.

## How it works

The user configures their computer to send your disk space and CPU load metrics to https://kwydc0d3o7.execute-api.us-east-1.amazonaws.com/prod. Then they say "alexa, ask host health to add a host {your alphanumeric host ID of length 12}" to associate their alexa account with the host's published data. Then we know which host data to use when they ask host health for a report.

See https://github.com/penniman26/hosthealth/wiki/Publishing-your-host-health-to-the-alexa-host-health-skill for user setup instructions
