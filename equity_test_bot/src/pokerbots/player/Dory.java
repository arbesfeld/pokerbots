package pokerbots.player;

import java.util.*;

public class Dory {
	ArrayList<Integer>[] raiseHistory = new ArrayList[4]; //array of ArrayLists representing the raise history of the opp
														  //raiseHistory[0] contains all raises of opp preflop, etc
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
		
		for(int i = 0; i < 4; i++) {
			raiseHistory[i] = new ArrayList<Integer>(30);
		}
		
	}
	
	void update() {
		if(brain.lastAction.getActor() == null || 
				brain.lastAction.getActor() == maj.myName)
			return;
		if(brain.lastAction.getType().equalsIgnoreCase("RAISE")) 
			raiseAction();
		else if(brain.lastAction.getType().equalsIgnoreCase("CALL"))
			callAction();
		else if(brain.lastAction.getType().equalsIgnoreCase("CHECK"))
			checkAction();
		else if(brain.lastAction.getType().equalsIgnoreCase("STREET"))
			streetAction();
	}
	
	void raiseAction() {
		raiseHistory[currentState.ordinal()].add(brain.lastAction.getAmount());
	}
	
	int lastOpponentRaiseSize() {
		return raiseHistory[currentState.ordinal()].get(raiseHistory[currentState.ordinal()].size() - 1);
	}
	
	boolean hasOpponentRaised() {
		return raiseHistory[currentState.ordinal()].size() > 0;
	}
	
	void callAction() {
		
	}
	
	void checkAction() {
		
	}
	
	void streetAction() {
		switch(brain.lastAction.getStreet()) {
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
}
