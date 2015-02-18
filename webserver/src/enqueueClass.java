import java.io.IOException;
import java.sql.SQLException;

public class enqueueClass implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Hello from thread!");
		SqsConnect sqs = new SqsConnect();
		System.out.println("After sqs");
		try {
			sqs.run();
			System.out.println("In try");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
