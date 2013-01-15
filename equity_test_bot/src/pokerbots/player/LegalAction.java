package pokerbots.player;

public class LegalAction extends Action {
	
	private int max, min;
	
	public LegalAction(String t) {
		super(t);
		max = 0;
		min = 0;
	}
	
	public LegalAction(String t, int small, int big) {
		super(t);
		min = small;
		max = big;
	}
	
	public int getMax() {
		return max;
	}
	
	public int getSmall() {
		return min;
	}
	
	
}
