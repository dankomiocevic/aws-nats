package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;

/**
 * The NATSClusterManager class manages the cluster created using aws-nats.
 * 
 * @author Danko Miocevic
 * 
 */
public class NATSClusterManager {
	private static final int SERVER_TIMEOUT = 30;
	private static final int SERVER_PORT = 4242;
	
	/**
	 * getServers obtains a list of servers from the aws-nats DynamoDB table. It
	 * connects with the DynamoDB table that contains the servers information
	 * and retrieves all the servers that are available without authentication.
	 * 
	 * @param tableName
	 *            The name of the DynamoDB table.
	 * @param accessKey
	 *            Is the AWS Credentials key to access the DynamoDB server.
	 * @param accessPassword
	 *            Is the AWS Credentials password to access the DynamoDB server.
	 * @param region
	 *            The region where the Table is located.
	 * @return An array of servers or null if there was a problem.
	 */
	public static String[] getServers(String tableName, String accessKey,
			String accessPassword, Regions region) {
		return getServers( tableName,  accessKey,
				 accessPassword,  region,  null, null);
	}
	
	/**
	 * getServers obtains a list of servers from the aws-nats DynamoDB table. It
	 * connects with the DynamoDB table that contains the servers information
	 * and retrieves all the servers that are available.
	 * 
	 * The username for the NATS server can be null, in that case the 
	 * servers will be generated without authentication.
	 * 
	 * @param tableName
	 *            The name of the DynamoDB table.
	 * @param accessKey
	 *            Is the AWS Credentials key to access the DynamoDB server.
	 * @param accessPassword
	 *            Is the AWS Credentials password to access the DynamoDB server.
	 * @param region
	 *            The region where the Table is located.
	 * @param username
	 * 			  The NATS server username.
	 * @param password
	 * 			  The NATS server password.
	 * @return An array of servers or null if there was a problem.
	 */
	public static String[] getServers(String tableName, String accessKey,
			String accessPassword, Regions region, String username, String password) {
		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey,
				accessPassword);
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(awsCredentials);
		client.setRegion(Region.getRegion(region));
		DynamoDB dynamoDB = new DynamoDB(client);
		Table serversTable = dynamoDB.getTable(tableName);
		
		long currentTime = System.currentTimeMillis() / 1000;

		Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
		expressionAttributeValues.put(":val", currentTime - SERVER_TIMEOUT);
		Map<String, String> expressionAttributeNames = new HashMap<String, String>();
		expressionAttributeNames.put("#att", "time");

		ItemCollection<ScanOutcome> columns = serversTable.scan(new ScanSpec()
				.withFilterExpression("#att > :val")
				.withProjectionExpression("ip")
				.withNameMap(expressionAttributeNames)
				.withValueMap(expressionAttributeValues).withMaxResultSize(50));
				
		ArrayList<String> servers = new ArrayList<String>();
		try {
			Iterator<Item> iterator = columns.iterator();
			while (iterator.hasNext()) {
				Item item = iterator.next();
				if(username == null) {
					servers.add("nats://"+item.getString("ip")+":"+SERVER_PORT);
				} else {
					servers.add("nats://"+username+":"+password+"@"+item.getString("ip")+":"+SERVER_PORT);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		// Looks like that due to JVM optimizations is better
		// to use new String[0] here instead of new String[list.size()]
		// Don't know for sure, but it is not going to change too much
		// of the performance. XD
		return servers.toArray(new String[0]);
	}
}
