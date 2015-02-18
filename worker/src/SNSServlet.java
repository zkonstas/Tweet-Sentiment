import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Servlet implementation class SNSServlet
 */
public class SNSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger( SNSServlet.class.getName() );

    /**
     * Default constructor. 
     */
    public SNSServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
//	public void init(ServletConfig config) throws ServletException {
//		// TODO Auto-generated method stub
//	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("hello");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		       sc.close();
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
				
		scan.close();
	}
	
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
	
	private int saveSentiment(String id, String sentiment) {
		
		
//		Class.forName("com.mysql.jdbc.Driver").newInstance();

		// Open new connection.
		java.sql.Connection conn;
		/* To connect to the database, you need to use a JDBC url with the following 
		   format ([xxx] denotes optional url components):
		   jdbc:mysql://[hostname][:port]/[dbname][?param1=value1][&param2=value2]... 
		   By default MySQL's hostname is "localhost." The database used here is 
		   called "mydb" and MySQL's default user is "root". If we had a database 
		   password we would add "&password=xxx" to the end of the url.
		*/
		
		try {
			conn = DriverManager.getConnection("<db-url>");
			Statement sqlStatement = conn.createStatement();

			// Generate the SQL query.
			String query = "UPDATE obama SET sentiment=\""+sentiment+"\" WHERE tweet_id=\""+id+"\"";

			// Get the query results and display them.
			int result = sqlStatement.executeUpdate(query);
			log.info("Result from saving sentiment is"+result);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
