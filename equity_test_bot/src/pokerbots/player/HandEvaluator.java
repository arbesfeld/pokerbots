package pokerbots.player;

import java.util.*;

public class HandEvaluator {
	private static Card[] hand, board;
	private static int[] valFreq;
	
	/* Returns actual value of complete hand (7 cards) as a "HandRank"
	 * Final two values are the sorted hand cards
	   [0, 0, 0] - No pair
	   [1, *, 0] - One pair with value *
	   [2, *, #] - Two pair with values * and # (ordered)
	   [3, *, 0] - Three of a kind with value *
	   [4, *, 0] - Straight with high card *
	   [5, 0, 0] - Flush 
	   [6, *, #] - Full house with 3 * and 2 #
	   [7, *, 0] - Four of a kind with value *
	   [8, *, 0] - Straight flush with high card * 
	 */
	public static HandRank getHandRank(Card[] h, Card[] b) {
		hand = h;
		board = b;
		
	    valFreq = new int[15];
		Arrays.fill(valFreq, 0);
		
		for(int i = 0; i < 2; i++) { 
			valFreq[hand[i].val]++;
			if(hand[i].val == 14)
				valFreq[1] = 1;
		}
		for(int i = 0; i < 5; i++) { 
			valFreq[board[i].val]++;
			if(board[i].val == 14)
				valFreq[1] = 1;
		}
		
		//kind[4] is the value of the four of a kind, kind[3] is the value of the three of a kind
	    //kind[2] and kind[1] are both pairs
	    int[] kind = new int[5];
	    Arrays.fill(kind, 0);
	    
		for(int i = 14; i >= 2; i--) {
			if(valFreq[i] > 1) {
				if(valFreq[i] == 3 && kind[3] != 0)
					kind[2] = i;
				else if(valFreq[i] == 2) {
					if(kind[2] == 0)
						kind[2] = i;
					else if(kind[2] != 0 && kind[1] == 0)
						kind[1] = i;
				}
				kind[valFreq[i]] = Math.max(kind[valFreq[i]], i);
			}
		}
		
		int flush = flush();
	    int straight = straight();
		int straightFlush = -1;
		int topVal = 0, botVal = 0;
		if(hand[0].compareTo(hand[1]) > 0) {
			topVal = hand[0].val;
			botVal = hand[1].val;
		}
		else {
			topVal = hand[1].val;
			botVal = hand[0].val;
		}
		
	    if(flush != -1)
	    	straightFlush = straightFlush(flush);
	    
		if(straightFlush != -1)                       //straight flush
			return new HandRank(8, straight, 0, topVal, botVal);
		else if(kind[4] != 0)                         //four of a kind
			return new HandRank(7, kind[4], 0, topVal, botVal); 
		else if(kind[3] != 0 && kind[2] != 0)         //full house
			return new HandRank(6, kind[3], kind[2], topVal, botVal);
		else if(flush != -1)                          //flush
			return new HandRank(5, 0, 0, topVal, botVal);
		else if(straight != -1)                       //straight
			return new HandRank(4, straight, 0, topVal, botVal);
		else if(kind[3] != 0)                         //three of a kind
			return new HandRank(3, kind[3], 0, topVal, botVal);
		else if(kind[2] != 0 && kind[1] != 0)         //two pair
			return new HandRank(2, kind[2], kind[1], topVal, botVal);
		else if(kind[2] != 0)                         //one pair
			return new HandRank(1, kind[2], 0, topVal, botVal);
		else
			return new HandRank(0, 0, 0, topVal, botVal);
			
	}
	
	//straight with suit of suit
	private static int straightFlush(int suit) {
		int[] straightFlushFreq = new int[15];
		Arrays.fill(straightFlushFreq, 0);
		
		for(int i = 0; i < 2; i++) {
			if(hand[i].suit == suit) {
				straightFlushFreq[hand[i].val]++;
				if(hand[i].val == 14)
					straightFlushFreq[1] = 1;
			}
		}
		for(int i = 0; i < 5; i++) { 
			if(board[i].suit == suit) {
				straightFlushFreq[board[i].val]++;
				if(board[i].val == 14)
					straightFlushFreq[1] = 1;
			}
		}
		int consecutive = 0;
		for(int i = 14; i >= 1; i--) {
			if(straightFlushFreq[i] > 0)
				consecutive++;
			else
				consecutive = 0;
			if(consecutive == 5) {
				return i+4;
			}
		}
		return -1;
	}
	
	//returns -1 if not flush, suit of flush otherwise
	private static int flush() {
		int[] suitFreq = new int[4];
		Arrays.fill(suitFreq, 0);
		
		for(int i = 0; i < 2; i++) 
			suitFreq[hand[i].suit]++;
		for(int i = 0; i < 5; i++) 
			suitFreq[board[i].suit]++;
		
		
		if(suitFreq[0] >= 5)
			return 0;
		else if(suitFreq[1] >= 5)
			return 1;
		else if(suitFreq[2] >= 5)
			return 2;
		else if(suitFreq[3] >= 5)
			return 3;
		else
			return -1;
	}
	
	//returns value of highest card if straight, -1 if not straight
	private static int straight() {
		int consecutive = 0;
		for(int i = 14; i >= 1; i--) {
			if(valFreq[i] > 0)
				consecutive++;
			else
				consecutive = 0;
			if(consecutive == 5) {
				return i+4;
			}
		}
		return -1;
	}
}
