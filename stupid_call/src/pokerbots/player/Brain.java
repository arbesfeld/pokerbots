package pokerbots.player;

public class Brain {
	//instantiated every hand
	public int handId, myBank, oppBank;
	public boolean button;
	
	public Card[] hand;
	
	//instantiated every action
	public int potSize, numBoardCards, numLastActions, numLegalActions;
	public Card[] board;
	public PerformedAction[] lastActions;
	public LegalAction[] legalActions;
	
	public LegalAction raiseAction, betAction;

	public double timebank;
	
	private double equity;
	private boolean canCheck, canBet, canRaise, canDiscard, canCall;
	
	private Card chosenDiscardCard;
	private Historian maj;
	private Dory dory;
	
	private EquityCalculator ec;
	
	Brain(Historian maj, Card[] hand) {
		this.maj = maj;
		dory = new Dory(this, maj);	
		ec = new EquityCalculator(hand, null);
		this.hand = hand;
	}
	public void reset() {
		raiseAction = null;
		betAction = null;
		canRaise = false;
		canBet = false;
		canCheck = false;
		canCall = false;
		canDiscard = false;
	}
	public Action act() {
		
		reset();
		
		for(LegalAction legalAction : legalActions) {
			if(legalAction.getType().equalsIgnoreCase("RAISE")) {
				raiseAction = legalAction;
				canRaise = true;
			}
			else if(legalAction.getType().equalsIgnoreCase("BET")) {
				betAction = legalAction;
				canBet = true;
			}
			else if(legalAction.getType().equalsIgnoreCase("CHECK")) {
				canCheck = true;
			}
			else if(legalAction.getType().equalsIgnoreCase("CALL")) {
				canCall = true;
			}
			else if(legalAction.getType().equalsIgnoreCase("DISCARD")) {
				canDiscard = true;
				if(chosenDiscardCard == null)
					chooseDiscardCard();
				return discardCard();
			}
		}
		
		for(PerformedAction performedAction : lastActions) {
			if(performedAction.getType().equalsIgnoreCase("DEAL")) {
				ec.setBoard(board);
				if(performedAction.getStreet().equalsIgnoreCase("FLOP")) {
					chooseDiscardCard(); //this will also update equity
				}
				else {
					equity = ec.calculateTotalEquity();
				}
			}
			else if(performedAction.getType().equalsIgnoreCase("POST") &&
					performedAction.getActor().equalsIgnoreCase(maj.myName)) {
				equity = ec.calculateTotalEquity();
			}
				
		}
		
		maj.update(this);
		dory.update();
		
		if(equity < 0.50)
			return putAllinMinusOne();
		else if(equity < 0.55)
			return putPotPercentage(equity, 0.65, 0.7, 1.0, 2.0);
		else if(equity < 0.6)
			return putMin();
		else if(equity < 0.65) {
			if(canCall)
				return call();
			else
				return check();
		}
		else
			return fold();
//		if(board[2] == null) 
//			return actPreFlop();
//		
//		else if(board[3] == null)  {
//			return actPostFlop();
//		}
//		
//		else if(board[4] == null) 
//			return actPostTurn();
//		
//		else
//			return actPostRiver();
	}
	
	//////////////////////////////////////////////
	private void chooseDiscardCard() {
		//assumes you have a three card hand
		EquityCalculator ec0 = new EquityCalculator(new Card[]{hand[1], hand[2]}, board);
		EquityCalculator ec1 = new EquityCalculator(new Card[]{hand[0], hand[2]}, board);
		EquityCalculator ec2 = new EquityCalculator(new Card[]{hand[0], hand[1]}, board);
		double equity0 = ec0.calculateTotalEquity();
		double equity1 = ec1.calculateTotalEquity();
		double equity2 = ec2.calculateTotalEquity();
		
		equity = Math.max(Math.max(equity0, equity1), equity2);
		
		if(equity0 > equity1) {
			if(equity0 > equity2) {
				ec = ec0;
				chosenDiscardCard = hand[0];
			}
			else {
				ec = ec2;
				chosenDiscardCard = hand[2];
			}
		}
		else {
			if(equity1 > equity2)  {
				ec = ec1;
				chosenDiscardCard = hand[1];
			}
			else {
				chosenDiscardCard = hand[2];
				ec = ec2;
			}
		}
	}
	
	private Action discardCard() {
		return ActionUtils.discard(chosenDiscardCard);
	}
	
	//////////////////////////////////////////
	//THIS IS WHAT WE EDIT
	//////////////////////////////////////////
	private Action actPreFlop() {
		if(maj.stackSize < potSize / 10)
			return putAllinMinusOne();
		return button ? actPreFlopButton() : actPreFlopNotButton();
	}
	
