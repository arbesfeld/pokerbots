package pokerbots.player;

//this file is for testing purposes only
public class equity_test_bot {
	public static void main(String[] args) {
		Card h1 = new Card(2, 0);
		Card h2 = new Card(3, 0);
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
		
//		System.out.println("Hand 1: ");
//		for(int i = 0; i < hand1.length; i++) {
//			hand1[i].print();
//		}
		System.out.println("Hand 2: ");
		for(int i = 0; i < hand2.length; i++) {
			hand2[i].print();
		}
		//printBoard(board);
//		System.out.println("\nHandRank Hand 1: ");
//		HandRank rank1 = HandEvaluator.getHandRank(hand1, board);
//		rank1.print();
//		
//		System.out.println("HandRank Hand 2: ");
//		HandRank rank2 = HandEvaluator.getHandRank(hand2, board);
//		rank2.print();
//		
//		System.out.println("\nHand 1 better?");
//		System.out.println(HandComparer.compareHands(hand1, hand2, board));
//	
		long time1 = System.nanoTime();
		EquityCalculator e1 = new EquityCalculator(hand2, null);
		System.out.println("\nEquity hand2: ");
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
