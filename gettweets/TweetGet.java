import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.auth.*;

/**
 * <p>
 * This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TweetGet {
	/**
	 * Main entry of this application.
	 *
	 * @param args
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void run() throws TwitterException,
			SQLException, IOException {
		// just fill this
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("*")
				.setOAuthConsumerSecret(
						"*")
				.setOAuthAccessToken(
						"*-*")
				.setOAuthAccessTokenSecret(
						"*");

		AWSCredentials credentials = new PropertiesCredentials(
				TweetGet.class.getResourceAsStream("AwsCredentials.properties"));
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("*");
		dataSource.setPassword("*");
		dataSource.setPort(3306);
		dataSource.setDatabaseName("TwitterDB");
		dataSource
				.setServerName("*.rds.amazonaws.com");
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		final Statement sqlStatement = conn.createStatement();
		
		
		try {
			
			System.out.println();
			System.out.println("===========================================");
			System.out.println("Connecting to Twitter");
			System.out.println("===========================================\n");
			TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
			.getInstance();

	StatusListener listener = new StatusListener() {
		@Override
		public void onStatus(Status status) {

			
			if (status.getLang().equals("en")) {

				String user_id = String.valueOf(status.getUser().getId());
				String created_at = String.valueOf(status.getCreatedAt());
				GeoLocation geo = status.getGeoLocation();

				if (geo != null) {
					String lon = String.valueOf(geo.getLongitude());
            		String lat = String.valueOf(geo.getLatitude());
            		String lon_lat = lon+","+lat;
            		String originaltext = status.getText();
            		String[] words = originaltext.split("\\s");
            		StringBuffer message = new StringBuffer();
            		for(String word:words){
            			if (word.contains("@") || word.contains("#") || word.contains("http")){
            				
            			}
            			else
            			{
            				if(word.contains("'")){
								word = word.replace("'", "");
							}
            				word = word.replaceAll("[^A-Za-z0-9 ]", "");
							message.append(word);
							message.append(" ");
            			}
            		}
					String query = "INSERT INTO tweets (tweet_id,coordinates,created_at,tweet_text) VALUES ('"
							+ user_id
							+ "','"
							+ lon_lat
							+ "','"
							+ created_at
							+ "','"
							+ message.toString()
							+ "')";
					String queueMessage = user_id + "_" + message;
				
					
					try {
						int sqlResult = sqlStatement.executeUpdate(query);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						//sqs.sendMessage(new SendMessageRequest(myQueueUrl,queueMessage));

					
					System.out.println(query);
					System.out.println(message.toString());
					System.out.println(status.getLang());
					System.out.println(status.getGeoLocation()
							.getLatitude()
							+ " "
							+ status.getGeoLocation().getLongitude());
				}
				// }
			}
		}

		@Override
		public void onDeletionNotice(
				StatusDeletionNotice statusDeletionNotice) {
			// System.out.println("Got a status deletion notice id:" +
			// statusDeletionNotice.getStatusId());
		}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			// System.out.println("Got track limitation notice:" +
			// numberOfLimitedStatuses);
		}

		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
			// System.out.println("Got scrub_geo event userId:" + userId +
			// " upToStatusId:" + upToStatusId);
		}

		@Override
		public void onStallWarning(StallWarning warning) {
			System.out.println("Got stall warning:" + warning);
		}

		@Override
		public void onException(Exception ex) {
			ex.printStackTrace();
		}
	};
	twitterStream.addListener(listener);
	twitterStream.sample();
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
}