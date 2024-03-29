package pokerbots.player;

import java.io.*;
//this file is for testing purposes only
public class equity_test_bot  {
	static Card g(String s) {
		return CardUtils.getCardByString(s);
	}
	
	public static void main(String[] args) throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter("suited.txt"));
		
		//suited pairs
		
		Card[] hand = new Card[3];

		long startingTime = System.nanoTime();
		
		for(int i = 2; i <= 14; i++) {
			hand[0] = new Card(i, 0);
			
			for(int j = i+1; j <= 14; j++) {
				hand[1] = new Card(j, 0);

				long time1 = System.nanoTime();
				
				for(int k = j+1; k <= 14; k++) {
					hand[2] = new Card(k, 0);
					EquityCalculator ec = new EquityCalculator(hand, null);
					
					double equity = ec.calculateTotalEquity();
					char a = 2, b = 2, c = 2;
					
					if(i == 10) 
						a = 'T';
					else if(i == 11)
						a = 'J';
					else if(i == 12) 
						a = 'Q';
					else if(i == 13) 
						a = 'K';
					else if(i == 14) 
						a = 'A';
					else
						a = Integer.toString(i).charAt(0);
					
					if(j == 10) 
						b = 'T';
					else if(j == 11)
						b = 'J';
					else if(j == 12) 
						b = 'Q';
					else if(j == 13) 
						b = 'K';
					else if(j == 14) 
						b = 'A';
					else
						b = Integer.toString(j).charAt(0);
					
					if(k == 10) 
						c = 'T';
					else if(k == 11)
						c = 'J';
					else if(k == 12) 
						c = 'Q';
					else if(k == 13) 
						c = 'K';
					else if(k == 14) 
						c = 'A';
					else
						c = Integer.toString(k).charAt(0);
					//System.out.println(a);
					out.write(Character.toString(a) + Character.toString(b) + Character.toString(c) + ":" + equity + ",");

					System.out.println(Character.toString(a) + Character.toString(b) + Character.toString(c) + ":" + equity + ",");

				}

				long time2 = System.nanoTime();
				double timeThisSet = (time2-time1)/Math.pow(10, 9);
				double totalTime = (time2-startingTime)/Math.pow(10, 9);
				double projectedTotalTime = timeThisSet * 13 * 13 / 2;
				System.out.println("Elapsed time: " + timeThisSet);
				System.out.println("Time elapsed: " + totalTime);
				System.out.println("Time remaining: " + (projectedTotalTime - totalTime));
//				out.close();
			}
			
		}
		out.close();
	}
	
	static void printBoard(Card[] board) {
		System.out.println("\nBoard: ");
		for(int i = 0; i < board.length; i++) {
			if(board[i] == null)
				return;
			board[i].print();
		}
	}
}
