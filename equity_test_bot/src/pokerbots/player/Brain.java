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
	
	public PerformedAction lastAction;
	public LegalAction raiseAction, betAction;
	
	public double timebank;
	
	private double equity;
	private boolean canCheck, canDiscard;
	
	private Historian maj;
	private Dory dory;
	
	private EquityCalculator ec;
	
	Brain(Historian maj) {
		this.maj = maj;
		
		
		dory = new Dory(this, maj);	
	}
	
//	public Action act() {
//		
//		lastAction = lastActions[lastActions.length - 1];
//		
//		for(LegalAction legalAction : legalActions) {
//			if(legalAction.getType().equalsIgnoreCase("RAISE")) {
//				raiseAction = legalAction;
//			}
//			else if(legalAction.getType().equalsIgnoreCase("BET")) {
//				betAction = legalAction;
//			}
//			else if(legalAction.getType().equalsIgnoreCase("CHECK")) {
//				canCheck = true;
//			}
//			else if(legalAction.getType().equalsIgnoreCase("DISCARD")) {
//				canDiscard = true;
//			}
//		}
//		
//		if(lastAction.getType().equalsIgnoreCase("DEAL")) {
//			ec.setBoard(board);
//			equity = ec.calculateTotalEquity();
//		}
//		
//		maj.update(this);
//		dory.update();
//		
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
//	}
	
//	private Action discardCard() {
//		
//	}
	
	private Action actPreFlop() {
		return button ? actPreFlopButton() : actPreFlopNoButton();
	}
	
	private Action actPreFlopButton() { //small blind acts first
		if(equity < 0.5) {
			if(dory.hasOpponentRaised()) { // opponent raised
				if(equity < 0.45 || 
						dory.lastOpponentRaiseSize() > maj.stackSize / 15)
					return fold();
				else // an amount less than 20
					return call(); 
			}
			else {
				return call();
			}
		}
		else if(equity < 0.6) { 
			if(dory.hasOpponentRaised())
				return fold();
			else
				return raise(HelperUtils.linInterp(equity, 0.55, 0.6, maj.stackSize / 16, maj.stackSize / 8));
		}
		else if(equity < 0.7) {
			if(dory.hasOpponentRaised())
				return call();
			else
				return raise(HelperUtils.linInterp(equity, 0.6, 0.7, maj.stackSize / 8, maj.stackSize / 4));
		}
		else {
			return putAllinMinusOne();
		}
	}
	
	private Action actPreFlopNoButton() { //big blind acts second
		if(equity < 0.5 && maj.getPFR() < (1.0 - equity)) {
			return checkFold();
		}
		else if(equity < 0.6) {
			if(maj.getPFR() < 1.0 - equity) {
				return checkFold();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.25) {
				return call();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.5) {
				return raisePotPercentage(equity, maj.getPFR(), (1.0 - equity) * 1.5, 
						0.5, 1.0 ); 
			}
			else {
				return raisePotPercentage(equity, maj.getPFR(), (1.0 - equity) * 1.5, 
						1.0, 1.5 ); 
			}
		}
		else if(equity < 0.7) {
			if(maj.getPFR() < 1.0 - equity) {
				return checkFold();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.33) {
				return call();
			}
			else if(maj.getPFR() < (1.0 - equity) * 1.66) {
				return raisePotPercentage( equity, maj.getPFR(), (1.0 - equity) * 1.66, 
						0.75, 1.25 ); 
			}
			else {
				return raisePotPercentage(equity, maj.getPFR(), (1.0 - equity) * 1.66, 
						1.25, 1.75 ); 
			}
		}
		else {
			return putAllinMinusOne();
		}
	}
	
	////////////////////////////////////////////////////
//	private Action actPostFlop() {
//		
//	}
//	
//	
//	////////////////////////////////////////////////////
//	private Action actPostTurn() {
//		
//	}
//	
//	////////////////////////////////////////////////////
//	private Action actPostRiver() {
//		
//	}
//	
	
	////////////////////////////////////////////////////
	// Return various actions
	////////////////////////////////////////////////////
	
	private Action call() {
		return ActionUtils.call();
	}
	
	private Action check() {
		return ActionUtils.check();
	}
	
	private Action fold() {
		return ActionUtils.fold();
	}
	
	//////////////////////
	///////RAISING/BETTING (whichever is allowed)
	//////////////////////
	private Action put(int amount) {
		return ActionUtils.raise(amount);
	}
	
	private Action putAllin() {
		return put(raiseAction.getMax());
	}
	
	private Action putAllinMinusOne() {
		return raise(HelperUtils.minMax(raiseAction.getMax() - 1, raiseAction.getMin(), raiseAction.getMax()));
	}
	
	private Action putMin() {
		return raise(raiseAction.getMin());
	}
	
	private Action putPotPercentage(double percent) {
		return raise(HelperUtils.minMax(potSize * percent, raiseAction.getMin(), raiseAction.getMax()));
	}
	
	private Action putPotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		double value = HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi);
		return raisePotPercentage(value);
	}
	
	private Action putHalfPot() {
		return raisePotPercentage(0.5);
	}

	private Action putPot() {
		return raisePotPercentage(1.0);
	}
	
	private Action putTwoPot() {
		return raisePotPercentage(2.0);
	}
	
	private Action putThreePot() {
		return raisePotPercentage(3.0);
	}
	
	//////////////
	///////RAISING
	//////////////
	
	private Action raise(int amount) {
		return ActionUtils.raise(amount);
	}
	
	private Action raiseAllin() {
		return raise(raiseAction.getMax());
	}
	
	private Action raiseAllinMinusOne() {
		return raise(HelperUtils.minMax(raiseAction.getMax() - 1, raiseAction.getMin(), raiseAction.getMax()));
	}
	
	private Action raiseMin() {
		return raise(raiseAction.getMin());
	}
	
	private Action raisePotPercentage(double percent) {
		return raise(HelperUtils.minMax(potSize * percent, raiseAction.getMin(), raiseAction.getMax()));
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
		return ActionUtils.bet(amount);
	}
	
	private Action betAllin() {
		return bet(betAction.getMax());
	}
	
	private Action betAllinMinusOne() {
		return raise(HelperUtils.minMax(betAction.getMax() - 1, betAction.getMin(), betAction.getMax()));
	}
	
	private Action betMin() {
		return bet(betAction.getMin());
	}
	
	private Action betPotPercentage(double percent) {
		return bet(HelperUtils.minMax(potSize * percent, betAction.getMin(), betAction.getMax()));
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
	
}
