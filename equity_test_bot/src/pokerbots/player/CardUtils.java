package pokerbots.player;

public class CardUtils {
	static Card getCardByID(int id) {
		return new Card(id % 13 + 2, id / 13);
	}
	static Card getCardByString(String card) {
		int val = 0, suit = 0;
		
		char char1 = card.charAt(0);
		if(char1 == 'T')
			val = 10;
		else if(char1 == 'J')
			val = 11;
		else if(char1 == 'Q')
			val = 12;
		else if(char1 == 'K')
			val = 13;
		else if(char1 == 'A')
			val = 14;
		else
			val = char1 - '0';
		
		char char2 = card.charAt(1);
		if(char2 == 'h')
			suit = 0;
		else if(char2 == 'd')
			suit = 1;
		else if(char2 == 's')
			suit = 2;
		else
			suit = 3;
		
		return new Card(val, suit);
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
