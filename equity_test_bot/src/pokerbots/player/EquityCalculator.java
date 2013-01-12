package pokerbots.player;

public class EquityCalculator {
	private Card[] cardByID;
	private Card[] hand; //3 card hand
	private Card[] board; //0, 3, 4, or 5 card board depending on stage in the game
	private boolean usedCards[]; //usedCards[i] is true if card with id=i is in hand or board
	
	EquityCalculator(Card[] hand, Card[] board) {
		cardByID = new Card[52];
		for(int i = 0; i < 52; i++)
			cardByID[i] = CardUtils.getCardByID(i);
		usedCards = new boolean[52];
		setHand(hand);
		if(board != null)
			setBoard(board);
	}
	
	public void setHand(Card[] hand) {
		this.hand = hand;
		for(int i = 0; i < 3; i++) 
			usedCards[hand[i].id] = true;
	}
	
	public void setBoard(Card[] board) {
		this.board = board;
		for(int i = 0; i < 5; i++)
			if(board[i] != null)
				usedCards[board[i].id] = true;
	}
	
	public double calculateTotalEquity() {
		//pre-flop
		if(board == null) {
			return equityPreFlop();
		}
		//post-flop
		else if(board[3] == null) {
			return equityPostFlop();
		}
		//post-turn
		else if(board[4] == null) {
			return equityPostTurn();
		}
		//post-river
		else {
			return equityPostRiver();
		}
	}
	
	//very very slow
	private double equityPreFlop() {
		board = new Card[5];
		for(int i = 0; i < 3; i++)
			board[i] = new Card(-1, -1); //placeholder
		
		double equity = 0.0;
		int possibleBoards = 0;
		
		for(int i = 0; i < 52; i++) {
			if(usedCards[i])
				continue;
			board[0] = cardByID[i];
			usedCards[i] = true;
			
			for(int j = i+1; j < 52; j++) {
				if(usedCards[j])
					continue;
				board[1] = cardByID[j];
				usedCards[j] = true;
				
				for(int k = j+1; k < 52; k++) {
					if(usedCards[k])
						continue;
					possibleBoards++;
					board[2] = cardByID[k];
					usedCards[k] = true;
					
					equity += equityPostFlop();
					
					usedCards[k] = false;
				}
				usedCards[j] = false;
			}
			
			usedCards[i] = false;
		}
		return equity / possibleBoards;
	}
	
	private double equityPostFlop() {
		board[3] = new Card(-1, -1); //placeholder
		
		double equity = 0.0;
		int possibleBoards = 0;
		
		for(int i = 0; i < 52; i++) {
			if(usedCards[i])
				continue;
			possibleBoards++;
			
			board[3] = cardByID[i];
			usedCards[i] = true;
			
			equity += equityPostTurn();
			
			usedCards[i] = false;
		}
		
		return equity / possibleBoards;
	}
	
	private double equityPostTurn() {
		board[4] = new Card(-1, -1); //placeholder
		
		double equity = 0.0;
		int possibleBoards = 0;
		
		for(int i = 0; i < 52; i++) {
			if(usedCards[i])
				continue;
			possibleBoards++;
			
			board[4] = cardByID[i];
			usedCards[i] = true;
			
			equity += equityPostRiver();
			
			usedCards[i] = false;
		}
		return equity / possibleBoards;
	}
			
	private double equityPostRiver() {
		int totalPossibleHands = 0;
		int winningHands = 0;
		
		Card[] opponentHand = new Card[2];
		
		//iterate through all possible opponent hands
		//note that two card opponent hand is an approximation
		for(int i = 0; i < 52; i++) {
			if(usedCards[i])
				continue;
			
			opponentHand[0] = cardByID[i];
			
			for(int j = i+1; j < 52; j++) {
				if(usedCards[j])
					continue;
				
				opponentHand[1] = cardByID[j];
					
				totalPossibleHands++;
				
				if(HandComparer.compareHands(hand, opponentHand, board))
					winningHands++;
			}
		}
			
		return (double)winningHands / totalPossibleHands;
	}
}
