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
	
	protected double callConstant;
	public double equity;
	protected double[] eVals, sVals, pVals;
	protected boolean canCheck, canBet, canRaise, canDiscard, canCall;
	
	protected Card chosenDiscardCard;
	protected Historian maj;
	protected Dory dory;
	
	public int wetness;
	protected int skip1, skip2;
	protected EquityCalculator ec;
	protected boolean swapSkip, callRaise, checkRaise;
	
	Brain(Historian maj, Card[] hand, double timebank, boolean callRaise, boolean checkRaise) {
		this.maj = maj;
		this.callRaise = callRaise;
		this.checkRaise = checkRaise;
		dory = new Dory(this, maj);	
		maj.setHand(dory, this);
		swapSkip = false;
		swapSkip = false;
		skip1 = 2;
		skip2 = 3;
		if(timebank < maj.startingTime * 0.4) {
			System.out.println("USING APPROX 0.2");
			skip2 = 3;
		}
		if(timebank < maj.startingTime * 0.1) {
			System.out.println("USING APPROX 0.1");
			skip1 = 5;
			skip2 = 4;
		}
		ec = new EquityCalculator(hand, null, skip1, skip2, swapSkip);
		this.hand = hand;
		
		wetness = 0;
		//the higher these numbers are, the more aggressive the bot is
		
		callConstant = 0.65;
		
		//should range from 0.35 to 0.85, they do not have to be equally spaced
		eVals = new double[]{0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85}; 
		
		//should range from maj.stackSize / 64 to maj.stackSize, play around with these a lot
		sVals = new double[]{maj.stackSize / 40.0, maj.stackSize / 20.0, maj.stackSize / 16.0, maj.stackSize / 8.0, maj.stackSize / 6.0, maj.stackSize / 4.0, maj.stackSize / 3.0, maj.stackSize}; 
		
		//should range from 0.05 to 3.0, play around with these a lot
		pVals = new double[]{0.1, 0.25, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5, 1.7, 1.9, 2.1, 2.3, 2.5, 3.0};
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

		
		for(PerformedAction performedAction : lastActions) {
			if(performedAction.getType().equalsIgnoreCase("DEAL")) {
				ec.setBoard(board);
				if(performedAction.getStreet().equalsIgnoreCase("FLOP")) {
					chooseDiscardCard(); //this will also update equity
					wetness = ec.boardWetness();
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
		
		maj.update(lastActions);
		dory.update();
		
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
		
		
		System.out.println("\nLiquid equity: " + dory.liqEquity());
		System.out.println("Equity: " + equity + "\n");

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
	protected void chooseDiscardCard() {
		//assumes you have a three card hand
		EquityCalculator ec0 = new EquityCalculator(new Card[]{hand[1], hand[2]}, board, skip1, skip2, swapSkip);
		EquityCalculator ec1 = new EquityCalculator(new Card[]{hand[0], hand[2]}, board, skip1, skip2, swapSkip);
		EquityCalculator ec2 = new EquityCalculator(new Card[]{hand[0], hand[1]}, board, skip1, skip2, swapSkip);
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
	
	protected Action discardCard() {
		return ActionUtils.discard(chosenDiscardCard);
	}
	
	//////////////////////////////////////////
	//THIS IS WHAT WE EDIT
	//////////////////////////////////////////
	protected Action actPreFlop() {
		return button ? actPreFlopButton() : actPreFlopNotButton();
	}
	
	protected Action actPreFlopButton() { //small blind acts first
//		if(callRaise) {
//			callRaise = false;
//			return call();
//		}
		
		if(eL(1)) {
			return checkFold();
		}
		else if(eL(2)) {
			return call();
		}
		else if(eL(3)) { 
			return call();
			//return putPotPercentage(dory.liqEquity(), eVals[2], eVals[3], pVals[0], pVals[1]);
		}
		else if(eL(4)) {
			return putPotPercentage(dory.liqEquity(), eVals[3], eVals[4], pVals[2], pVals[3]);
		}
		else if(eL(5)) {
			return putPotPercentage(dory.liqEquity(), eVals[4], eVals[5], pVals[4], pVals[7]);
		}
		else if(eL(6)) {
			return putPotPercentage(dory.liqEquity(), eVals[5], eVals[6], pVals[7], pVals[10]);
		}
		else {
			return putPotPercentage(dory.liqEquity(), eVals[6], eVals[7], pVals[10], pVals[13]);
		}
	}
	
	protected Action actPreFlopNotButton() { //big blind acts second
		if(eL(0)) {
			return checkFold();
		}
		else if(eL(1)) {
			return call();
		}
		else if(eL(2)) { 
			return call();
			//return putPotPercentage(dory.liqEquity(), eVals[2], eVals[3], pVals[0], pVals[1]);
		}
		else if(eL(3)) {
			return putPotPercentage(dory.liqEquity(), eVals[3], eVals[4], pVals[2], pVals[3]);
		}
		else if(eL(4)) {
			return putPotPercentage(dory.liqEquity(), eVals[4], eVals[5], pVals[3], pVals[4]);
		}
		else if(eL(5)) {
			return putPotPercentage(dory.liqEquity(), eVals[5], eVals[6], pVals[4], pVals[8]);
		}
		else {
			return putPotPercentage(dory.liqEquity(), eVals[6], eVals[7], pVals[8], pVals[11]);
		}
	}
	
	////////////////////////////////////////////////////
	protected Action actPostFlop() {
		return button ? actPostFlopButton() : actPostFlopNotButton();
	}
	
	protected Action actPostFlopButton() {    //acts second
		if(eL(4)) {
			if(dory.theirBetsThisStreet - dory.myBetsThisStreet < pV(0))
				return call();
			return checkFold();
		}
		else if(eL(4)) {
			return call();
			//return checkFoldCallPotOdds();
			//return putPotPercentage(dory.liqEquity(), eVals[2], eVals[3], pVals[1], pVals[2]);
		}
		else if(eL(5)) {
			return call();//return putPotPercentage(dory.liqEquity(), eVals[3], eVals[4], pVals[2], pVals[3]);
		}
		else if(eL(6)) {
			return putPotPercentage(dory.liqEquity(), eVals[5], eVals[6], pVals[4], pVals[5]);
		}
		else if(eL(7)) {
			return putPotPercentage(dory.liqEquity(), eVals[6], eVals[7], pVals[5], pVals[6]); 
		}
		else {
			return putPotPercentage(dory.liqEquity(), eVals[7], eVals[8], pVals[6], pVals[7]);
		}
	}
	
	protected Action actPostFlopNotButton() { //acts first
//		if(checkRaise) {
//			checkRaise = false;
//			return check();
//		}
		if(eL(4)) {
			if(dory.theirBetsThisStreet - dory.myBetsThisStreet < pV(0))
				return call();
			return checkFold();
		}
		else if(eL(4)) {
			return call();
			//return checkFoldCallPotOdds();
			//return putPotPercentage(dory.liqEquity(), eVals[2], eVals[3], pVals[1], pVals[2]);
		}
		else if(eL(5)) {
			return call();//return putPotPercentage(dory.liqEquity(), eVals[3], eVals[4], pVals[2], pVals[3]);
		}
		else if(eL(6)) {
			return putPotPercentage(dory.liqEquity(), eVals[5], eVals[6], pVals[4], pVals[5]);
		}
		else if(eL(7)) {
			return putPotPercentage(dory.liqEquity(), eVals[6], eVals[7], pVals[5], pVals[6]); 
		}
		else {
			return putPotPercentage(dory.liqEquity(), eVals[7], eVals[8], pVals[6], pVals[7]);
		}
	}
	
	////////////////////////////////////////////////////
	protected Action actPostTurn() {
	return actPostFlop();
	}
	//protected Action actPostTurnButton() {    //acts second
	//
	//}
	//protected Action actPostTurnNotButton() { //acts first
	//
	//}
	
	////////////////////////////////////////////////////
	protected Action actPostRiver() {
	return actPostFlop();
	}
	//protected Action actPostRiverButton() {    //acts second
	//
	//}
	//protected Action actPostRiverNotButton() { //acts first
	//
	//}
		
	
	////////////////////////////////////////////////////
	// Helper functions
	////////////////////////////////////////////////////
	
	protected Boolean eL(int num) {
		return (equity + dory.changeEquity) < eVals[num];
	}

	protected int pV(int num) {
		return (int)(potSize * pVals[num]);
	}
	
	
	////////////////////////////////////////////////////
	// Return various actions
	////////////////////////////////////////////////////
	
	protected Action call() {
		if(canCall)
			return ActionUtils.call();
		else if(canCheck)
			return check();
		else
			return putMin();
	}
	
	protected Action check() {
		if(!canCheck)
			return fold();
		else
			return ActionUtils.check();
	}
	
	protected Action fold() {
		return ActionUtils.fold();
	}
	
	protected Action checkFoldCallPotOdds() {
		if(canCheck)
			return check();
		else if(canCall && equity > dory.potOdds())
			return call();
		else
			return fold();
	}
	
	protected Action foldCallPotOdds() {
		if(canCall && equity > dory.potOdds())
			return call();
		else
			return fold();
	}
	//////////////////////
	///////RAISING/BETTING (whichever is allowed)
	//////////////////////
	protected Action put(int amount) {
		if(canRaise)
			return raise(amount);
		else if(canBet)
			return bet(amount);
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action putAllin() {
		if(canRaise)
			return raiseAllin();
		else if(canBet)
			return betAllin();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action putAllinMinusOne() {
		if(canRaise)
			return raiseAllinMinusOne();
		else if(canBet)
			return betAllinMinusOne();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action putMin() {
		if(canRaise)
			return raiseMin();
		else if(canBet)
			return betMin();
		else if(canCall)
			return call();
		else
			return fold();
	}

	protected Action putLin(double percent, double inLo, double inHi, double outLo, double outHi) {
		if(canRaise)
			return raiseLin(percent, inLo, inHi, outLo, outHi);
		else if(canBet)
			return betLin(percent, inLo, inHi, outLo, outHi);
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action putPotPercentage(double percent) {
		if(canRaise)
			return raisePotPercentage(percent);
		else if(canBet)
			return betPotPercentage(percent);
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action putPotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		if(canRaise)
			return raisePotPercentage(percent, inLo, inHi, outLo, outHi);
		else if(canBet)
			return betPotPercentage(percent, inLo, inHi, outLo, outHi);
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	
	protected Action putHalfPot() {
		if(canRaise)
			return raiseHalfPot();
		else if(canBet)
			return betHalfPot();
		else if(canCall)
			return call();
		else
			return fold();
	}

	protected Action putPot() {
		if(canRaise)
			return raisePot();
		else if(canBet)
			return betPot();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action putTwoPot() {
		if(canRaise)
			return raiseTwoPot();
		else if(canBet)
			return betTwoPot();
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action putThreePot() {
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
	
	protected Action raise(int amount) {
		if(canRaise)
			return ActionUtils.raise(HelperUtils.minMax(amount, raiseAction.getMin(), raiseAction.getMax()));
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action raiseAllin() {
		return raise(raiseAction.getMax());
	}
	
	protected Action raiseAllinMinusOne() {
		return raise(raiseAction.getMax());
	}
	
	protected Action raiseMin() {
		return raise(raiseAction.getMin() + 2);
	}
	
	protected Action raiseLin(double percent, double inLo, double inHi, double outLo, double outHi) {
		double linAmt = HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi);
		
		if(linAmt < raiseAction.getMin() * callConstant)
			return call();
		else
			return raise((int)linAmt);
	}
	
	protected Action raisePotPercentage(double percent) {
		double raiseAmt = potSize * percent + dory.myBetsThisStreet;

		if(raiseAmt < raiseAction.getMin() * callConstant)
			return call();
		else
			return raise((int)raiseAmt);
	}
	
	protected Action raisePotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		double value = HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi);
		
		return raisePotPercentage(value);
	}
	
	protected Action raiseHalfPot() {
		return raisePotPercentage(0.5);
	}

	protected Action raisePot() {
		return raisePotPercentage(1.0);
	}
	
	protected Action raiseTwoPot() {
		return raisePotPercentage(2.0);
	}
	
	protected Action raiseThreePot() {
		return raisePotPercentage(3.0);
	}
	
	//////////////
	///////BETTING
	//////////////
	protected Action bet(int amount) {
		if(canBet)
			return ActionUtils.bet(HelperUtils.minMax(amount, betAction.getMin(), betAction.getMax()));
		else if(canCall)
			return call();
		else
			return fold();
	}
	
	protected Action betAllin() {
		return bet(betAction.getMax());
	}
	
	protected Action betAllinMinusOne() {
		return bet(betAction.getMax() - 1);
	}
	
	protected Action betMin() {
		return bet(betAction.getMin() + 2);
	}
	
	protected Action betLin(double percent, double inLo, double inHi, double outLo, double outHi) {
		double linAmt = HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi);

		if(linAmt < betAction.getMin() * callConstant)
			return call();
		else
			return bet((int)linAmt);
	}
	
	protected Action betPotPercentage(double percent) {
		double betAmt = potSize * percent;

		if(betAmt < betAction.getMin() * callConstant)
			return call();
		else
			return bet( (int)betAmt) ;
	}
	
	protected Action betPotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		double value = HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi);

		return betPotPercentage(value);
	}
	
	protected Action betHalfPot() {
		return betPotPercentage(0.5);
	}

	protected Action betPot() {
		return betPotPercentage(1.0);
	}
	
	protected Action betTwoPot() {
		return betPotPercentage(2.0);
	}
	
	protected Action betThreePot() {
		return betPotPercentage(3.0);
	}
	
	protected Action checkFold() {
		return canCheck ? check() : fold();
	}
	

	
	protected Action checkCallAllinMinusOne() {
		if(canCheck)
			return check();
		else if(canCall)
			return call();
		else
			return putAllinMinusOne();
	}
}
