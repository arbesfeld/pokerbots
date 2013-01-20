package pokerbots.player;

//this file is for testing purposes only
public class equity_test_bot {
	
	static Card g(String s) {
		return CardUtils.getCardByString(s);
	}
	
	public static void main(String[] args) {
		Card h1 = g("5s");
		Card h2 = g("Ad");
		Card h3 = g("9c");
		
		Card b1 = g("2d");
		Card b2 = g("Ks");
		Card b3 = g("Qs");
		Card b4 = g("Kh");
		Card b5 = g("Qc");
		
		Card[] hand1 = {h1, h2};
		Card[] board = new Card[5];

		board[0] = b1;
		board[1] = b2;
		board[2] = b3;
		//board[3] = b4;
//		//board[4] = b5;
//		String str1 = "NEWGAME P1 P2 200 2 1000 20.0";
//		String str2 = "NEWHAND 1 true Ad Ac Td 0 0 20.0";
//		String str3 = "GETACTION 3 0 2 POST:1:P1 POST:2:P2 3 CALL FOLD RAISE:5:200 20.0";
//		String str4 = "GETACTION 400 3 6d 7s Jh 3 Raise:200:P1 CALL:P1 DEAL:FLOP 1 DISCARD 20.0";
//		Player p = new Player(null, null);
//		p.processInput(str1);
//		p.processInput(str2);
//		p.processInput(str3);
//		p.processInput(str4);
//		
		long time1 = System.nanoTime();
//		
		EquityCalculator e1 = new EquityCalculator(hand1, board);
		System.out.println("\nEquity hand1 : ");
		System.out.println(e1.calculateTotalEquity());
		
		long time2 = System.nanoTime();
//		
		System.out.println("Elapsed time: " + (time2-time1)/Math.pow(10, 9));
////		
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
