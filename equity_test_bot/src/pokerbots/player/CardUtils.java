package pokerbots.player;

public class CardUtils {
	static Card getCardByID(int id) {
		return new Card(id % 13 + 2, id / 13);
	}
	static Card max(Card a, Card b) {
		if(a.compareTo(b) >= 0)
			return a;
		else
			return b;
	}
	static Card min(Card a, Card b) {
		if(a.compareTo(b) >= 0)
			return b;
		else
			return a;
	}
	static void print(Card[] a) {
		for(int i = 0; i < a.length; i++)
			a[i].print();
		System.out.println();
	}
}