	private Action actPreFlopButton() { //small blind acts first
		if(equity < 0.4) {
			if(dory.hasOpponentRaisedThisStreet()) { // opponent raised
				if(equity < 0.45 || 
						dory.lastOpponentRaiseSize() > potSize / 4)
					return fold();
				else // an amount less than 20
					return call(); 
			}
			else {
				return call();
			}
		}
		else if(equity < 0.5) { 
			if(dory.hasOpponentRaisedThisStreet())
				return fold();
			else
				return putLin(equity, 0.55, 0.6, maj.stackSize / 16, maj.stackSize / 8);
		}
		else if(equity < 0.6) {
			if(dory.hasOpponentRaisedThisStreet())
				return putMin();
			else
				return putLin(equity, 0.6, 0.7, maj.stackSize / 8, maj.stackSize / 4);
		}
		else if(equity < 0.7) {
			return putLin(equity, 0.6, 0.7, maj.stackSize / 4, maj.stackSize / 2);
		}
		else {
			return putAllinMinusOne();
		}
	}
	
	private Action actPreFlopNotButton() { //big blind acts second
		if(equity < 0.45 && maj.getPFR() < (1.0 - equity)) {
			return checkFold();
		}
		else if(equity < 0.55) {
			if(maj.getPFR() < 1.0 - equity) {
				return checkFold();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.25) {
				return call();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.5) {
				return putPotPercentage(equity, maj.getPFR(), (1.0 - equity) * 1.5, 
						0.25, 0.5 ); 
			}
			else {
				return putPotPercentage(equity, maj.getPFR(), (1.0 - equity) * 1.5, 
						0.5, 0.75 ); 
			}
		}
		else if(equity < 0.60) {
			if(maj.getPFR() < 1.0 - equity) {
				return checkFold();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.33) {
				return putMin();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.66) {
				return putPotPercentage( equity, maj.getPFR(), (1.0 - equity) * 1.66, 
						0.75, 1.0); 
			}
			else {
				return putPotPercentage(equity, maj.getPFR(), (1.0 - equity) * 1.66, 
						1.0, 1.25); 
			}
		}
		else if(equity < 0.65) {
			if(maj.getPFR() < 1.0 - equity) {
				return call();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.33) {
				return putMin();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.66) {
				return putPotPercentage( equity, maj.getPFR(), (1.0 - equity) * 1.66, 
						1.25, 1.5); 
			}
			else {
				return putPotPercentage(equity, maj.getPFR(), (1.0 - equity) * 1.66, 
						1.5, 2.0); 
			}
		}
		else {
			return putAllinMinusOne();
		}
	}
	
	////////////////////////////////////////////////////
	private Action actPostFlop() {
		if(maj.stackSize < potSize / 10)
			return putAllinMinusOne();
		return button ? actPostFlopButton() : actPostFlopNotButton();
	}
	
	private Action actPostFlopButton() {    //acts second
		if(equity < 0.45) {
			return checkFold();
		}
		else if(equity < 0.50) {
			if(dory.hasOpponentCheckedThisStreet()) {
				if(dory.hasOpponentRaisedThisStreet())  //he checked, I raised, he raised
					return fold();
				else
					return putPotPercentage(equity, 0.45, 0.5, 0.25, 0.5);
			}
			else if(dory.opponentBetSizeThisStreet() <= potSize / 10)
				return putMin();
			else
				return fold();
		}
		else if(equity < 0.60) {
			if(dory.hasOpponentCheckedThisStreet()) {
				return putPotPercentage(equity, 0.50, 0.60, 0.5, 1.0); 
			}
			else if(dory.opponentBetSizeThisStreet() <= potSize / 5) {
				if(dory.hasOpponentRaisedThisStreet()) //he checked, I raised, he raised
					return fold();
				else 
					return putPotPercentage(equity, 0.5, 0.6, 0.25, 0.5);
			}
			else if(dory.opponentBetSizeThisStreet() <= potSize / 3)
				return putMin();
			else
				return fold();
		}
		else if(equity < 0.70) {
			if(dory.hasOpponentCheckedThisStreet()) {
				if(dory.hasOpponentRaisedThisStreet())  //he checked then I raised then he raised
					return putMin();
				else
					return putPotPercentage(equity, 0.6, 0.7, 1.0, 2.0); 
			}
			else if(dory.hasOpponentBetThisStreet()) {
				if(dory.hasOpponentRaisedThisStreet()) //he bet, I raised, he raised
					return putMin();
				else 
					return putPotPercentage(equity, 0.65, 0.75, 0.5, 1.0);
			}
			else //should never be executed
				return putAllinMinusOne();
		}
		else 
			return putAllinMinusOne();
	}
	
	private Action actPostFlopNotButton() { //acts first
		if(equity < 0.55) {
			return checkFold();
		}
		else if(equity < 0.65) {
			if(dory.hasOpponentRaisedThisStreet()) {
				if(dory.lastOpponentRaiseSize() > potSize / 8)
					return fold();
				else
					return call();
			}
			else {
				return putPotPercentage(equity, 0.55, 0.65, 0.25, 0.5);
			}
		}
		else if(equity < 0.75) {
			if(dory.hasOpponentRaisedThisStreet()) {
				return call();
			}
			else {
				return putPotPercentage(equity, 0.65, 0.75, 0.5, 1.0);
			}
		}
		else {
			return putAllinMinusOne();
		}
	}
	
	////////////////////////////////////////////////////
	private Action actPostTurn() {
		return actPostFlop();
	}
//	private Action actPostTurnButton() {    //acts second
//		
//	}
//	private Action actPostTurnNotButton() { //acts first
//		
//	}
	
