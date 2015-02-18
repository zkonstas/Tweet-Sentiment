
import java.io.IOException;
import java.sql.SQLException;

public class test {

	public static void main(String[] args) throws SQLException, InterruptedException, IOException{
		SqsConnect sqs = new SqsConnect();
		try {
			sqs.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
