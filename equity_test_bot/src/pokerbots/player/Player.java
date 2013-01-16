package pokerbots.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Player {
	private final PrintWriter outStream;
	private final BufferedReader inStream;
	
	private double timeBank; //keep in player
	
	private Brain brain;
	private Historian maj;
	
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
			String myName =    tokens[1];
			String oppName =   tokens[2];
			int stackSize =    Integer.parseInt(tokens[3]);
			int bb =           Integer.parseInt(tokens[4]);
			
			timeBank =  Double.parseDouble(tokens[5]);
			
			maj = new Historian(myName, oppName, stackSize, bb);
			//newGame();
		} 
		
		else if ("KEYVALUE".compareToIgnoreCase(word) == 0) {
			//pass
		}
		
		else if ("NEWHAND".compareToIgnoreCase(word) == 0) {
			brain = new Brain(maj);
			
			brain.handId = Integer.parseInt(tokens[1]);
			brain.button = Boolean.parseBoolean(tokens[2]);
			
			brain.hand = new Card[3];
			brain.hand[0] = CardUtils.getCardByString(tokens[3]);
			brain.hand[1] = CardUtils.getCardByString(tokens[4]);
			brain.hand[2] = CardUtils.getCardByString(tokens[5]);
		
			brain.board = new Card[5];
			
			brain.myBank = Integer.parseInt(tokens[6]);
			brain.oppBank = Integer.parseInt(tokens[7]);
			
			timeBank = Double.parseDouble(tokens[8]);
			
			//newHand();
		}
		
		else if ("GETACTION".compareToIgnoreCase(word) == 0) {
			brain.potSize = Integer.parseInt(tokens[1]);
			
			brain.numBoardCards = Integer.parseInt(tokens[2]);
			int i = 3;
			for( ; i < brain.numBoardCards + 3; i++)
				brain.board[i - 3] = CardUtils.getCardByString(tokens[i]);
			
			brain.numLastActions = Integer.parseInt(tokens[i]);
			int j = i+1;
			for( ; j < brain.numLastActions + i; j++) 
				brain.lastActions[j - i] = ActionUtils.getPerformedActionByString(tokens[j]);
			
			brain.numLegalActions = Integer.parseInt(tokens[j]);
			int k = j+1;
			for( ; k < brain.numLegalActions + j; k++)
				brain.legalActions[k - j] = ActionUtils.getLegalActionByString(tokens[k]);
			
			timeBank = Integer.parseInt(tokens[k]);
			
			//brain.act();
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
