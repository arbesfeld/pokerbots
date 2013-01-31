package pokerbots.player;

import java.util.*;

public class Dory {
	ArrayList<Integer>[] theirRaiseHistory; //array of ArrayLists representing the raise history of the opp
								       //theirRaiseHistory[0] contains all raises of opp preflop, etc
	ArrayList<Integer>[] myRaiseHistory;
	boolean[] checkHistory;   		   //has opponent checked this street?
	int[] betHistory;                  //opponent can only bet once
	double pfRaiseFactor = 0.00026;
	double pfCallFactor = 0.05;
	double pfCheckFactor = 0.15;
	double callRaiseFactor = 0.02;
	
	double aggroFactor = 0.2;
	double sdwFactor = 0.3;
	
	double pfrDivFactor = 1.0;
	double aggroDivFactor = 1.0;
	double aggroFreqDivFactor = 1.0;
	
	public GameState currentState;
	private Brain brain;
	private Historian maj;
	
	public double changeEquity, changeEquityCall, changeEquityTemp;
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
		
		changeEquityCall = 0.0;
		changeEquity = 0.0;
		changeEquityTemp = 0.0;
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
		
		myRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
	}
	
	private void myBetAction(PerformedAction performedAction) {
		myBetsThisStreet += performedAction.getAmount();

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
		changeEquity += (maj.getSDWRate() - 0.6) * sdwFactor;
	}
	
	private void theirRaiseAction(PerformedAction performedAction) {

		double potBet = (double)(performedAction.getAmount() - theirBetsThisStreet) / (brain.potSize - performedAction.getAmount());
		if(potBet < 0.2) {
			theirBetsThisStreet += performedAction.getAmount();
			return;
		}
		
		theirRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
		
		double PFR = adjustPFR(maj.getPFR());
		double AGGRO = adjustAggro(maj.getAggression());
		double AGGROFREQ = adjustAggroFreq(maj.getAggressionFrequency());
		
		double logFactor = HelperUtils.logisticSmall(3.0, 3.0, potBet) * 
				           Math.min(100.0, (HelperUtils.logistic(400.0, 400.0, performedAction.getAmount() - theirBetsThisStreet) + 300.0) / 4);
		
		double THREEB = maj.get3BetRate();
		double TWOB = maj.get2BetRate();
		
		if(myRaiseHistory[currentState.ordinal()].size() == 1 && theirRaiseHistory[currentState.ordinal()].size() == 1) {
			logFactor *= 1.0 + (0.2 - TWOB) * Math.min(100.0, myRaiseHistory[currentState.ordinal()].get(0)) / 30.0;
		}
		else if(theirRaiseHistory[currentState.ordinal()].size() == 2) {
			logFactor *= 1.0 + (0.25 - THREEB) * 1.5;
		}
		else if(theirRaiseHistory[currentState.ordinal()].size() >= 3) {
			logFactor *= 1.0 + (0.25 - THREEB) * 3.0;
		}
		
		if(currentState == GameState.PREFLOP) { 
			changeEquity -= pfRaiseFactor * logFactor / PFR;
		}
		else {
			changeEquity -= pfRaiseFactor * logFactor / AGGROFREQ;
		}
		
		changeEquityCall = 0.0;
		theirBetsThisStreet += performedAction.getAmount();
	}
	
	private void theirBetAction(PerformedAction performedAction) {
		theirBetsThisStreet += performedAction.getAmount();
		
		double potBet = (double)performedAction.getAmount() / (brain.potSize - performedAction.getAmount());

		if(potBet < 0.2) 
			return;
		
		theirRaiseHistory[currentState.ordinal()].add(performedAction.getAmount());
		
		double PFR = adjustPFR(maj.getPFR());
		double AGGRO = adjustAggro(maj.getAggression());
		double AGGROFREQ = adjustAggroFreq(maj.getAggressionFrequency());
		
		double logFactor = HelperUtils.logisticSmall(3.0, 3.0, potBet) * 
		                   Math.min(100.0, (HelperUtils.logistic(400.0, 400.0, performedAction.getAmount()) + 300.0) / 4);
		
		if(currentState == GameState.PREFLOP) {
			changeEquity -= pfRaiseFactor * logFactor / PFR;
		}
		else {
			changeEquity -= pfRaiseFactor * logFactor / AGGROFREQ;
		}
		
		changeEquityCall = 0.0;
	}
	
	private void theirCallAction(PerformedAction performedAction) {
		//if(currentState == GameState.PREFLOP) { 
		double PFR = adjustPFR(maj.getPFR());
		double CALLRAISE = maj.getCallRaise();
		
//		if(currentState == GameState.PREFLOP) {
//			changeEquityTemp -= callRaiseFactor * CALLRAISE;
//		}
		
		//changeEquityCall = pfCallFactor * adjustedPFR / (1.0 + theirRaiseHistory[currentState.ordinal()].size() + myRaiseHistory[currentState.ordinal()].size());
			// equity += d * pfr / (1 + total number of raises (both players))
		//}
				
		theirBetsThisStreet = myBetsThisStreet;
	}

	private void theirCheckAction(PerformedAction performedAction) {
		checkHistory[currentState.ordinal()] = true;
		
		double PFR = adjustPFR(maj.getPFR());
		double AGGRO = adjustAggro(maj.getAggression());
		double AGGROFREQ = adjustAggroFreq(maj.getAggressionFrequency());
		double CHECKRAISE = maj.getCheckRaise();
		
		if(currentState == GameState.PREFLOP) {
			changeEquityCall = pfCheckFactor * PFR * (1.0 - 4 * CHECKRAISE);
		}
		else {
			changeEquityCall = pfCheckFactor * AGGROFREQ * (1.0 - 4 * CHECKRAISE);
		}
	}

	private void streetAction(PerformedAction performedAction) {
		myBets += myBetsThisStreet;
		myBetsThisStreet = 0;
		theirBets += theirBetsThisStreet;
		theirBetsThisStreet = 0;
		
		changeEquity += changeEquityCall;
		
		changeEquityCall = 0.0;
		changeEquityTemp = 0.0;
		
		if(performedAction.getStreet().equalsIgnoreCase("FLOP")) {
			currentState = GameState.PRETURN;

			//changeEquity += (maj.getSDWRate() - 0.6) * sdwFactor / 2.0;
		}
		else if(performedAction.getStreet().equalsIgnoreCase("TURN")) {
			currentState = GameState.PRERIVER;

			changeEquity += (maj.getSDWRate() - 0.6) * sdwFactor / 4.0;
		}
		else if(performedAction.getStreet().equalsIgnoreCase("RIVER")) {
			currentState = GameState.POSTRIVER;

			changeEquity += (maj.getSDWRate() - 0.6) * sdwFactor / 4.0;
		}
	}
	
	private double adjustPFR(double PFR) {
		if (PFR > 0.5)
			return (PFR - 0.5) / pfrDivFactor + 0.5;
		else
			return  (0.25 / (1 - PFR) - 0.5) / pfrDivFactor + 0.5;
	}
	
	private double adjustAggro(double AGGRO) {
		AGGRO /= 3.0;
		
		if (AGGRO > 0.5)
			return Math.min(3.0, (AGGRO - 0.5) / aggroDivFactor + 0.5);
		else
			return (0.25 / (1 - AGGRO) - 0.5) / aggroDivFactor + 0.5;
	}
	
	private double adjustAggroFreq(double AGGROFREQ) {
		if (AGGROFREQ > 0.5)
			return (AGGROFREQ - 0.5) / aggroFreqDivFactor + 0.5;
		else
			return  (0.25 / (1 - AGGROFREQ) - 0.5) / aggroFreqDivFactor + 0.5;
	}
	public double potOdds() {
		int amountToPut = theirBetsThisStreet - myBetsThisStreet;
		return (double)amountToPut / (amountToPut + brain.potSize); 
	}
	public double liqEquity() {
		return brain.equity + changeEquity + changeEquityCall + changeEquityTemp;
	}
	
}
