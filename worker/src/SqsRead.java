import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class SqsRead {
	public static void run() throws SQLException,
			InterruptedException, XPathExpressionException, IOException, SAXException, ParserConfigurationException {
		AWSCredentials credentials = new PropertiesCredentials(
   			 SqsRead.class.getResourceAsStream("AwsCredentials.properties"));
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
		String myQueueUrl = "<QUEUE-URL>";
		
		//Initialize SNS
		AmazonSNSClient snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());		                           
		snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
		String topicArn = "<TOPIC-NAME>";
		
		while(true){
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
	        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
	        System.out.println(messages.size());
	        String notificationMsg="";
	        for (Message message : messages) {
	            System.out.println("  Message");
	            System.out.println("  MessageId:     " + message.getMessageId());
	            System.out.println("  ReceiptHandle: " + message.getReceiptHandle());
	            System.out.println("  MD5OfBody:     " + message.getMD5OfBody());
	            System.out.println("  Body:          " + message.getBody());
	            String[] tokens = message.getBody().split("_");
	            StringBuffer sb = new StringBuffer();
	            for (int i=1 ; i<=tokens.length ; i++){
	            	sb.append(tokens[i]);
	            	sb.append(" ");
	            }
	            String sentimentResult = SentimentAnalysis.analysis(sb.toString());
	            System.out.println(sentimentResult);
	            for (Entry<String, String> entry : message.getAttributes().entrySet()) {
	                System.out.println("  Attribute");
	                System.out.println("    Name:  " + entry.getKey());
	                System.out.println("    Value: " + entry.getValue());
	            }
	            notificationMsg = tokens[0]+"_"+sentimentResult;
	            sendMessage(topicArn, snsClient, notificationMsg);
	        }
	        System.out.println(notificationMsg);
	        System.out.println();
	        System.out.println("Deleting a message.\n");
	        String messageRecieptHandle = messages.get(0).getReceiptHandle();
	        sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageRecieptHandle));
	        Thread.currentThread().sleep(10000);
            //return notificationMsg;

		}
	}
	
private static void sendMessage(String topicArn, AmazonSNSClient snsClient, String msg) {
		
		//publish to an SNS topic
		PublishRequest publishRequest = new PublishRequest(topicArn, msg);
		PublishResult publishResult = snsClient.publish(publishRequest);
		//print MessageId of message published to SNS topic
		System.out.println("MessageId - " + publishResult.getMessageId());
		
	}
	

}
