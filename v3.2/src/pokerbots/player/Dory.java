package pokerbots.player;

import java.util.*;

public class Dory {
	ArrayList<Integer>[] theirRaiseHistory; //array of ArrayLists representing the raise history of the opp
								       //theirRaiseHistory[0] contains all raises of opp preflop, etc
	ArrayList<Integer>[] myRaiseHistory;
	boolean[] checkHistory;   		   //has opponent checked this street?
	int[] betHistory;                  //opponent can only bet once
	double pfRaiseFactor = 0.00025;
	double pfCallFactor = 0.2;
	double aggroFactor = 0.2;
	double sdwFactor = 0.1;
	double pfrDivFactor = 4.0;
	public GameState currentState;
	private Brain brain;
	private Historian maj;
	
	public double changeEquity, changeEquityThisStreet;
	public int myBets, theirBets, myBetsThisStreet, theirBetsThisStreet;
	
	enum GameState {
		PREFLOP,
		PRETURN,
		PRERIVER,
		POSTRIVER
	}
	
	Dory(Brain brain, Historian maj) {
		this.brain = brain;
		this.maj = maj;
		currentState = GameState.PREFLOP;

		myBets = 0;
		theirBets = 0;
		myBetsThisStreet = 0;
		theirBetsThisStreet = 0;
		
		betHistory = new int[4];
		Arrays.fill(betHistory, 0);
		
		checkHistory = new boolean[4];

		theirRaiseHistory = new ArrayList[4];
		myRaiseHistory = new ArrayList[4];
		
		for(int i = 0; i < 4; i++) {
			myRaiseHistory[i] = new ArrayList<Integer>(30);
			theirRaiseHistory[i] = new ArrayList<Integer>(30);
		}
		
		changeEquityThisStreet = 0.0;
		changeEquity = 0.0;
	}
	
	public void update() {
		for(int i = 0; i < brain.lastActions.length; i++) {
			PerformedAction performedAction = brain.lastActions[i];
			if(performedAction.getType().equalsIgnoreCase("RAISE")) {
				if(performedAction.getActor().equalsIgnoreCase(maj.myName))
					myRaiseAction(performedAction);
				else
					theirRaiseAction(performedAction);
			}
			else if(performedAction.getType().equalsIgnoreCase("BET"))  {
				if(performedAction.getActor().equalsIgnoreCase(maj.myName))
					myBetAction(performedAction);
				else
					theirBetAction(performedAction);
			}
			else if(performedAction.getType().equalsIgnoreCase("CALL")) {
				if(performedAction.getActor().equalsIgnoreCase(maj.myName))
					myCallAction(performedAction);
				else
					theirCallAction(performedAction);
			}
			else if(performedAction.getType().equalsIgnoreCase("CHECK")) {
				if(performedAction.getActor().equalsIgnoreCase(maj.myName))
					myCheckAction(performedAction);
				else
					theirCheckAction(performedAction);
			}
			else if(performedAction.getType().equalsIgnoreCase("POST")) {
				if(performedAction.getActor().equalsIgnoreCase(maj.myName))
					myPostAction(performedAction);
				else
					theirPostAction(performedAction);
			}
			else if(performedAction.getType().equalsIgnoreCase("STREET")) {
				streetAction(performedAction);
			}
		}
	}

	
	//my actions
	private void myPostAction(PerformedAction performedAction) {
		myBetsThisStreet += performedAction.getAmount();
	}
	
	private void myRaiseAction(PerformedAction performedAction) {
		myBetsThisStreet += performedAction.getAmount();
		
		if(performedAction.getAmount() < 11)
			return;
		
		myRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
	}
	
	private void myBetAction(PerformedAction performedAction) {
		myBetsThisStreet += performedAction.getAmount();

		if(performedAction.getAmount() < 11)
			return;
		
		myRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
	}
	
	private void myCallAction(PerformedAction performedAction) {
		myBetsThisStreet = theirBetsThisStreet;
	}

	private void myCheckAction(PerformedAction performedAction) {
	}
	
	//their actions
	
