import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;


public class DequeueClass implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Hello from Dequeue thread!");
		SqsRead sqs = new SqsRead();
		try {
			sqs.run();
		} catch (XPathExpressionException | SQLException | InterruptedException
				| IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
}
