Cloud Formation Templates
=========================

These templates are created to make it easier to create a cluster on 
AWS using the aws-nats.py script.

There are two scripts on this folder. Both scripts to the same thing but 
the one named *aws-nats-no-ssh.json* does not have any access to the 
instances through SSH.


Cloud structure
---------------

The structure that this template creates on the AWS cloud is the following
one:

* A DynamoDB table (named *aws-nats*) to keep track of the available nodes.
* An AutoScaling group for the instances.
* IAM Roles to enable access to the DynamoDB table and to update instance states.
* A security group to enable access to the nodes.

All these items will be explained in detail in the following sections.


### DynamoDB table ###

This table is based on the [KCL table](http://docs.aws.amazon.com/kinesis/latest/dev/kinesis-record-processor-ddb.html) to manage the Kinesis Streams by Amazon. 
Of course this project has many differences because it is a different use, but is nice to check how that works.

The DynamoDB table created has a single HASH that represents each node on the cluster by using its IP address.

It also keeps a keep-alive timer to check that the instance is still there (time field) and a state field to know what is the instance doing.


### AutoScaling group ###

The AutoScaling group makes it easy to grow or shrink when the input changes. The aws-nats.py script keeps track of the status of the gnatsd Daemon and sends an Unhealty message to the AutoScaling group if something is wrong.


### IAM Roles ###

To enable access to the DynamoDB table and to allow to update the instance health (Healthy or Unhealthy) to the AutoScaling group, an IAM Role is created.
The role makes the the following statement:

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
    "Resource": { "Fn::Join" :  [ "",
        [ "arn:aws:dynamodb:", { "Ref" : "AWS::Region" }, ":",{"Ref": "AWS::AccountId"},":table/", { "Ref" : "awsnatsdb" } ]
      ]
    }
  }
]
```

So it only allows access to the specific DynamoDB table created in the specific Region and allows all access to SetInstanceHealth.


### Security Group ###

The security group only gives access to the following ports:

* SSH (port 22) to everyone, to have access to the instances using SSH (This is not enabled on the *aws-nats-no-ssh.json* template).
* NATS (port 4242) to everyone, to have access to the NATS queues from the clients.
* CLUSTER (port 7244) to everyone, to enable the instances to talk to each other. 


Configuration
-------------

The template has some configuration options detailed here:

* **KeyPair**: This is the Key to connect to the instances using SSH.
* **InstanceType**: The type of EC2 instance to be used in the cluster (t1.micro as default).
* **MinInstances**: An integer defining how many instances will be the minimum for the AutoScaling group.
* **MaxInstances**: An integer defining how many instances will be the maximum for the AutoScaling group.
* **NATSUser**: The user to connect to the NATS queue.
* **NATSPass**: The password to connect to the NATS queue.
* **NATSTimeout**: Value of seconds to use as timeout for the queue clients.
