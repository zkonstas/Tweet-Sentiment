import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class SqsConnect {
	static int currentDBSize = 0;

	@SuppressWarnings("null")
	public void run() throws SQLException, InterruptedException, IOException {
		// TODO Auto-generated method stub

		AWSCredentials credentials = new PropertiesCredentials(
	   			 SqsConnect.class.getResourceAsStream("AwsCredentials.properties"));
		
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("*");
		dataSource.setPassword("*");
		dataSource.setPort(3306);
		dataSource.setDatabaseName("TwitterDB");
		dataSource.setServerName("*.rds.amazonaws.com");

		Connection conn = dataSource.getConnection();
		Statement sqlStatement = conn.createStatement();
//		try {
//			credentials = new ProfileCredentialsProvider().getCredentials();
//		} catch (Exception e) {
//			throw new AmazonClientException(
//					"Cannot load the credentials from the credential profiles file. "
//							+ "Please make sure that your credentials file is at the correct "
//							+ "location (~/.aws/credentials), and is in valid format.",
//					e);
//		}
		AmazonSQS sqs = new AmazonSQSClient(credentials);
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		sqs.setRegion(usEast1);
		System.out.println("===========================================");
		System.out.println("Getting Started with Amazon SQS");
		System.out.println("===========================================\n");
		try {
			// Create a queue
			System.out.println("Creating a new SQS queue called MyQueue.\n");
			CreateQueueRequest createQueueRequest = new CreateQueueRequest(
					"MyQueue");
			String myQueueUrl = sqs.createQueue(createQueueRequest)
					.getQueueUrl();
			// List queues
			System.out.println("Listing all queues in your account.\n");
			for (String queueUrl : sqs.listQueues().getQueueUrls()) {
				System.out.println(" QueueUrl: " + queueUrl);
			}
			System.out.println();
			
			// Send a message

			while(true){
				System.out.println("Sending a message to MyQueue.\n");
				//sqs.sendMessage(new SendMessageRequest(myQueueUrl,
						//"This is my message text."));
				ArrayList<String> newTweets = fetchMessages(sqlStatement);
				if (newTweets != null || newTweets.size() !=0){
					for(String tweet:newTweets){
						String[] tweetAttributes = tweet.split("_");
						String message = tweetAttributes[0]+"_"+tweetAttributes[3];
						//System.out.println("0: "+tweetAttributes[0]+" 1: "+tweetAttributes[1]+" 2: "+tweetAttributes[2]+" 3:"+tweetAttributes[3]);
						sqs.sendMessage(new SendMessageRequest(myQueueUrl,message));
					}
				}
				System.out.println(newTweets.size());
				System.out.println("sleeping for a while: Zzzzzzzzz");
	            Thread.currentThread().sleep(60000);

			}
			
		} catch (AmazonServiceException ase) {
			System.out
					.println("Caught an AmazonServiceException, which means your request made it "
							+ "to Amazon SQS, but was rejected with an error response for some reason.");
			System.out.println("Error Message: " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code: " + ase.getErrorCode());
			System.out.println("Error Type: " + ase.getErrorType());
			System.out.println("Request ID: " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out
					.println("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with SQS, such as not "
							+ "being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
	
	public ArrayList<String> fetchMessages(Statement sqlStatement) throws SQLException{
		
		ArrayList<String> messages = new ArrayList<String>();
        String query1 = "SELECT count(*) AS count FROM tweets";
        String query2 = "SELECT * FROM tweets";
        ResultSet sqlResult1 = sqlStatement.executeQuery(query1);
        int dbSize = 0;
        while(sqlResult1.next()){
            dbSize = sqlResult1.getInt("count");
        }
        System.out.println(dbSize);
        if (dbSize > currentDBSize){
            ResultSet sqlResult2 = sqlStatement.executeQuery(query2);
            sqlResult2.absolute(currentDBSize);
            while(sqlResult2.next() ){
            	StringBuffer sb = new StringBuffer();
                sb.append(sqlResult2.getString("tweet_id"));
                sb.append("_");
                sb.append(sqlResult2.getString("coordinates"));
                sb.append("_");
                sb.append(sqlResult2.getString("created_at"));
                sb.append("_");
                sb.append(sqlResult2.getString("tweet_text"));
                //System.out.println(sb.toString());
                messages.add(sb.toString());
            }
            
        }
        currentDBSize = dbSize;
        System.out.println("Messages to be sent to be saved in the Queue");
		return messages;
	}

}
