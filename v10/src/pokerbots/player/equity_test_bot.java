package pokerbots.player;

import java.io.*;
//this file is for testing purposes only
public class equity_test_bot  {
	
	static Card g(String s) {
		return CardUtils.getCardByString(s);
	}
	
	public static void main(String[] args) throws IOException{
		Card h1 = g("9h");
		Card h2 = g("7h");
		Card h3 = g("7d");
		
		Card b1 = g("Ad");
		Card b2 = g("As");
		Card b3 = g("2s");
		Card b4 = g("Qh");
		Card b5 = g("Qd");
		
		Card[] hand1 = {h1, h2, h3};
		
		Card[] board = new Card[5];
//	 	board[0] = b1;
//		board[1] = b2;
//		board[2] = b3;
//		board[3] = b4;
//		board[4] = b5;
		
//		String[] str = {
//			"NEWGAME v2 v3 400 2 2000 110.000000",
//			"NEWHAND 1 false 8s 5h 9s 0 0 110.000000",
//			"GETACTION 145 0 3 POST:1:v3 POST:2:v2 RAISE:143:v3 3 FOLD CALL RAISE:284:400 110.0",
//			"GETACTION 684 0 2 RAISE:284:v2 RAISE:400:v3 2 FOLD CALL 109.997614621",
//			"GETACTION 800 3 9c 3d Qd 2 CALL:v2 DEAL:FLOP 1 DISCARD 109.99738905",
//			"HANDOVER -400 400 5 9c 3d Qd Td 2h 5 DEAL:TURN DEAL:RIVER SHOW:Qh:Ac:v3 SHOW:8s:9s:v2 WIN:800:v3 109.95737789799999",
//			"NEWHAND 2 true 4c 7d 8c -400 400 109.957378",
//			"GETACTION 3 0 2 POST:1:v2 POST:2:v3 3 CALL FOLD RAISE:4:400 109.95737789799999",
//			"GETACTION 97 0 2 RAISE:33:v2 RAISE:64:v3 3 FOLD CALL RAISE:95:400 109.95687785999999",
//			"HANDOVER -433 433 0 3 FOLD:v2 REFUND:31:v3 WIN:66:v3 109.95657583799999",
//			"NEWHAND 1 false 6s 6d Tc -433 433 109.956576",
//			"GETACTION 115 0 3 POST:1:v3 POST:2:v2 RAISE:113:v3 3 FOLD CALL RAISE:224:400 109.95657583799999",
//			"GETACTION 559 0 2 RAISE:224:v2 RAISE:335:v3 3 FOLD CALL RAISE:400:400 109.956259768",
//			"GETACTION 800 3 3c 7c 9d 3 RAISE:400:v2 CALL:v3 DEAL:FLOP 1 DISCARD 109.956015557",
//			"HANDOVER -33 33 5 3c 7c 9d 5s 9h 5 DEAL:TURN DEAL:RIVER SHOW:Jc:Ac:v3 SHOW:6s:6d:v2 WIN:800:v2 109.937106499",
//			"NEWHAND 4 true Jd Qd 4c -33 33 109.937106",
//			"GETACTION 3 0 2 POST:1:v2 POST:2:v3 3 CALL FOLD RAISE:4:400 109.937106499",
//			"HANDOVER -31 31 0 4 RAISE:73:v2 FOLD:v3 REFUND:71:v2 WIN:4:v2 109.9368477" 
//		};
//		String str1 = "NEWGAME P1 P2 200 2 1000 20.0";
//		String str2 = "NEWHAND 1 true Ad Ac Td 0 0 20.0";
//		String str3 = "GETACTION 3 0 2 POST:1:P1 POST:2:P2 3 CALL FOLD RAISE:5:200 20.0";
//		String str4 = "GETACTION 400 3 6d 7s Jh 3 Raise:200:P1 CALL:P1 DEAL:FLOP 1 DISCARD 20.0";
		
//		Player p = new Player(null, null);
//		for (String line : str)
//			p.processInput(line)
		System.out.println(HelperUtils.linInterp(0.588, 0.5, 0.55, 0.1, 0.25));
		
		System.out.println(HelperUtils.logisticSmall(3.0, 3.0, 5.0));

		System.out.println(HelperUtils.logistic(100.0, 100.0, 20.0));
//		BufferedReader input = new BufferedReader(new FileReader("v3.2.dump"));
//		Player p = new Player(null, input);
//		p.run();
//		
//	
//		long time1 = System.nanoTime();
//		
//		EquityCalculator e1 = new EquityCalculator(hand1, board, 1, 1, false);
//		System.out.println("\nEquity hand1 : ");
//		System.out.println(e1.calculateTotalEquity());
//		long time2 = System.nanoTime();
//		
//		System.out.println("Elapsed time: " + (time2-time1)/Math.pow(10, 9));
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
