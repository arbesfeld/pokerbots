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
	public LegalAction raiseAction;
	
	public double timebank;
	
	private double equity;
	private boolean canCheck;
	
	private Historian maj;
	private Dory dory;
	
	private EquityCalculator ec;
	
	Brain(Historian maj) {
		this.maj = maj;
		
		lastAction = lastActions[lastActions.length - 1];
		
		for(LegalAction legalAction : legalActions) {
			switch (legalAction.getType()) {
			case "RAISE":
				raiseAction = legalAction;
				break;
			case "CHECK":
				canCheck = true;
				break;
			}
		}
		dory = new Dory(this, maj);	
	}
	
	public Action act() {
		ec.setBoard(board);
		equity = ec.calculateTotalEquity();
		
		maj.update(this);
		dory.update();
		
		if(board[2] == null) 
			return actPreFlop();
		
		else if(board[3] == null)  {
			ec.setHand(hand); //just discarded a card
			return actPostFlop();
		}
		
		else if(board[4] == null) 
			return actPostTurn();
		
		else
			return actPostRiver();
	}
	
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
			return allinMinusOne();
		}
	}
	
	private Action actPreFlopNoButton() { //big blind acts second
		if(equity < 0.5 && maj.pfr < (1.0 - equity)) {
			return checkFold();
		}
		else if(equity < 0.6) {
			if(maj.pfr < 1.0 - equity) {
				return checkFold();
			}
			else if(maj.pfr < (1.0 - equity) * 1.25) {
				return call();
			}
			else if(maj.pfr < (1.0 - equity) * 1.5) {
				return raisePotPercentage(equity, maj.pfr, (1.0 - equity) * 1.5, 
						0.5, 1.0 ); 
			}
			else {
				return raisePotPercentage(equity, maj.pfr, (1.0 - equity) * 1.5, 
						1.0, 1.5 ); 
			}
		}
		else if(equity < 0.7) {
			if(maj.pfr < 1.0 - equity) {
				return checkFold();
			}
			else if(maj.pfr < (1.0 - equity) * 1.33) {
				return call();
			}
			else if(maj.pfr < (1.0 - equity) * 1.66) {
				return raisePotPercentage( equity, maj.pfr, (1.0 - equity) * 1.66, 
						0.75, 1.25 ); 
			}
			else {
				return raisePotPercentage(equity, maj.pfr, (1.0 - equity) * 1.66, 
						1.25, 1.75 ); 
			}
		}
		else {
			return allinMinusOne();
		}
	}
	
	////////////////////////////////////////////////////
	private Action actPostFlop() {
	}
	
	
	////////////////////////////////////////////////////
	private Action actPostTurn() {
		
	}
	
	////////////////////////////////////////////////////
	private Action actPostRiver() {
		
	}
	
	
	////////////////////////////////////////////////////
	// Return various actions
	////////////////////////////////////////////////////
	private Action bet(int amount) {
		return ActionUtils.bet(amount);
	}
	
	private Action call() {
		return ActionUtils.call();
	}
	
	private Action check() {
		return ActionUtils.check();
	}
	
	private Action fold() {
		return ActionUtils.fold();
	}
	
	private Action raise(int amount) {
		return ActionUtils.raise(amount);
	}
	
	private Action allin() {
		return raise(raiseAction.getMax());
	}
	
	private Action allinMinusOne() {
		return raise(HelperUtils.minMax(raiseAction.getMax() - 1, raiseAction.getMin(), raiseAction.getMax()));
	}
	
	private Action raiseMin() {
		return raise(raiseAction.getMin());
	}
	
	private Action raisePotPercentage(double percent) {
		return raise(HelperUtils.minMax(potSize * percent, raiseAction.getMin(), raiseAction.getMax()));
	}
	
	//interpolated
	private Action raisePotPercentage(double percent, double inLo, double inHi, double outLo, double outHi) {
		double value = HelperUtils.linInterp(percent, inLo, inHi, outLo, outHi);
		return raisePotPercentage(value);
	}
	
	public Action raiseHalfPot() {
		return raisePotPercentage(0.5);
	}

	public Action raisePot() {
		return raisePotPercentage(1.0);
	}
	
	public Action raiseTwoPot() {
		return raisePotPercentage(2.0);
	}
	
	public Action raiseThreePot() {
		return raisePotPercentage(3.0);
	}
	
	public Action checkFold() {
		return canCheck ? check() : fold();
	}
	
}
