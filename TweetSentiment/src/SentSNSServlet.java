import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.sns.model.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet implementation class SentSNSServlet
 */
public class SentSNSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger( SentSNSServlet.class.getName() );
       
	public void init(ServletConfig config) throws ServletException {
		System.out.println("hello from init");
	}
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SentSNSServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		
		System.out.println("get!!");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		System.out.println("post!!");
		
		//Get the message type header.
				String messagetype = request.getHeader("x-amz-sns-message-type");
				//If message doesn't have the message type header, don't process it.
				if (messagetype == null)
					return;

		    // Parse the JSON message in the message body
		    // and hydrate a Message object with its contents 
		    // so that we have easy access to the name/value pairs 
		    // from the JSON message.
		    Scanner scan = new Scanner(request.getInputStream());
		    StringBuilder builder = new StringBuilder();
		    while (scan.hasNextLine()) {
		      builder.append(scan.nextLine());
		    }
				SNSMessage msg = readMessageFromJson(builder.toString());

		    // Process the message based on type.
				if (messagetype.equals("Notification")) {
					//TODO: Do something with the Message and Subject.
					//Just log the subject (if it exists) and the message.
					String logMsgAndSubject = ">>Notification received from topic " + msg.getTopicArn();
					if (msg.getSubject() != null)
						logMsgAndSubject += " Subject: " + msg.getSubject();
					logMsgAndSubject += " Message: " + msg.getMessage();
					log.info(logMsgAndSubject);
				}
		    else if (messagetype.equals("SubscriptionConfirmation"))
				{
		       //TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics 
		       //that you want to enable to add this endpoint as a subscription.
		        	
		       //Confirm the subscription by going to the subscribeURL location 
		       //and capture the return value (XML message body as a string)
		       Scanner sc = new Scanner(new URL(msg.getSubscribeURL()).openStream());
		       StringBuilder sb = new StringBuilder();
		       while (sc.hasNextLine()) {
		         sb.append(sc.nextLine());
		       }
		       log.info(">>Subscription confirmation (" + msg.getSubscribeURL() +") Return value: " + sb.toString());
		       //TODO: Process the return value to ensure the endpoint is subscribed.
				}
		    else if (messagetype.equals("UnsubscribeConfirmation")) {
		      //TODO: Handle UnsubscribeConfirmation message. 
		      //For example, take action if unsubscribing should not have occurred.
		      //You can read the SubscribeURL from this message and 
		      //re-subscribe the endpoint.
		      log.info(">>Unsubscribe confirmation: " + msg.getMessage());
				}
		    else {
		      //TODO: Handle unknown message type.
		      log.info(">>Unknown message type.");
		    }
				log.info(">>Done processing message: " + msg.getMessageId());
		}

//	private byte[] getMessageBytesToSign(SNSMessage msg) {
//
//		byte [] bytesToSign = null;
//		if (msg.getType().equals("Notification"))
//			bytesToSign = buildNotificationStringToSign(msg).getBytes();
//		else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
//			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
//		return bytesToSign;
//	}
//
//	//Build the string to sign for Notification messages.
//	private static String buildNotificationStringToSign( SNSMessage msg) {
//		String stringToSign = null;
//
//		//Build the string to sign from the values in the message.
//		//Name and values separated by newline characters
//		//The name value pairs are sorted by name 
//		//in byte sort order.
//		stringToSign = "Message\n";
//		stringToSign += msg.getMessage() + "\n";
//		stringToSign += "MessageId\n";
//		stringToSign += msg.getMessageId() + "\n";
//		if (msg.getSubject() != null) {
//			stringToSign += "Subject\n";
//			stringToSign += msg.getSubject() + "\n";
//		}
//		stringToSign += "Timestamp\n";
//		stringToSign += msg.getTimestamp() + "\n";
//		stringToSign += "TopicArn\n";
//		stringToSign += msg.getTopicArn() + "\n";
//		stringToSign += "Type\n";
//		stringToSign += msg.getType() + "\n";
//		return stringToSign;
//	}
//
//	//Build the string to sign for SubscriptionConfirmation 
//	//and UnsubscribeConfirmation messages.
//	private static String buildSubscriptionStringToSign(SNSMessage msg) {
//		String stringToSign = null;
//		//Build the string to sign from the values in the message.
//		//Name and values separated by newline characters
//		//The name value pairs are sorted by name 
//		//in byte sort order.
//		stringToSign = "Message\n";
//		stringToSign += msg.getMessage() + "\n";
//		stringToSign += "MessageId\n";
//		stringToSign += msg.getMessageId() + "\n";
//		stringToSign += "SubscribeURL\n";
//		stringToSign += msg.getSubscribeURL() + "\n";
//		stringToSign += "Timestamp\n";
//		stringToSign += msg.getTimestamp() + "\n";
//		stringToSign += "Token\n";
//		stringToSign += msg.getToken() + "\n";
//		stringToSign += "TopicArn\n";
//		stringToSign += msg.getTopicArn() + "\n";
//		stringToSign += "Type\n";
//		stringToSign += msg.getType() + "\n";
//		return stringToSign;
//	}
//
//
	private SNSMessage readMessageFromJson(String string) {
		ObjectMapper mapper = new ObjectMapper(); 
		SNSMessage message = null;
		try {
			message = mapper.readValue(string, SNSMessage.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return message;
	}
//	
//	private TranscoderMessage readTransMesFromJson(String mes) {
//		ObjectMapper mapper = new ObjectMapper(); 
//		TranscoderMessage message = null;
//		try {
//			message = mapper.readValue(mes, TranscoderMessage.class);
//		} catch (JsonParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return message;
//	}
	
	}
