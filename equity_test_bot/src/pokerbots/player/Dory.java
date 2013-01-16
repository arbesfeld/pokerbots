package pokerbots.player;

import java.util.*;

public class Dory {
	ArrayList<Integer>[] raiseHistory = new ArrayList[4]; //array of ArrayLists representing the raise history of the opp
														  //raiseHistory[0] contains all raises of opp preflop, etc
	int street;
	Brain brain;
	Historian maj;
	
	Dory(Brain brain, Historian maj) {
		this.brain = brain;
		this.maj = maj;
		
		for(int i = 0; i < 4; i++) {
			raiseHistory[i] = new ArrayList<Integer>(30);
		}
		
		street = 0;
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
		raiseHistory[street].add(brain.lastAction.getAmount());
	}
	
	int lastOpponentRaiseSize() {
		return raiseHistory[street].get(raiseHistory[street].size() - 1);
	}
	
	boolean hasOpponentRaised() {
		return raiseHistory[street].size() > 0;
	}
	
	void callAction() {
		
	}
	
	void checkAction() {
		
	}
	
	void streetAction() {
		switch(brain.lastAction.getStreet()) {
		case "FLOP":
			street = 1;
			break;
		case "TURN":
			street = 2;
			break;
		case "RIVER":
			street = 3;
			break;
		}
	}
}
