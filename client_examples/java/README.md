JAVA aws-nats example
=====================

This example is created to use in conjunction with [jnats](https://github.com/nats-io/jnats) java client.

The idea with this code is to generate a list of servers to use in jnats. 

The code should be added as a single class to the project and requires the [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/).


Basic usage
-----------

Taking as a reference the code from [jnats](https://github.com/nats-io/jnats) README file, this would be the usage for this example:

```java
// Creating a Regions object from the configuration string.
Regions r = Regions.fromName("us-west-2");

// Get the servers list from the Cluster Manager.
String[] servers = NATSClusterManager.getServers(table, accessKey, secretKey, r, username, password);
		
// If servers are null something went wrong.
if(servers == null){
	System.out.printf("There was a problem getting the servers.");
	return;
}

// Setup options to include all servers in the cluster
ConnectionFactory cf = new ConnectionFactory();
cf.setServers(servers);

// Optionally set ReconnectWait and MaxReconnect attempts.
// This example means 10 seconds total per backend.
cf.setMaxReconnect(5);
cf.setReconnectWait(2000);

// Keep randomize enabled to distribute the connected clients.
cf.setNoRandomize(false);

Connection nc = cf.createConnection();

// Setup callbacks to be notified on disconnects and reconnects
nc.setDisconnectedCallback(new DisconnectedCallback() {
	public void onDisconnect(ConnectionEvent event) {
    	System.out.printf("Got disconnected!\n")
    }
});

// See who we are connected to on reconnect.
nc.setReconnectedCallback(new ReconnectedCallback() {
	public void onReconnect(ConnectionEvent event) {
	    System.out.printf("Got reconnected to %s!\n", event.getConnectedUrl())
    }
});

// Setup a callback to be notified when the Connection is closed
nc.setClosedCallback(new ClosedCallback() {
	public void onClose(ConnectionEvent event) {
	    System.out.printf("Got reconnected to %s!\n", event.getConnectedUrl())
    }
});

```
