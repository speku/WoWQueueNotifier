package WoWQueueNotifier.main;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Scanner;

public class WoWQueueNotifier {
	
	private static final String SHUTDOWN = "shutdown";
	private static final String TRIGGERED = "triggered";
	private Robot robot;
	private static final int[] XYWH = {0, 0, 5, 5};
	private static final Rectangle CAPTURE_SIZE = new Rectangle(XYWH[0], XYWH[1], XYWH[2], XYWH[3]);
	private static final int FREQUENCY = 2000;
	private static int[] searchMask = new int[XYWH[2] * XYWH[3]];
	private BufferedImage buffer;
	private int[] capturedMask;
	private PrintWriter toClient;
	private Scanner fromClient;
	private ServerSocket socket;
	private static final int SERVER_PORT = 34710;
	
	public static void main(String[] args) {
		WoWQueueNotifier q = new WoWQueueNotifier();
		q.run();

		
	}
	
	private void run() {
		buildSearchMask();
		createRobot();
		if (clientIsReady()){
			loop();
		}
	}
	
	private void createRobot() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
		}
	}
	
	private void buildSearchMask() {
		int count = 1;
		int trackCount = 1;
		boolean isBlack = true;
//		for (int i = 0; i < XYWH[2] * XYWH[3]; i++) {
//			searchMask[i] = isBlack ? new Color(0, 0, 0).getRGB() : new Color(255, 255, 255).getRGB();
//			--count;
//			if (count == 0) {
//				isBlack = !isBlack;
//				count = ++trackCount;
//			}
//		}
		for (int i = 0; i < XYWH[2] * XYWH[3]; i++) {
			searchMask[i] = new Color(0, 0, 0).getRGB();
		}
	}
	
	private boolean clientIsReady() {
		try {
			System.out.println("establishing connection to client...");
			socket = new ServerSocket(SERVER_PORT);
			System.out.println("waiting for client request...");
			Socket cs = socket.accept();
			toClient = new PrintWriter(cs.getOutputStream(), true);
			fromClient = new Scanner(cs.getInputStream());
			System.out.println("connection to client established...");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnections();
			System.out.println("couldn't connect to client");
			return false;
		}
	}
	

	
	private boolean hasTriggered() {
		buffer = robot.createScreenCapture(CAPTURE_SIZE);
		capturedMask = buffer.getRGB(0, 0, XYWH[2], XYWH[3], null, 0, XYWH[2]);
		return masksAreEqual(searchMask, capturedMask);
		
		
	}
	
	private void loop() {
//		listenToClient();
		System.out.println("waiting for queue to pop...");
		while (true) {
			try {
				Thread.sleep(FREQUENCY);
				if (hasTriggered()) {
//					System.out.println("server has triggered");
					toClient.println(TRIGGERED);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean masksAreEqual(int[] m1, int[] m2) {
		if (m1.length == m2.length){
			for (int i = 0; i < m1.length; i++) {
				if (m1[i] != m2[i]) {
					System.out.println("masks not equal");
					return false;
				}
			}
//			System.out.println("masks equal");
			return true;
		}
//		System.out.println("masks not equal");
		return false;
	}
	
	public void closeConnections() {
		if (toClient != null) toClient.close();
		if (fromClient != null) fromClient.close();
		if (socket != null)
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private void listenToClient() {
		System.out.println("listening for client shutdown requests...");
		new Thread(){
			public void run() {
				String message;
				while((message = fromClient.nextLine()) != null){
					switch(message) {
					case SHUTDOWN : System.out.println("client requested shutdown - bye!"); closeConnections(); System.exit(0);
					break;
					}
				}
			}
		}.start();
	}
}