	private void theirPostAction(PerformedAction performedAction) {
		theirBetsThisStreet += performedAction.getAmount();
		//changeEquity += (maj.getSDWRate() - 1.5) * sdwFactor;
	}
	
	private void theirRaiseAction(PerformedAction performedAction) {
		if(performedAction.getAmount() < 11) {
			theirBetsThisStreet += performedAction.getAmount();
			return;
		}

		theirRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
		double PFR = maj.getPFR();
		double adjustedPFR = 0.25 / (1 - PFR);
		if(PFR > 0.5)
			adjustedPFR = PFR;
		//if(currentState == GameState.PREFLOP) { 
			changeEquity -= pfRaiseFactor * HelperUtils.logistic(maj.stackSize, maj.stackSize, (performedAction.getAmount() - theirBetsThisStreet)) / 
					((adjustedPFR - 0.5) / pfrDivFactor + 0.5) / theirRaiseHistory[currentState.ordinal()].size();
			// equity -= c * logistic(their raise) / PFR / #of their raises
		//}

		theirBetsThisStreet += performedAction.getAmount();
	}
	
	private void theirBetAction(PerformedAction performedAction) {
		theirBetsThisStreet += performedAction.getAmount();

		if(performedAction.getAmount() < 11)
			return;
		
		double PFR = maj.getPFR();
		double adjustedPFR = 0.25 / (1 - PFR);
		if(PFR > 0.5)
			adjustedPFR = PFR;
		changeEquity -= pfRaiseFactor * HelperUtils.logistic(maj.stackSize, maj.stackSize, performedAction.getAmount()) / 
				((adjustedPFR - 0.5) / pfrDivFactor + 0.5);
		theirRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
	}
	
	private void theirCallAction(PerformedAction performedAction) {
		//if(currentState == GameState.PREFLOP) { 
		changeEquity += pfCallFactor * maj.getPFR() / 
				(1.0 + myRaiseHistory[currentState.ordinal()].size() + theirRaiseHistory[currentState.ordinal()].size());
			// equity += d * pfr / (1 + total number of raises (both players))
		//}
				
		theirBetsThisStreet = myBetsThisStreet;
	}

	private void theirCheckAction(PerformedAction performedAction) {
		checkHistory[currentState.ordinal()] = true;
	}

	private void streetAction(PerformedAction performedAction) {
		myBets += myBetsThisStreet;
		myBetsThisStreet = 0;
		theirBets += theirBetsThisStreet;
		theirBetsThisStreet = 0;
		changeEquity += changeEquityThisStreet;
		changeEquityThisStreet = 0.0;
		if(performedAction.getStreet().equalsIgnoreCase("FLOP")) {
			currentState = GameState.PRETURN;
		}
		else if(performedAction.getStreet().equalsIgnoreCase("TURN")) {
			currentState = GameState.PRERIVER;
		}
		else if(performedAction.getStreet().equalsIgnoreCase("RIVER")) {
			currentState = GameState.POSTRIVER;
		}
	}
	
	public int lastOpponentRaiseSize() {
		return theirRaiseHistory[currentState.ordinal()].get(theirRaiseHistory[currentState.ordinal()].size() - 1);
	}
	
	public boolean hasOpponentRaisedThisStreet() {
		return theirRaiseHistory[currentState.ordinal()].size() > 0;
	}

	public boolean hasOpponentRaisedTwiceThisStreet() {
		return theirRaiseHistory[currentState.ordinal()].size() > 1;
	}
	
	public boolean hasOpponentCheckedThisStreet() {
		return checkHistory[currentState.ordinal()];
	}
	public boolean hasOpponentBetThisStreet() {
		return betHistory[currentState.ordinal()] > 0;
	}
	public int opponentBetSizeThisStreet() {
		return betHistory[currentState.ordinal()];
	}
	public double potOdds() {
		int amountToPut = theirBetsThisStreet - myBetsThisStreet;
		return (double)amountToPut / (amountToPut + brain.potSize); 
	}
	public double liqEquity() {
		return brain.equity + changeEquity + changeEquityThisStreet;
	}
}
