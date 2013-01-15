package pokerbots.player;

public class Historian {
	//instantiated every match
	public String myName, oppName; //historian
	public int stackSize, bb; //historian
	
	public Historian(String myName, String oppName, int stackSize, int bb) {
		this.myName = myName;
		this.oppName = oppName;
		this.stackSize = stackSize;
		this.bb = bb;
	}
	
	
}
