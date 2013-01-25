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
		skip =           2;
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
		return equityPreFlop();
		
//		//post-flop
//		else if(board[3] == null) {
//			return equityPostFlop(0);
//		}
//		//post-turn
//		else if(board[4] == null) {
//			return equityPostTurn(0);
//		}
//		//post-river
//		else {
//			return equityPostRiver();
//		}
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

			long time1 = System.nanoTime();
			
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
					
					Card[] oldHand = hand;
					chooseDiscardCard();
					equityPostFlop(k+1);
					hand = oldHand;
					
					board[2] = null;
					usedCards[k] = false;
				}
				board[1] = null;
				usedCards[j] = false;
			}
			long time2 = System.nanoTime();
			double totalTime = (time2 - time1) / Math.pow(10, 9) * 52 * 14 * 14 * 14 / 60 / 60; 
			//System.out.println(i + "/52");
			//System.out.println("Total time (hours): " + totalTime);
			board[0] = null;
			usedCards[i] = false;
		}
		return getEquity();
	}
	private void chooseDiscardCard() {
		//assumes you have a three card hand
		EquityCalculatorOld ec0 = new EquityCalculatorOld(new Card[]{hand[1], hand[2]}, board);
		EquityCalculatorOld ec1 = new EquityCalculatorOld(new Card[]{hand[0], hand[2]}, board);
		EquityCalculatorOld ec2 = new EquityCalculatorOld(new Card[]{hand[0], hand[1]}, board);
		double equity0 = ec0.calculateTotalEquity();
		double equity1 = ec1.calculateTotalEquity();
		double equity2 = ec2.calculateTotalEquity();
		
		if(equity0 > equity1) {
			if(equity0 > equity2) {
				this.setHand(new Card[]{hand[1], hand[2]});
			}
			else {
				this.setHand(new Card[]{hand[0], hand[1]});
			}
		}
		else {
			if(equity1 > equity2)  {
				this.setHand(new Card[]{hand[0], hand[2]});
			}
			else {
				this.setHand(new Card[]{hand[0], hand[1]});
			}
		}
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
