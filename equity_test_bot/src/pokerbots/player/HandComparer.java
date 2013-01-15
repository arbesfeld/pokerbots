package pokerbots.player;

//hand is 2 card hand, opponentHand is 2 card hand, board is 5 cards
public class HandComparer {
	//returns true if hand is stronger or "equal" to opponentHand, false otherwise
	public static boolean compareHands(Card[] hand, Card[] opponentHand, Card[] board) {
		HandRank rank1 = HandEvaluator.getHandRank(new Card[]{hand[0], hand[1]}, board);
		HandRank rankOpponent = HandEvaluator.getHandRank(opponentHand, board);
		
		//our hand is better or equal
		if(rank1.compareTo(rankOpponent) >= 0)
			return true;
		else
			return false;
		
	}
	
	public static boolean compareHands3CardHand(Card[] hand, Card[] opponentHand, Card[] board) {
		HandRank rank1 = HandEvaluator.getHandRank(new Card[]{hand[0], hand[1]}, board);
		HandRank rank2 = HandEvaluator.getHandRank(new Card[]{hand[0], hand[2]}, board);
		HandRank rank3 = HandEvaluator.getHandRank(new Card[]{hand[1], hand[2]}, board);
		
		HandRank rankOpponent = HandEvaluator.getHandRank(opponentHand, board);
		
		//our hand is better or equal
		if(rank1.compareTo(rankOpponent) > 0 || rank2.compareTo(rankOpponent) > 0 ||
				rank3.compareTo(rankOpponent) > 0)
			return true;
		else
			return false;
				
	}
}
