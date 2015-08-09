package WoWQueueNotifier_Client.main;


import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WoWQueueNotifierClient {
	
	private static final String MESSAGE_SHUTDOWN = "shutdown";
	private static final String MESSAGE_POPPED = "triggered";
	private static final String MESSAGE_RETURNED = "triggered";
	private static final String MESSAGE_TEST= "test";
	
	private static final String SERVER_IP = "192.168.56.1";
	private static String serverIp = SERVER_IP;
	private static final int SERVER_PORT = 34710;
	private static int serverPort = SERVER_PORT;

	
	public static void main(String[] args) {
		WoWQueueNotifierClient client = new WoWQueueNotifierClient();
		client.connectToServerAndLoop();
	}
	
	private void connectToServerAndLoop() {
		try (Socket s = new Socket(serverIp, serverPort);
			PrintWriter toServer  = new PrintWriter(s.getOutputStream());
			Scanner fromServer = new Scanner(s.getInputStream());) {
			while (fromServer.hasNext()) {
				switch(fromServer.next()) {
				case MESSAGE_SHUTDOWN : System.out.println("server requested shutdown - bye!"); System.exit(0);
				break;
				case MESSAGE_POPPED : System.out.println("a queue has popped - run away little girl!");
				break;
				}
			}
		} catch(Exception e) {
			System.out.println("connection lost");
		}	
		System.out.println("server shutdown => shutting down client...");
	}
	
}
