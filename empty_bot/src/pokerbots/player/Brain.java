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
	private double[] eVals, sVals, pVals;
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
		
		//the higher these numbers are, the more aggressive the bot is
		
		//should range from 0.35 to 0.85, they do not have to be equally spaced
		eVals = new double[]{0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8}; 
		
		//should range from maj.stackSize / 64 to maj.stackSize, play around with these a lot
		sVals = new double[]{maj.stackSize / 32.0, maj.stackSize / 16.0, maj.stackSize / 8.0, maj.stackSize / 4.0, maj.stackSize / 2.0, maj.stackSize}; 
		
		//should range from 0.05 to 3.0, play around with these a lot
		pVals = new double[]{0.1, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 3.0};
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
		if(maj.stackSize < pV(0))
			return putAllinMinusOne();
		return button ? actPreFlopButton() : actPreFlopNotButton();
	}
	
	private Action actPreFlopButton() { //small blind acts first
		if(eL(0)) {
			if(dory.hasOpponentRaisedThisStreet()) { // opponent raised
				if(dory.lastOpponentRaiseSize() > sVals[0])
					return fold();
				else // he raised an amount less than sVals[0]
					return call(); 
			}
			else {
				return call();
			}
		}
		else if(eL(2)) { 
			if(dory.hasOpponentRaisedThisStreet()) {
				if(dory.lastOpponentRaiseSize() > sVals[1])
					return fold();
				else // he raised an amount less than sVals[1]
					return call();
			}
			else
				return putLin(equity, eVals[0], eVals[2], sVals[1], sVals[2]);
		}
		else if(eL(4)) {
			if(dory.hasOpponentRaisedThisStreet()) {
				if(dory.hasOpponentRaisedTwiceThisStreet())
					return call();
				else
					return putMin();
			}
			else
				return putLin(equity, eVals[2], eVals[4], sVals[2], sVals[3]);
		}
		else if(eL(6)) {
			return putLin(equity, eVals[4], eVals[6], sVals[3], sVals[4]);
		}
		else {
			return putAllinMinusOne();
		}
	}
	
	private Action actPreFlopNotButton() { //big blind acts second
		if(eL(1)) {
			return checkFold();
		}
		else if(eL(2)) {
			if(equity < 1.0 - maj.pfAggro()) {
				return checkFold();
			}
			else if(equity < 1.0 - maj.pfAggro() / 1.25) {
				return call();
			}
			else if(equity < 1.0 - maj.pfAggro() / 1.5) {
				return putLin(equity, 1.0 - maj.pfAggro() / 1.25, 1.0 - maj.pfAggro() / 1.5, 
						sVals[0], sVals[1] ); 
			}
			else {
				return putLin(equity, 1.0 - maj.pfAggro() / 1.5, 1.0, 
						sVals[1], sVals[2] ); 
			}
		}
		else if(eL(4)) {
			if(equity < 1.0 - maj.pfAggro()) {
				return checkFold();
			}
			else if(equity < 1.0 - maj.pfAggro() / 1.33) {
				return putMin();
			}
			else if(equity < 1.0 - maj.pfAggro() / 1.66) {
				return putLin( equity, 1.0 - maj.pfAggro() / 1.33, 1.0 - maj.pfAggro() / 1.66, 
						sVals[1], sVals[2]); 
			}
			else {
				return putLin(equity, 1.0 - maj.pfAggro() / 1.66, 1.0, 
						sVals[2], sVals[3]); 
			}
		}
		else if(eL(5)) {
			if(equity < 1.0 - equity) {
				return call();
			}
			else if(equity < 1.0 - maj.pfAggro() / 1.33) {
				return putMin();
			}
			else if(equity < 1.0 - maj.pfAggro() / 1.66) {
				return putLin( equity, 1.0 - maj.pfAggro() / 1.33, 1.0 - maj.pfAggro() / 1.66, 
						sVals[3], sVals[4]); 
			}
			else {
				return putLin(equity, 1.0 - maj.pfAggro() / 1.66, 1.0, 
						sVals[4], sVals[5]); 
			}
		}
		else {
			return putAllinMinusOne();
		}
	}
	
	////////////////////////////////////////////////////
	private Action actPostFlop() {
		if(maj.stackSize < pV(0))
			return putAllinMinusOne();
		return button ? actPostFlopButton() : actPostFlopNotButton();
	}
	
	private Action actPostFlopButton() {    //acts second
		if(eL(1)) {
			return checkFold();
		}
		else if(eL(2)) {
			if(dory.hasOpponentCheckedThisStreet()) {
				if(dory.hasOpponentRaisedThisStreet())  //he checked, I raised, he raised
					return fold();
				else
					return putPotPercentage(equity, eVals[1], eVals[2], pVals[1], pVals[2]);
			}
			else if(dory.opponentBetSizeThisStreet() <= pV(0))
				return call();
			else
				return fold();
		}
		else if(eL(4)) {
			if(dory.hasOpponentCheckedThisStreet()) {
				return putPotPercentage(equity, eVals[2], eVals[4], pVals[1], pVals[2]); 
			}
			else if(dory.opponentBetSizeThisStreet() <= pV(1)) {
				if(dory.hasOpponentRaisedThisStreet()) //he checked, I raised, he raised
					return fold();
				else 
					return putPotPercentage(equity, eVals[2], eVals[4], pVals[1], pVals[2]);
			}
			else if(dory.opponentBetSizeThisStreet() <= pV(2))
				return call();
			else
				return fold();
		}
		else if(eL(6)) {
			if(dory.hasOpponentCheckedThisStreet()) {
				if(dory.hasOpponentRaisedThisStreet())  //he checked then I raised then he raised
					return putMin();
				else
					return putPotPercentage(equity, eVals[4], eVals[6], pVals[4], pVals[6]); 
			}
			else if(dory.hasOpponentBetThisStreet()) {
				if(dory.hasOpponentRaisedThisStreet()) //he bet, I raised, he raised
					return putMin();
				else 
					return putPotPercentage(equity, eVals[4], eVals[6], pVals[5], pVals[7]);
			}
			else //should never be executed
				return putAllinMinusOne();
		}
		else 
			return putAllinMinusOne();
	}
	
	private Action actPostFlopNotButton() { //acts first
		if(eL(3)) {
			return checkFold();
		}
		else if(eL(5)) {
			if(dory.hasOpponentRaisedThisStreet()) {
				if(dory.lastOpponentRaiseSize() > pV(1))
					return fold();
				else
					return call();
			}
			else {
				return putPotPercentage(equity, eVals[3], eVals[5], pVals[1], pVals[2]);
			}
		}
		else if(eL(7)) {
			if(dory.hasOpponentRaisedThisStreet()) {
				return call();
			}
			else {
				return putPotPercentage(equity, eVals[5], eVals[7], pVals[2], pVals[4]);
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
	// Helper functions
	////////////////////////////////////////////////////
	
	private Boolean eL(int num) {
		return equity < eVals[num];
	}

	private int pV(int num) {
		return (int)(potSize * pVals[num]);
	}
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
