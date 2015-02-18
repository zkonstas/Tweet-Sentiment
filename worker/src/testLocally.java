import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;


public class testLocally {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello from thread!");
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