	////////////////////////////////////////////////////
	private Action actPostRiver() {
		return actPostFlop();
	}
//	private Action actPostRiverButton() {    //acts second
//			 
//	}
//	private Action actPostRiverNotButton() { //acts first
//		
//	}
	
	
	////////////////////////////////////////////////////
	// Return various actions
	////////////////////////////////////////////////////
	
	private Action call() {
		if(!canCall)
			return putMin();
		else
			return ActionUtils.call();
	}
	
	private Action check() {
		if(!canCheck)
			return fold();
		else
			return ActionUtils.check();
	}
	
	private Action fold() {
		return ActionUtils.fold();
	}
	
	//////////////////////
	///////RAISING/BETTING (whichever is allowed)
	//////////////////////
	private Action put(int amount) {
		if(canRaise)
			return raise(amount);
		else if(canBet)
			return bet(amount);
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action putAllin() {
		if(canRaise)
			return raiseAllin();
		else if(canBet)
			return betAllin();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action putAllinMinusOne() {
		if(canRaise)
			return raiseAllinMinusOne();
		else if(canBet)
			return betAllinMinusOne();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action putMin() {
		if(canRaise)
			return raiseMin();
		else if(canBet)
			return betMin();
		else if(canCall)
			return call();
		else
			return fold();
	}

	private Action putLin(double percent, double inLo, double inHi, double outLo, double outHi) {
		if(canRaise)
			return raiseLin(percent, inLo, inHi, outLo, outHi);
		else if(canBet)
			return betLin(percent, inLo, inHi, outLo, outHi);
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action putPotPercentage(double percent) {
		if(canRaise)
			return raisePotPercentage(percent);
		else if(canBet)
			return betPotPercentage(percent);
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action putPotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		if(canRaise)
			return raisePotPercentage(percent, inLo, inHi, outLo, outHi);
		else if(canBet)
			return betPotPercentage(percent, inLo, inHi, outLo, outHi);
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	
	private Action putHalfPot() {
		if(canRaise)
			return raiseHalfPot();
		else if(canBet)
			return betHalfPot();
		else if(canCall)
			return call();
		else
			return fold();
	}

	private Action putPot() {
		if(canRaise)
			return raisePot();
		else if(canBet)
			return betPot();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action putTwoPot() {
		if(canRaise)
			return raiseTwoPot();
		else if(canBet)
			return betTwoPot();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action putThreePot() {
		if(canRaise)
			return raiseThreePot();
		else if(canBet)
			return betThreePot();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	//////////////
	///////RAISING
	//////////////
	
	private Action raise(int amount) {
		if(canRaise)
			return ActionUtils.raise(HelperUtils.minMax(amount, raiseAction.getMin(), raiseAction.getMax()));
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action raiseAllin() {
		return raise(raiseAction.getMax());
	}
	
	private Action raiseAllinMinusOne() {
		return raise(raiseAction.getMax());
	}
	
	private Action raiseMin() {
		return raise(raiseAction.getMin());
	}
	
	private Action raiseLin(double percent, double inLo, double inHi, double outLo, double outHi) {
		return raise(HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi));
	}
	
	private Action raisePotPercentage(double percent) {
		return raise( (int) (potSize * percent) );
	}
	
	private Action raisePotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		double value = HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi);
		return raisePotPercentage(value);
	}
	
	private Action raiseHalfPot() {
		return raisePotPercentage(0.5);
	}

	private Action raisePot() {
		return raisePotPercentage(1.0);
	}
	
	private Action raiseTwoPot() {
		return raisePotPercentage(2.0);
	}
	
	private Action raiseThreePot() {
		return raisePotPercentage(3.0);
	}
	
	//////////////
	///////BETTING
	//////////////
	private Action bet(int amount) {
		if(canBet)
			return ActionUtils.bet(HelperUtils.minMax(amount, betAction.getMin(), betAction.getMax()));
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	private Action betAllin() {
		return bet(betAction.getMax());
	}
	
	private Action betAllinMinusOne() {
		return bet(betAction.getMax() - 1);
	}
	
	private Action betMin() {
		return bet(betAction.getMin());
	}
	
	private Action betLin(double percent, double inLo, double inHi, double outLo, double outHi) {
		return bet(HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi));
	}
	
	private Action betPotPercentage(double percent) {
		return bet( (int) (potSize * percent) );
	}
	
	private Action betPotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		double value = HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi);
		return betPotPercentage(value);
	}
	
	private Action betHalfPot() {
		return betPotPercentage(0.5);
	}

	private Action betPot() {
		return betPotPercentage(1.0);
	}
	
	private Action betTwoPot() {
		return betPotPercentage(2.0);
	}
	
	private Action betThreePot() {
		return betPotPercentage(3.0);
	}
	
	private Action checkFold() {
		return canCheck ? check() : fold();
	}
	
	private Action checkCallAllinMinusOne() {
		if(canCheck)
			return check();
		else if(canCall)
			return call();
		else
			return putAllinMinusOne();
	}
}
