package pokerbots.player;

public class Action {
	
	private String myType;
	
	public Action(String t) {
		myType = t;
	}
	
	public String getType() {
		return myType;
	}
	
	public void processAction() { //might need to change return type
		// overridden in child classes
	}
}
