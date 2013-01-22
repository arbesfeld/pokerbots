package pokerbots.player;

public class Card implements Comparable<Card>{
	/* val range from 2 to 14
	 * 11 - jack
	 * 12 - queen
	 * 13 - king
	 * 14 - ace
	 */
	public int val;
	
	/* suit range from 0 to 3
	 * 0 - heart
	 * 1 - diamond
	 * 2 - spade
	 * 3 - club
	 */
	public int suit;
	
	// ranges from 0 to 51
	public int id;
	
	Card(int val, int suit) {
		this.val = val;
		this.suit = suit;
		id = suit * 13 + val - 2;
	}
	
	public void print() {
		String str = "";
		switch (val) {
		case 11:
			str = "Jack ";
			break;
		case 12:
			str = "Queen ";
			break;
		case 13:
			str = "King ";
			break;
		case 14:
			str = "Ace ";
			break;
		default:
			str += val;
		}
		str += " of ";
		switch(suit) {
		case 0:
			str += "hearts.";
			break;
		case 1:
			str += "diamonds.";
			break;
		case 2:
			str += "spades.";
			break;
		case 3:
			str += "clubs.";
			break;
		}
		System.out.println(str);
	}
	
	public int compareTo(Card other) {
		return this.val - other.val;
	}
	
}
