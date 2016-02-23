aws-nats Client Side Examples
=============================

In this directory there are some samples on how to use the aws-nats generated cluster from the application side.

The only thing needed in this case is to retrieve the servers list from the DynamoDB database, then the list is sent to the client NATS code.
So the steps are the following:

1. Connect to DynamoDB table.
2. Scan all the items in the table.
3. Filter the servers that have more than 30 seconds (default) of inactivity.
4. Pass the server list to the NATS client code.


Java
----

This example shows how to use the AWS SDK to retrive the servers using Java.
