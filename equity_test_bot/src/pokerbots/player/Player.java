package pokerbots.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Player {
	
	private final PrintWriter outStream;
	private final BufferedReader inStream;
	
	//instantiated every match
	private String myName, oppName;
	private int stackSize, bb;
	private double timeBank;
	
	//instantiated every hand
	private int handId, myBank, oppBank;
	private boolean button;
	private Card[] hand;
	
	//instantiated every action
	private int potSize, numBoardCards, numLastActions, numLegalActions;
	private Card[] board;
	private Action[] lastActions, legalActions;
	
	public Player(PrintWriter output, BufferedReader input) {
		this.outStream = output;
		this.inStream = input;
	}
	
	public void run() {
		String input;
		try {
			// Block until engine sends us a packet; read it into input.
			while ((input = inStream.readLine()) != null) {

				// Here is where you should implement code to parse the packets
				// from the engine and act on it.
				System.out.println(input);

				processInput(input);
				
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}

		System.out.println("Gameover, engine disconnected");
		
		// Once the server disconnects from us, close our streams and sockets.
		try {
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			System.out.println("Encounterd problem shutting down connections");
			e.printStackTrace();
		}
	}
	
	private void processInput(String input) {
		String[] tokens = input.split(" ");
		String word = tokens[0];
		
		if ("NEWGAME".compareToIgnoreCase(word) == 0) {
			myName =    tokens[1];
			oppName =   tokens[2];
			stackSize = Integer.parseInt(tokens[3]);
			bb =        Integer.parseInt(tokens[4]);
			timeBank =  Double.parseDouble(tokens[5]);
			
			//newGame();
		} 
		
		else if ("KEYVALUE".compareToIgnoreCase(word) == 0) {
			//pass
		}
		
		else if ("NEWHAND".compareToIgnoreCase(word) == 0) {
			handId = Integer.parseInt(tokens[1]);
			button = Boolean.parseBoolean(tokens[2]);
			
			hand = new Card[3];
			hand[0] = CardUtils.getCardByString(tokens[3]);
			hand[1] = CardUtils.getCardByString(tokens[4]);
			hand[2] = CardUtils.getCardByString(tokens[5]);
		
			board = new Card[5];
			
			myBank = Integer.parseInt(tokens[6]);
			oppBank = Integer.parseInt(tokens[7]);
			timeBank = Double.parseDouble(tokens[8]);
			
			//newHand();
		}
		
		else if ("GETACTION".compareToIgnoreCase(word) == 0) {
			potSize = Integer.parseInt(tokens[1]);
			
			numBoardCards = Integer.parseInt(tokens[2]);
			int i = 3;
			for( ; i < numBoardCards + 3; i++)
				board[i - 3] = CardUtils.getCardByString(tokens[i]);
			
			numLastActions = Integer.parseInt(tokens[i]);
			int j = i+1;
			for( ; j < numLastActions + i; j++) 
				lastActions[j - i] = ActionUtils.getActionByString(tokens[j]);
			
			numLegalActions = Integer.parseInt(tokens[j]);
			int k = j+1;
			for( ; k < numLegalActions + j; k++)
				legalActions[k - j] = ActionUtils.getActionByString(tokens[k]);
			
			timeBank = Integer.parseInt(tokens[k]);
			
			//performAction();
		}
		
		else if ("HANDOVER".compareToIgnoreCase(word) == 0) {
		}
		
		else if ("REQUESTKEYVALUES".compareToIgnoreCase(word) == 0) {
			// At the end, engine will allow bot to send key/value pairs to store.
			// FINISH indicates no more to store.
			outStream.println("FINISH");
		}
	}
}
