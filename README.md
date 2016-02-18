AWS NATS
========

This repository contains tools and templates to help using [NATS](http://nats.io) in the [Amazon Web Services](http://aws.amazon.com) platform.

[NATS](http://nats.io) is a Scalable, Performant, Simple and Open Source messaging platform. 
There are two implementations of this messaging platform but in this project we use [gnats](https://github.com/nats-io/gnatsd) that is recoded in GoLang.

There are also in this repository some [CloudFormation templates](https://github.com/dankomiocevic/aws-nats/tree/master/cloud_formation_templates) and you can find more specific information about them in that section.


aws-nats script
---------------

The script is written in Python and it does the following steps:

1. Get instance public IP address.
2. Get instance ID.
3. Get the list of available servers from the DynamoDB table.
  * The server list is filtered by the keep-alive timestamp (time field) and only the servers that have been active on the last 30 seconds (default value) will be used. Servers with more than 3 minutes (default value) of inactivity will be removed from the list.
4. Generate the NATS Cluster configuration file. See [NATS Server Clustering](http://nats.io/documentation/server/gnatsd-cluster/) for more info.
5. Run the gnats daemon.
6. Keep checking the daemon state every 10 seconds.

Between all these steps, it also updates the keep-alive timestamp of itself every 10 or less seconds to show the other instances that is still available. When the gnats daemon is down, it sends an Unhealthy signal to the AutoScaling group that contains the instance to be removed.


### Configuration options ###

This script uses [Boto3](https://github.com/boto/boto3) to connect to AWS services and it requires some configuration options to send the credentials to it. In the CloudFormation templates defined as example in this repository, the Boto3 SDK credentials are obtained from the IAM Role defined in the template.

The IAM statement required is as follows:

```json
"Statement": [
  {
    "Action":[
      "autoscaling:SetInstanceHealth"
    ],
    "Effect": "Allow",
    "Resource": "*"
  },
  {
    "Action": [
      "dynamodb:Scan",
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem",
      "dynamodb:DeleteItem"
    ],
    "Effect": "Allow",
    "Resource": "DynamoDB table ARN"
    }
  }
]
```

The script also uses a configuration file as follows:

```ini
[general]
servers-timeout=30
delete-timeout=300

[DynamoDB]
table=aws-nats

[user]
nats_user=some_user 
nats_pass=T0pS3cr3tT00!
timeout=0.5
```

The options are:

* **General**
  * **servers-timeout**: Time in seconds for the last keep-alive from an instance to be considered alive.
  * **delete-timeout**: Time in seconds for the las keep-alive from an instance to be deleted from the table.
* **DynamoDB**
  * **table**: The table name on DynamoDB.
* **user**
  * **nats_user**: The user to connect to the NATS messaging system.
  * **nats_pass**: The password to connect to the NATS messaging system.
  * **timeout**: Timeout in seconds for the connections to the NATS messaging system.


### Requirements ###

This script requires the following packages:

* [Boto3](https://github.com/boto/boto3): An SDK to use AWS from Python.
* [requests](http://docs.python-requests.org/en/master/): Easy way to communicate with HTTP services.

It also requires a DynamoDB table to be created with a String HASH key named "ip". This table will be used to keep track of the available servers at any moment. It maintains a keep-alive timer to check that the instance is still there (time field) and a state field to know what is the instance doing.


### CLI Options ###

These are the options to use aws-nats from the CLI:

```bash
aws-nats -c <configfile> -n <natsconfigfile>
```

Where:

* *configfile*: Is the config file defined in the previous sections. 
* *natsconfigfile*: Is the path where the gnatsd configuration file will be created.


License
-------

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
