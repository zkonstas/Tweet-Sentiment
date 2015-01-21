import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

class SentimentAnalysis {
    public static String analysis(String args) throws IOException, SAXException,
            ParserConfigurationException, XPathExpressionException {
        // Create an AlchemyAPI object.
        AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromString("<API-KEY>");
        

        // Extract sentiment for a text string.
        Document doc = alchemyObj.TextGetTextSentiment(
            args);
         return (getStringFromDocument(doc));
	
}

    // utility method
    private static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            String[] sentiment = writer.toString().split("\\n");
            
            return sentiment[6].replace("<type>", "").replace("</type>", "").trim();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
