import java.io.IOException;
import java.sql.SQLException;

import twitter4j.TwitterException;


public class testLocally {

	public static void main(String[] args){
		TweetGet tg = new TweetGet();
		try {
			tg.run();
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
