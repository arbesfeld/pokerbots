package pokerbots.player;

import java.util.*;

public class EquityCalculator {
	private Card[] cardByID;
	private Card[] hand; //2 or 3 card hand
	private Card[] board; //0, 3, 4, or 5 card board depending on stage in the game
	private boolean usedCards[]; //usedCards[i] is true if card with id=i is in hand or board
	private int winningBoards, possibleBoards;
	private int skip, skip2; //how many cards you should scan "ie. monte carlo method where 1 = all cards"
	
	public final int cardCount = 52;
	EquityCalculator(Card[] hand, Card[] board) {
		cardByID =       new Card[cardCount];
		usedCards =      new boolean[cardCount];
		winningBoards =  0;
		possibleBoards = 0;
		skip =           3;
		skip2 =          2;
		
		for(int i = 0; i < cardCount; i++)
			cardByID[i] = CardUtils.getCardByID(i);
		
		setHand(hand);
		if(board != null)
			setBoard(board);
	}
	
	public void setSkip(int skip) {
		this.skip = skip;
	}
	
	public void setHand(Card[] hand) {
		this.hand = hand;
		for(int i = 0; i < hand.length; i++) 
			usedCards[hand[i].id] = true;
		Arrays.sort(hand);
	}
	
	public void setBoard(Card[] board) {
		this.board = board;
		for(int i = 0; i < 5; i++)
			if(board[i] != null)
				usedCards[board[i].id] = true;
	}
	
	private double getEquity() {
		return (double) winningBoards / possibleBoards;
	}
	public double calculateTotalEquity() {
		winningBoards = 0;
		possibleBoards = 0;
		
		//pre-flop
		if(board == null) {
			return equityPreFlopApprox();
			//return equityPreFlop();
		}
		//post-flop
		else if(board[3] == null) {
			return equityPostFlop(0);
		}
		//post-turn
		else if(board[4] == null) {
			return equityPostTurn(0);
		}
		//post-river
		else {
			return equityPostRiver();
		}
	}
	
	private double equityPreFlopApprox() {
		return EquityUtils.getEquityThreeCardHand(hand);
	}
	
	//very very slow
	private double equityPreFlop() {
		board = new Card[5];
		
		for(int i = 0; i < cardCount; i += skip) {
			if(usedCards[i])
				continue;
			board[0] = cardByID[i];
			usedCards[i] = true;
			
			for(int j = i+1; j < cardCount; j += skip) {
				if(usedCards[j])
					continue;
				board[1] = cardByID[j];
				usedCards[j] = true;
				
				for(int k = j+1; k < cardCount; k += skip) {
					if(usedCards[k])
						continue;
					
					board[2] = cardByID[k];
					usedCards[k] = true;
					
					equityPostFlop(i+1);
					
					board[2] = null;
					usedCards[k] = false;
				}
				board[1] = null;
				usedCards[j] = false;
			}
			
			board[0] = null;
			usedCards[i] = false;
		}
		return getEquity();
	}
	
	private double equityPostFlop(int start) {
		for(int i = start; i < cardCount; i += skip) {
			if(usedCards[i])
				continue;
			
			board[3] = cardByID[i];
			usedCards[i] = true;
			
			equityPostTurn(i+1);
			
			usedCards[i] = false;
			board[3] = null;
		}
		
		return getEquity();
	}
	
	private double equityPostTurn(int start) {
		for(int i = start; i < cardCount; i += skip) {
			if(usedCards[i])
				continue;
		
			board[4] = cardByID[i];
			usedCards[i] = true;
			
			equityPostRiver();
			
			usedCards[i] = false;
			board[4] = null;
		}
		
		return getEquity();
	}
			
	private double equityPostRiver() {
		Card[] opponentHand = new Card[2];
		
		//iterate through all possible opponent hands
		//note that two card opponent hand is an approximation
		for(int i = 0; i < cardCount; i += skip2) {
			if(usedCards[i])
				continue;
			
			opponentHand[0] = cardByID[i];
			
			for(int j = i+1; j < cardCount; j += skip2) {
				if(usedCards[j])
					continue;
				
				opponentHand[1] = cardByID[j];
					
				possibleBoards++;
				
				if(HandComparer.compareHands(hand, opponentHand, board))
					winningBoards++;
			}
		}
			
		return getEquity();
	}
}
