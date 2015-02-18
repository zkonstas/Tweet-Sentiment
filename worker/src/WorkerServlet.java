
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.xml.sax.SAXException;

/**
 * An example Amazon Elastic Beanstalk Worker Tier application. This example
 * requires a Java 7 (or higher) compiler.
 */
public class WorkerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    String topicArn;
    AmazonSNSClient snsClient;
    
    
    public void init(ServletConfig config) throws ServletException {
    	//Initialize SNS
    	snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());		                           
    	snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    	topicArn = "<CREATED_TOPIC>";
	}
    
    /**
     * This method is invoked to handle POST requests from the local
     * SQS daemon when a work item is pulled off of the queue. The
     * body of the request contains the message pulled off the queue.
     */
    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response)
            throws ServletException, IOException {
    	
    	System.out.println("got message from queue1");

        try {

            // Parse the work to be done from the POST request body.
            
//            WorkRequest workRequest = WorkRequest.fromJson(request.getInputStream());
        	
        	StringBuffer jb = new StringBuffer();
        	  String line = null;
        	  try {
        		  System.out.println("trying to read");
        	    BufferedReader reader = request.getReader();
        	    while ((line = reader.readLine()) != null)
        	      jb.append(line);
        	  } catch (Exception e) { /*report an error*/ }
        	  
        	System.out.println("After getting message from queue");
        	String message = jb.toString();
        	System.out.println(message);
        	processMessage(message);
        	
            response.setStatus(200);

        } catch (RuntimeException exception) {
            
            // Signal to beanstalk that something went wrong while processing
            // the request. The work request will be retried several times in
            // case the failure was transient (eg a temporary network issue
            // when writing to Amazon S3).
            
            response.setStatus(500);
            try (PrintWriter writer =
                 new PrintWriter(response.getOutputStream())) {
                exception.printStackTrace(writer);
            }
        } catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void processMessage(String message) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
    	String[] tokens = message.split("_");
        StringBuffer sb = new StringBuffer();
        
        for (int i=1; i<tokens.length ; i++){
        	sb.append(tokens[i]);
        	sb.append(" ");
        }
        
        String sentimentResult = SentimentAnalysis.analysis(sb.toString());
        
        if(!sentimentResult.equals("positive") || !sentimentResult.equals("negative") || !sentimentResult.equals("neutral")) {
        	sentimentResult = "no_result";
        }

        
        String notificationMsg = tokens[0]+"_"+sentimentResult;
        System.out.println("Message with sentiment:");
        System.out.println(notificationMsg);
        sendMessage(topicArn, snsClient, notificationMsg);
    }
    
    private void sendMessage(String topicArn, AmazonSNSClient snsClient, String msg) {
		
		//publish to an SNS topic
		PublishRequest publishRequest = new PublishRequest(topicArn, msg);
		PublishResult publishResult = snsClient.publish(publishRequest);
		//print MessageId of message published to SNS topic
		System.out.println("MessageId - " + publishResult.getMessageId());
		
	}
    
}
