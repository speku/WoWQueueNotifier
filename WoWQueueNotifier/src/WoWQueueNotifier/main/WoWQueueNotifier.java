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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * Notifies the user of popped World of Warcraft queues by establishing
 * a connection to a client device (smartphone/tablet) and transmitting
 * trigger messages when queue-related ingame events occur.
 * 
 * This is achieved by capturing a small rectangle at the top-left corner
 * of the screen while the game is running. An in-game addon, that has to
 * be run at the same time, creates little textures at this location.
 * These textures encode messages by means of alternation of black and
 * white pixels. Those alternations can be then transcoded to binary
 * values which represent messages for the application to react upon.
 * 
 * @author Moonfuzz@Antonidas
 *
 */
public class WoWQueueNotifier {
	
	private static final int SERVER_PORT = 34710;
	
	private static final String MESSAGE_SHUTDOWN = "shutdown";
	private static final String MESSAGE_POPPED = "triggered";
	private static final String MESSAGE_RETURNED = "triggered";
	private static final String MESSAGE_TEST= "test";
	
	private static final String MASK_CODE_POPPED = "000000001";
	private static final String MASK_CODE_RETURNED = "000000010";
	private static final String MASK_CODE_SHUTDOWN = "000000010";
	private static final String MASK_CODE_TEST = "000000000";
	
	private static final int FREQUENCY_WAITING = 2000;
	private static final int FREQUENCY_POPPED = 100;
	
	private static final int[] XYWH = {0, 0, 3, 3};
	private static final Rectangle CAPTURE_SIZE = new Rectangle(XYWH[0], XYWH[1], XYWH[2], XYWH[3]);
	
	private static int[] MASK_POPPED = new int[XYWH[2] * XYWH[3]];
	private static int[] MASK_RETURNED = new int[XYWH[2] * XYWH[3]];
	private static int[] MASK_SHUTDOWN = new int[XYWH[2] * XYWH[3]];
	private static int[] MASK_TEST = new int[XYWH[2] * XYWH[3]];
	
	private static boolean popped = false;
	private static String state = MASK_CODE_TEST;
	
	private static HashMap<int[], String> masksToMessages= new HashMap<>();
	static {
		masksToMessages.put(MASK_POPPED, MESSAGE_POPPED);
		masksToMessages.put(MASK_RETURNED, MESSAGE_RETURNED);
		masksToMessages.put(MASK_SHUTDOWN, MESSAGE_SHUTDOWN);
		masksToMessages.put(MASK_TEST, MESSAGE_TEST);
		
	}
	
	private static HashMap<String, int[]> codesToMasks = new HashMap<>();
	static {
		codesToMasks.put(MASK_CODE_POPPED, MASK_POPPED);
		codesToMasks.put(MASK_CODE_RETURNED, MASK_RETURNED);
		codesToMasks.put(MASK_CODE_SHUTDOWN, MASK_SHUTDOWN);
		codesToMasks.put(MASK_CODE_TEST, MASK_TEST);
	}
	
	static {
		for (Entry<String, int[]> entry : codesToMasks.entrySet()) {
			buildMask(entry.getKey(), entry.getValue());
		}
	}
	
	
	private Robot robot;
	private BufferedImage buffer;
	private int[] capturedMask;

	
	public static void main(String[] args) {
		WoWQueueNotifier notifier = new WoWQueueNotifier();
		notifier.run();

		
	}
	
	private void run() {
		createRobot();
		waitForClientAndLoop();
	}
	
	private void createRobot() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
		}
	}
	
	private static void buildMask(String maskCode, int[] mask) {
		int bit = 0;
		for (int i = 0; i < mask.length; i++) {
			bit = Integer.parseInt(maskCode.charAt(i)+"");
			mask[i] = bit == 0 ? new Color(0,0,0).getRGB() : new Color(255, 255, 255).getRGB();
		}
	}
	
	private void waitForClientAndLoop() {
		try (ServerSocket ss = new ServerSocket(SERVER_PORT);
			Socket cs = ss.accept();
			PrintWriter toClient = new PrintWriter(cs.getOutputStream(), true);
			Scanner fromClient = new Scanner(cs.getInputStream());){
			listenToClient(fromClient);
			loop(toClient);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("lost connection to client");

		}
	}
	

	
	private boolean hasPopped(String maskCode) {
		buffer = robot.createScreenCapture(CAPTURE_SIZE);
		capturedMask = buffer.getRGB(0, 0, XYWH[2], XYWH[3], null, 0, XYWH[2]);
		return masksAreEqual(codesToMasks.get(maskCode), capturedMask);
		
		
	}
	
	private void loop(PrintWriter toClient) {
		while (true) {
			try {
				Thread.sleep(popped ? FREQUENCY_POPPED : FREQUENCY_WAITING);
				if (hasPopped(state)) {
					toClient.println(MESSAGE_POPPED);
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
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	private void listenToClient(Scanner fromClient) {
		new Thread(){
			public void run() {
				while((fromClient.hasNext())){
					switch(fromClient.next()) {
					case MESSAGE_SHUTDOWN : System.out.println("client requested shutdown - bye!"); System.exit(0);
					break;
					}
				}
			}
		}.start();
	}
}
