package pokerbots.player;

import java.util.*;

public class Dory {
	ArrayList<Integer>[] theirRaiseHistory; //array of ArrayLists representing the raise history of the opp
								       //theirRaiseHistory[0] contains all raises of opp preflop, etc
	ArrayList<Integer>[] myRaiseHistory;
	boolean[] checkHistory;   		   //has opponent checked this street?
	int[] betHistory;                  //opponent can only bet once
	double[] pfRaiseFactor;
	double[] pfCallFactor;
	double[] pfCheckFactor;
	
	double pfrDivFactor;
	double aggroDivFactor;
	double sdwFactor;
	public GameState currentState;
	private Brain brain;
	private Historian maj;
	
	public double changeEquity, changeEquityCall;
	public int myBets, theirBets, myBetsThisStreet, theirBetsThisStreet, numTheirCallChecks;
	
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
		numTheirCallChecks = 0;
		
		pfRaiseFactor = new double[]{0.00026, 0.00015, 0.00015, 0.00015};
		pfCheckFactor = new double[]{0.2, 0.1, 0.1, 0.1};
		pfCallFactor = new double[4];
		for(int i = 0; i < 4; i++)
			pfCallFactor[i] = pfCheckFactor[i];
		
		pfrDivFactor = 1.0;
		aggroDivFactor = 1.0;
		sdwFactor = 0.1;
		
		betHistory = new int[4];
		Arrays.fill(betHistory, 0);
		
		checkHistory = new boolean[4];

		theirRaiseHistory = new ArrayList[4];
		myRaiseHistory = new ArrayList[4];
		
		for(int i = 0; i < 4; i++) {
			myRaiseHistory[i] = new ArrayList<Integer>(30);
			theirRaiseHistory[i] = new ArrayList<Integer>(30);
		}
		
		changeEquityCall = 0.0;
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
			else if(performedAction.getType().equalsIgnoreCase("DEAL")) {
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
		double littleFactor = 1.0;
		if(performedAction.getAmount() - theirBetsThisStreet < 11)
			return;

		theirRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
		
		double PFR = maj.getPFR();
		double adjustedPFR = (0.25 / (1 - PFR) - 0.5) / pfrDivFactor + 0.5;
		
		double aggro = maj.getPostFlopRaise();
		double adjustedAggro = (0.25 / (1 - aggro) - 0.5) / aggroDivFactor + 0.5;
		
		if(PFR > 0.5)
			adjustedPFR = PFR;
		
		if(aggro > 0.5)
			adjustedAggro = aggro;
		
		if(currentState == GameState.PREFLOP) {
			changeEquity -= littleFactor * pfRaiseFactor[0] * HelperUtils.logistic(maj.stackSize, maj.stackSize, (performedAction.getAmount() - theirBetsThisStreet)) / 
					adjustedPFR;
				// equity -= c * logistic(their raise) / PFR / #of their raises
		}
		else if (currentState == GameState.PRETURN){
			changeEquity -= littleFactor * pfRaiseFactor[1] * HelperUtils.logistic(maj.stackSize, maj.stackSize, (performedAction.getAmount() - theirBetsThisStreet)) / 
					adjustedAggro;
		}
		else if (currentState == GameState.PRERIVER){
			changeEquity -= littleFactor * pfRaiseFactor[2] * HelperUtils.logistic(maj.stackSize, maj.stackSize, (performedAction.getAmount() - theirBetsThisStreet)) / 
					adjustedAggro;
		}
		else if (currentState == GameState.POSTRIVER){
			changeEquity -= littleFactor * pfRaiseFactor[3] * HelperUtils.logistic(maj.stackSize, maj.stackSize, (performedAction.getAmount() - theirBetsThisStreet)) / 
					adjustedAggro;
		}
		
		changeEquity -= changeEquityCall;
		changeEquityCall = 0.0;
		
		theirBetsThisStreet += performedAction.getAmount();
	}
	
