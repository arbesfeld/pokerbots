package pokerbots.player;


public class PerformedAction extends Action {
	
	private String actor;
	private int amount;
	public String street;
	public Card discard;
	public Card show1, show2;
	
	public PerformedAction(String t) {
		super(t);
		actor = null;
		amount = 0;
		street = null;
		discard = null;
		show1 = null;
		show2 = null;
	}
	
	public void setActor(String a) {
		actor = a;
	}
	
	public void setAmount(int a) {
		amount = a;
	}
	
	public void setStreet(String s) {
		street = s;
	}
	
	public void setDiscard(Card d) {
		discard = d;
	}
	
	public void setShowCards(Card c1, Card c2) {
		show1 = c1;
		show2 = c2;
	}
	
	public String getActor() {
		return actor;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public String getStreet() {
		return street;
	}
	
	public Card getDiscard() {
		return discard;
	}
	
	public Card[] getShowCards() {
		return new Card[] {show1, show2};
	}
	
}
	
	