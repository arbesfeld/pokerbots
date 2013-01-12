package pokerbots.player;

//hand is 3 card hand, opponentHand is 2 card hand, board is 5 cards
public class HandComparer {
	//returns true if hand1 is stronger or "equal" to hand2, false otherwise
	public static boolean compareHands(Card[] hand, Card[] opponentHand, Card[] board) {
		//tries every possible discard and choose the best one--
		//not ideal but very close approximation
		HandRank rank1 = HandEvaluator.getHandRank(new Card[]{hand[0], hand[1]}, board);
		HandRank rank2 = HandEvaluator.getHandRank(new Card[]{hand[0], hand[2]}, board);
		HandRank rank3 = HandEvaluator.getHandRank(new Card[]{hand[1], hand[2]}, board);
		
		HandRank rankOpponent = HandEvaluator.getHandRank(opponentHand, board);
		
		int rank1compare = rank1.compareTo(rankOpponent);
		int rank2compare = rank2.compareTo(rankOpponent);
		int rank3compare = rank3.compareTo(rankOpponent);
		
		//our hand is outright better
		if(rank1compare > 0 || rank2compare > 0 || rank3compare > 0)
			return true;
		else
			return false;
		
	}
	
}