	private void theirBetAction(PerformedAction performedAction) {
		double littleFactor = 1.0;
		if(performedAction.getAmount() - theirBetsThisStreet < 11)
			return;
		
		double PFR = maj.getPFR();
		double adjustedPFR = (0.25 / (1 - PFR) - 0.5) / pfrDivFactor + 0.5;
		
		double aggro = maj.getPostFlopRaise();
		double adjustedAggro = (0.25 / (1 - aggro) - 0.5) / aggroDivFactor + 0.5;
		
		if(PFR > 0.5)
			adjustedPFR = PFR;
		
		if(aggro > 0.5)
			adjustedAggro = aggro;
		
		if(currentState == GameState.PREFLOP) {
			changeEquity -= littleFactor * pfRaiseFactor[0] * HelperUtils.logistic(maj.stackSize, maj.stackSize, performedAction.getAmount()) / adjustedPFR;
		}
		else if(currentState == GameState.PRETURN) {
			changeEquity -= littleFactor * pfRaiseFactor[1] * HelperUtils.logistic(maj.stackSize, maj.stackSize, performedAction.getAmount()) / adjustedAggro;
		}
		else if(currentState == GameState.PRERIVER) {
			changeEquity -= littleFactor * pfRaiseFactor[2] * HelperUtils.logistic(maj.stackSize, maj.stackSize, performedAction.getAmount()) / adjustedAggro;
		}
		else if(currentState == GameState.POSTRIVER) {
			changeEquity -= littleFactor * pfRaiseFactor[3] * HelperUtils.logistic(maj.stackSize, maj.stackSize, performedAction.getAmount()) / adjustedAggro;
		}
		
		changeEquity -= changeEquityCall;
		changeEquityCall = 0.0;

		theirBetsThisStreet += performedAction.getAmount();
		theirRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
	}
	
	private void theirCallAction(PerformedAction performedAction) {
		numTheirCallChecks++;
		int callAmt = myBetsThisStreet - theirBetsThisStreet;
		
		if(changeEquityCall > 0)
			changeEquity -= changeEquityCall;
		
		double PFR = maj.getPFR();
		double adjustedPFR = (0.25 / (1 - PFR) - 0.5) / pfrDivFactor + 0.5;
		
		double aggro = maj.getPostFlopRaise();
		double adjustedAggro = (0.25 / (1 - aggro) - 0.5) / aggroDivFactor + 0.5;
		
		if(PFR > 0.5)
			adjustedPFR = PFR;
		
		if(aggro > 0.5)
			adjustedAggro = aggro;
		
		// equity += d * pfr / (1 + total number of raises (both players))
		if(currentState == GameState.PREFLOP) { 
			changeEquityCall = pfCallFactor[0] * adjustedPFR / 
					(1.0 + myRaiseHistory[currentState.ordinal()].size() + theirRaiseHistory[currentState.ordinal()].size()) / numTheirCallChecks;
		}
		else if(currentState == GameState.PRETURN){
			changeEquityCall = pfCallFactor[1] * adjustedAggro / 
					(1.0 + myRaiseHistory[currentState.ordinal()].size() + theirRaiseHistory[currentState.ordinal()].size()) / numTheirCallChecks;
		}
		else if(currentState == GameState.PRERIVER){
			changeEquityCall = pfCallFactor[2] * adjustedAggro / 
					(1.0 + myRaiseHistory[currentState.ordinal()].size() + theirRaiseHistory[currentState.ordinal()].size()) / numTheirCallChecks;
		}
		else if(currentState == GameState.POSTRIVER){
			changeEquityCall = pfCallFactor[3] * adjustedAggro / 
					(1.0 + myRaiseHistory[currentState.ordinal()].size() + theirRaiseHistory[currentState.ordinal()].size()) / numTheirCallChecks;
			
		}		
		
		changeEquity += changeEquityCall;
		theirBetsThisStreet = myBetsThisStreet;
	}

	private void theirCheckAction(PerformedAction performedAction) {
		numTheirCallChecks++;
		checkHistory[currentState.ordinal()] = true;
		

		double PFR = maj.getPFR();
		double adjustedPFR = (0.25 / (1 - PFR) - 0.5) / pfrDivFactor + 0.5;
		
		double aggro = maj.getPostFlopRaise();
		double adjustedAggro = (0.25 / (1 - aggro) - 0.5) / aggroDivFactor + 0.5;
		
		if(PFR > 0.5)
			adjustedPFR = PFR;
		
		if(aggro > 0.5)
			adjustedAggro = aggro;
		
		if(currentState == GameState.PREFLOP) { 
			changeEquityCall = pfCheckFactor[0] * adjustedPFR / numTheirCallChecks;
		}
		else if(currentState == GameState.PRETURN){
			changeEquityCall = pfCheckFactor[1] * adjustedAggro / numTheirCallChecks;
		}
		else if(currentState == GameState.PRERIVER){
			changeEquityCall = pfCheckFactor[2] * adjustedAggro / numTheirCallChecks;
		}
		else if(currentState == GameState.POSTRIVER){
			changeEquityCall = pfCheckFactor[3] * adjustedAggro / numTheirCallChecks;
			
		}
		
		changeEquity += changeEquityCall;
	}

	private void streetAction(PerformedAction performedAction) {
		myBets += myBetsThisStreet;
		myBetsThisStreet = 0;
		theirBets += theirBetsThisStreet;
		theirBetsThisStreet = 0;
		changeEquityCall = 0.0;
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
		return brain.equity + changeEquity;
	}
}
