package pokerbots.player;

//this file is for testing purposes only
public class equity_test_bot {
	
	static Card g(String s) {
		return CardUtils.getCardByString(s);
	}
	
	public static void main(String[] args) {
		Card h1 = g("Js");
		Card h2 = g("6d");
		Card h3 = new Card(4, 3);
		
		Card f1 = new Card(14, 1);
		Card f2 = new Card(13, 2);
		Card f3 = new Card(9, 1);
		
		Card b1 = new Card(4, 0);
		Card b2 = new Card(5, 0);
		Card b3 = new Card(6, 0);
		Card b4 = new Card(13, 0);
		Card b5 = new Card(14, 2);
		
		Card[] hand1 = {h1, h2};
		Card[] hand2 = {f1, f2};
		Card[] board = new Card[5];
		
		//board[0] = b1;
		//board[1] = b2;
		//board[2] = b3;
		//board[3] = b4;
		//board[4] = b5;
		//Card[] board = {b1, b2, b3};// b4, b5};
	
//		System.out.println("\nHand 1 better?");
//		System.out.println(HandComparer.compareHands(hand1, hand2, board));
	
		long time1 = System.nanoTime();
		
		EquityCalculator e1 = new EquityCalculator(hand1, null);
		System.out.println("\nEquity hand1: ");
		System.out.println(e1.calculateTotalEquity());
		
		long time2 = System.nanoTime();
		System.out.println("Elapsed time: " + (time2-time1)/Math.pow(10, 9));
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
