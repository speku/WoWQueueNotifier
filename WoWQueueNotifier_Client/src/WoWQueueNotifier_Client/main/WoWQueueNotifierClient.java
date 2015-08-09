package WoWQueueNotifier_Client.main;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class WoWQueueNotifierClient {
	
	private static final String SHUTDOWN = "shutdown";
	private static final String TRIGGERED = "triggered";
	private static final String SERVER_IP = "192.168.56.1";
	private static String serverIp = SERVER_IP;
	private static final int SERVER_PORT = 34710;
	private static int serverPort = SERVER_PORT;
	private PrintWriter toServer;
	private Scanner fromServer;
	private Socket socket;
	
	public static void main(String[] args) {
		WoWQueueNotifierClient client = new WoWQueueNotifierClient();
		client.connectToServerAndLoop();
	}
	
	public void run() {
		connectToServerAndLoop();
//		if (connectToServer()) {
//			loop();
//		}
	}
	
//	private boolean connectToServer() {
//		try {
//			socket = new Socket(serverIp, serverPort);
//			toServer  = new PrintWriter(socket.getOutputStream());
//			fromServer = new Scanner(socket.getInputStream());
//			return true;
//			} catch (Exception e) {
//				e.printStackTrace();
//				closeConnections();
//				return false;
//			}
//	}
//	
//	public void closeConnections() {
//		if (toServer != null) toServer.close();
//		if (fromServer != null) fromServer.close();
//	}
//	
//	private void loop() {
//		System.out.println("client is looping");
//		while (fromServer.hasNext()) {
//			System.out.println("got something client");
//			switch(fromServer.next()) {
//			case SHUTDOWN : System.out.println("server requested shutdown - bye!"); closeConnections(); System.exit(0);
//			break;
//			case TRIGGERED : System.out.println("a queue has popped - run away little girl!");
//			break;
//			}
//		}
//	}
	
	private void connectToServerAndLoop() {
		try (Socket s = new Socket(serverIp, serverPort);
			PrintWriter toServer  = new PrintWriter(s.getOutputStream());
			Scanner fromServer = new Scanner(s.getInputStream());) {
			System.out.println("client is looping");
			while (fromServer.hasNext()) {
				System.out.println("got something client");
				switch(fromServer.next()) {
				case SHUTDOWN : System.out.println("server requested shutdown - bye!"); System.exit(0);
				break;
				case TRIGGERED : System.out.println("a queue has popped - run away little girl!");
				break;
				}
			}
		} catch(Exception e) {
			System.out.println("connection lost");
		}
		
			
	}
	
	public void closeConnections() {
		if (toServer != null) toServer.close();
		if (fromServer != null) fromServer.close();
	}
	
	private void loop() {
		System.out.println("client is looping");
		while (fromServer.hasNext()) {
			System.out.println("got something client");
			switch(fromServer.next()) {
			case SHUTDOWN : System.out.println("server requested shutdown - bye!"); closeConnections(); System.exit(0);
			break;
			case TRIGGERED : System.out.println("a queue has popped - run away little girl!");
			break;
			}
		}
	}
	
	
	
}
