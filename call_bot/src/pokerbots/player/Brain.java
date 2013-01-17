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
	
	public Action act() {
		
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
			else if(performedAction.getType().equalsIgnoreCase("POST")) {
				equity = ec.calculateTotalEquity();
			}
				
		}
		
		maj.update(this);
		dory.update();
		
		if(board[2] == null) 
			return actPreFlop();
		
		else if(board[3] == null)  {
			return actPostFlop();
		}
		
		else if(board[4] == null) 
			return actPostTurn();
		
		else
			return actPostRiver();
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
			if(equity0 > equity2)
				chosenDiscardCard = hand[0];
			else
				chosenDiscardCard = hand[2];
		}
		else {
			if(equity1 > equity2) 
				chosenDiscardCard = hand[1];
			else
				chosenDiscardCard = hand[2];
		}
	}
	
	private Action discardCard() {
		return ActionUtils.discard(chosenDiscardCard);
	}
	
	//////////////////////////////////////////
	private Action actPreFlop() {
		return button ? actPreFlopButton() : actPreFlopNotButton();
	}
	
	private Action actPreFlopButton() { //small blind acts first
		return putAllinMinusOne();
	}
	
	private Action actPreFlopNotButton() { //big blind acts second
		return putAllinMinusOne();
	}
	
	////////////////////////////////////////////////////
	private Action actPostFlop() {
		return button ? actPostFlopButton() : actPostFlopNotButton();
	}
	
	private Action actPostFlopButton() {    //acts second
		return putAllinMinusOne();
	}
	
	private Action actPostFlopNotButton() { //acts first
		return putAllinMinusOne();
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
		else
			return fold();
	}
	
	private Action putAllin() {
		if(canRaise)
			return raiseAllin();
		else if(canBet)
			return betAllin();
		else
			return fold();
	}
	
	private Action putAllinMinusOne() {
		if(canRaise)
			return raiseAllinMinusOne();
		else if(canBet)
			return betAllinMinusOne();
		else
			return fold();
	}
	
	private Action putMin() {
		if(canRaise)
			return raiseMin();
		else if(canBet)
			return betMin();
		else
			return fold();
	}

	private Action putLin(double percent, double inLo, double inHi, double outLo, double outHi) {
		if(canRaise)
			return raiseLin(percent, inLo, inHi, outLo, outHi);
		else if(canBet)
			return betLin(percent, inLo, inHi, outLo, outHi);
		else
			return fold();
	}
	
	private Action putPotPercentage(double percent) {
		if(canRaise)
			return raisePotPercentage(percent);
		else if(canBet)
			return betPotPercentage(percent);
		else
			return fold();
	}
	
	private Action putPotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		if(canRaise)
			return raisePotPercentage(percent, inLo, inHi, outLo, outHi);
		else if(canBet)
			return betPotPercentage(percent, inLo, inHi, outLo, outHi);
		else
			return fold();
	}
	
	
	private Action putHalfPot() {
		if(canRaise)
			return raiseHalfPot();
		else if(canBet)
			return betHalfPot();
		else
			return fold();
	}

	private Action putPot() {
		if(canRaise)
			return raisePot();
		else if(canBet)
			return betPot();
		else
			return fold();
	}
	
	private Action putTwoPot() {
		if(canRaise)
			return raiseTwoPot();
		else if(canBet)
			return betTwoPot();
		else
			return fold();
	}
	
	private Action putThreePot() {
		if(canRaise)
			return raiseThreePot();
		else if(canBet)
			return betThreePot();
		else
			return fold();
	}
	
	//////////////
	///////RAISING
	//////////////
	
	private Action raise(int amount) {
		return ActionUtils.raise(HelperUtils.minMax(amount, raiseAction.getMin(), raiseAction.getMax()));
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
		return ActionUtils.bet(HelperUtils.minMax(amount, betAction.getMin(), betAction.getMax()));
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