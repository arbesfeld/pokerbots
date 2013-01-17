package pokerbots.player;

import java.util.*;

public class Dory {
	ArrayList<Integer>[] raiseHistory; //array of ArrayLists representing the raise history of the opp
								       //raiseHistory[0] contains all raises of opp preflop, etc
	boolean[] checkHistory;   		   //has opponent checked this street?
	int[] betHistory;                  //opponent can only bet once
	public GameState currentState;
	private Brain brain;
	private Historian maj;
	
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
		
		betHistory = new int[4];
		Arrays.fill(betHistory, 0);
		
		checkHistory = new boolean[4];

		raiseHistory = new ArrayList[4];
		for(int i = 0; i < 4; i++) {
			raiseHistory[i] = new ArrayList<Integer>(30);
		}
		
	}
	
	public void update() {
		for(int i = 1; i < brain.lastActions.length; i++) {
			PerformedAction performedAction = brain.lastActions[i];
			if(performedAction.getType().equalsIgnoreCase("RAISE")) 
				raiseAction(performedAction);
			else if(performedAction.getType().equalsIgnoreCase("BET")) 
				betAction(performedAction);
			else if(performedAction.getType().equalsIgnoreCase("CALL"))
				callAction(performedAction);
			else if(performedAction.getType().equalsIgnoreCase("CHECK"))
				checkAction(performedAction);
			else if(performedAction.getType().equalsIgnoreCase("STREET"))
				streetAction(performedAction);
		}
	}
	
	private void raiseAction(PerformedAction performedAction) {
		raiseHistory[currentState.ordinal()].add(performedAction.getAmount());
	}
	
	private void betAction(PerformedAction performedAction) {
		betHistory[currentState.ordinal()] = performedAction.getAmount();
	}
	
	private void callAction(PerformedAction performedAction) {
		
	}

	private void checkAction(PerformedAction performedAction) {
		checkHistory[currentState.ordinal()] = true;
	}

	private void streetAction(PerformedAction performedAction) {
		switch(performedAction.getStreet()) {
		case "FLOP":
			currentState = GameState.PRETURN;
			break;
		case "TURN":
			currentState = GameState.PRERIVER;
			break;
		case "RIVER":
			currentState = GameState.POSTRIVER;
			break;
		}
	}
	
	public int lastOpponentRaiseSize() {
		return raiseHistory[currentState.ordinal()].get(raiseHistory[currentState.ordinal()].size() - 1);
	}
	
	public boolean hasOpponentRaisedThisStreet() {
		return raiseHistory[currentState.ordinal()].size() > 0;
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
}
