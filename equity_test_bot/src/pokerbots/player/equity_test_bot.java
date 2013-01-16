package pokerbots.player;

//this file is for testing purposes only
public class equity_test_bot {
	
	static Card g(String s) {
		return CardUtils.getCardByString(s);
	}
	
	public static void main(String[] args) {
		Card h1 = g("Kc");
		Card h2 = g("Kd");
		Card h3 = g("5h");
		
		Card b1 = g("4s");
		Card b2 = g("5s");
		Card b3 = g("6s");
		Card b4 = g("Qh");
		Card b5 = g("Qd");
		
		Card[] hand1 = {h1, h2};
		Card[] board = new Card[5];

		board[0] = b1;
		board[1] = b2;
		board[2] = b3;
		//board[3] = b4;
		//board[4] = b5;
	
		long time1 = System.nanoTime();
		
		EquityCalculator e1 = new EquityCalculator(hand1, board);
		System.out.println("\nEquity hand1 : ");
		System.out.println(e1.calculateTotalEquity());
		
		long time2 = System.nanoTime();
		
		System.out.println("Elapsed time: " + (time2-time1)/Math.pow(10, 9));
//		
//		System.out.println("\nFast method:");
//		System.out.println(EquityUtils.getEquityThreeCardHand(hand1));
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
