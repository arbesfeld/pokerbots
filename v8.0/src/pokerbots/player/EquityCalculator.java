package pokerbots.player;

import java.util.*;

public class EquityCalculator {
	private Card[] cardByID;
	private Card[] hand; //2 or 3 card hand
	private Card[] board; //0, 3, 4, or 5 card board depending on stage in the game
	private boolean usedCards[]; //usedCards[i] is true if card with id=i is in hand or board
	private int winningBoards, possibleBoards;
	private int skip, skip2; //how many cards you should scan "ie. monte carlo method where 1 = all cards"
	private boolean swapSkip;
	//private long wetness;
	public final int cardCount = 52;
	EquityCalculator(Card[] hand, Card[] board, int skip, int skip2, boolean swapSkip) {
		cardByID =       new Card[cardCount];
		usedCards =      new boolean[cardCount];
		winningBoards =  0;
		possibleBoards = 0;
		//wetness =        0;
		this.skip =      skip;
		this.skip2 =     skip2;
		this.swapSkip =  swapSkip;
		
		for(int i = 0; i < cardCount; i++)
			cardByID[i] = CardUtils.getCardByID(i);
		
		setHand(hand);
		if(board != null)
			setBoard(board);
	}
	
	public void setSkip(int skip, int skip2, boolean swapSkip) {
		this.skip = skip;
		this.skip2 = skip;
		this.swapSkip = swapSkip;
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
			
			if(swapSkip) {
				if(skip == 2)
					skip = 1;
				else if(skip == 1)
					skip = 2;
			}
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
			
			if(swapSkip) {
				if(skip == 2)
					skip = 1;
				else if(skip == 1)
					skip = 2;
			}
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
				
				if(swapSkip) {
					if(skip2 == 2)
						skip2 = 1;
					else if(skip2 == 1)
						skip2 = 2;
				}
			}
			
			if(swapSkip) {
				if(skip2 == 2)
					skip2 = 1;
				else if(skip2 == 1)
					skip2 = 2;
			}
		}
			
		return getEquity();
	}
	
	public int boardWetness() {
		int wetness = 0;
		Card[] flop = new Card[]{board[0], board[1], board[2]};
		
		Arrays.sort(flop);
		int[] suits = new int[4];
		Arrays.fill(suits, 0);
		int highCards = 0;
		Arrays.sort(flop);
		
		for(int i = 0; i < 3; i++) {
			Card card = flop[i];
			
			suits[card.suit]++;
			if(card.val >= 11) {
				highCards++;
			}
		}
		
		//suited?
		Arrays.sort(suits);
		switch(suits[3]) {
		case 3:
			wetness -= 10;
			break;
		case 2:
			wetness -= 7;
			break;
		default:
			wetness += 10;
			break;
		}
		
		//high cards
		switch(highCards) {
		case 3: 
			wetness -= 10;
			break;
		case 2: 
			wetness -= 5;
			break;
		case 1:
			wetness += 10;
			break;
		default:
			wetness += 12;
			break;
		}
		
		//connecters
		if(flop[2].val == flop[1].val + 1 && flop[1].val == flop[0].val + 1) {
			wetness -= 10;
		}
		else if(flop[2].val == flop[1].val + 1 || flop[1].val == flop[0].val + 1) {
			wetness -= 5;
		}
		else {
			wetness += 5;
		}
		
		//gap connectors
		if(flop[2].val == flop[1].val + 2 && flop[1].val == flop[0].val + 2) {
			wetness -= 5;
		}
		else if(flop[2].val == flop[1].val + 2 || flop[1].val == flop[0].val + 2 || flop[2].val == flop[0].val + 2) {
			wetness -= 3;
		}
		else {
			wetness += 3;
		}
		
		return wetness;
	}
//	public double boardWetness() {;
//		wetness = 0;
//		possibleBoards = 0;
//		
//		//post-flop
//		if(board[3] == null) {
//			return boardWetnessPostflop();
//		}
//		//post-turn
//		else if(board[4] == null) {
//			return boardWetnessPostturn(0);
//		}
//		//post-river
//		else {
//			return boardWetnessPostriver();
//		}
//	}
//	private double boardWetnessPostflop() {
//		for(int i = 0; i < cardCount; i += skip) {
//			if(usedCards[i])
//				continue;
//			
//			board[3] = cardByID[i];
//			usedCards[i] = true;
//			
//			boardWetnessPostturn(i);
//			
//			usedCards[i] = false;
//			board[3] = null;
//		}
//		return (double)wetness / possibleBoards / Math.pow(10, 5);
//	}
//	
//	private double boardWetnessPostturn(int start) {
//		for(int i = start+1; i < cardCount; i += skip) {
//			if(usedCards[i])
//				continue;
//			
//			board[4] = cardByID[i];
//			usedCards[i] = true;
//			
//			boardWetnessPostriver();
//			
//			usedCards[i] = false;
//			board[4] = null;
//		}
//		return (double)wetness / possibleBoards / Math.pow(10, 5);
//	}
//	
//	private double boardWetnessPostriver() {
//		Card[] opponentHand = new Card[2];
//		
//		//iterate through all possible opponent hands
//		//note that two card opponent hand is an approximation
//		for(int i = 0; i < cardCount; i += skip2) {
//			if(usedCards[i])
//				continue;
//			
//			opponentHand[0] = cardByID[i];
//			
//			for(int j = i+1; j < cardCount; j += skip2) {
//				if(usedCards[j])
//					continue;
//				
//				opponentHand[1] = cardByID[j];
//				
//				possibleBoards++;
//				HandRank rank = HandEvaluator.getHandRank(opponentHand, board);
//				wetness += rank.val;
//			}
//		}
//		return (double)wetness / possibleBoards / Math.pow(10, 5);
//	}
}
