package pokerbots.player;

import java.util.*;

public class EquityUtils {
	//[:,0] - second card is Ace, [:,1] - second card King... [:,12] - second card 2
	static HashMap<String, Double> threeSuited;
	static private double [][] suited =
		{{0,67.05,66.21,65.4,64.6,62.78,61.94,60.98,59.91,59.92,59.03,58.22,57.38},		//A
		{67.05,0,63.4,62.57,61.79,59.99,58.31,57.54,56.64,55.79,54.89,54.06,53.21},		//K
		{66.21,63.4,0,60.26,59.47,57.66,56.02,54.3,53.61,52.77,51.86,51.02,50.17},		//Q
		{65.4,62.57,60.26,0,57.53,55.66,54.02,52.33,50.61,49.99,49.07,48.23,47.38},		//J
		{64.6,61.79,59.47,57.53,0,54.03,52.33,50.64,48.94,47.22,46.53,45.69,44.84},		//10
		{62.78,59.99,57.66,55.66,54.03,0,50.8,49.12,47.43,45.72,43.86,43.26,42.42},		//9
		{61.94,58.31,56.02,54.02,52.33,50.8,0,47.94,46.24,44.55,42.7,40.87,40.27},		//8
		{60.98,57.54,54.3,52.33,50.64,49.12,47.94,0,45.37,43.68,41.85,40.04,38.16},		//7
		{59.91,56.64,53.61,50.61,48.94,47.43,46.24,45.37,0,43.13,41.33,39.53,37.67},	//6
		{59.92,55.79,52.77,49.99,47.22,45.72,44.55,43.68,43.13,0,41.45,39.69,37.85},	//5
		{59.03,54.89,51.86,49.07,46.53,43.86,42.7,41.85,41.33,41.45,0,38.64,36.83},		//4
		{58.22,54.06,51.02,48.23,45.69,43.26,40.87,40.04,39.53,39.69,38.64,0,35.98},	//3
		{57.38,53.21,50.17,47.38,44.84,42.42,40.27,38.16,37.67,37.85,36.83,35.98,0}		//2
		};
	static private double [][] unsuited =
		{{85.2,65.32,64.43,63.56,62.72,60.77,59.87,58.84,57.68,57.7,56.73,55.85,54.93},	//A
		{65.32,82.4,61.46,60.57,59.4,57.81,56.02,55.19,54.22,53.31,52.33,51.43,50.51},	//K
		{64.43,61.46,79.93,58.14,57.3,55.36,53.6,51.77,51.02,50.12,49.13,48.22,47.3},	//Q
		{63.56,60.57,58.14,77.47,55.29,53.25,51.49,49.68,47.84,47.18,46.19,45.28,44.35},//J
		{62.72,59.4,57.3,55.29,75.01,51.53,49.72,47.91,46.1,44.25,43.5,42.6,41.69},		//10
		{60.77,57.81,55.36,53.25,51.53,72.06,48.1,46.3,44.49,42.67,40.67,40.02,39.1},	//9
		{59.87,56.02,53.6,51.49,49.72,48.1,69.16,45.05,43.24,41.43,39.47,37.48,36.83},	//8
		{58.84,55.19,51.77,49.68,47.91,46.3,45.05,66.24,42.32,40.51,38.55,36.6,34.58},	//7
		{57.68,54.22,51.02,47.84,46.1,44.49,43.24,42.32,63.29,39.94,38.01,36.08,34.08},	//6
		{57.7,53.31,50.12,47.18,44.25,42.67,41.43,40.51,39.94,60.33,38.16,36.27,34.29},	//5
		{56.73,52.33,49.13,46.19,43.5,40.67,39.47,38.55,38.01,38.16,57.02,35.15,33.2},	//4
		{55.85,51.43,48.22,45.28,42.6,40.02,37.48,36.6,36.08,36.27,35.15,53.69,32.3},	//3
		{54.93,50.51,47.3,44.35,41.69,39.1,36.83,34.58,34.08,34.29,33.2,32.3,50.33}		//2
		};
	
	static private double getEquityTwoCardHand(Card card1, Card card2) {
		int val1 = 14 - card1.val;
		int val2 = 14 - card2.val;
		return card1.suit == card2.suit ? suited[val1][val2]/100.0 : unsuited[val1][val2]/100.0;
	}
	
	static public double getEquityThreeCardHand(Card[] hand) {
//		if(hand[0].suit == hand[1].suit && hand[1].suit == hand[2].suit) {
//			String a = "", b = "", c = "";
//			
//			if(hand[0].val == 10) 
//				a = "T";
//			else if(hand[0].val == 11)
//				a = "J";
//			else if(hand[0].val == 12) 
//				a = "Q";
//			else if(hand[0].val == 13) 
//				a = "K";
//			else if(hand[0].val == 14) 
//				a = "A";
//			else
//				a = Integer.toString(hand[0].val);
//			
//			if(hand[1].val == 10) 
//				b = "T";
//			else if(hand[1].val == 11)
//				b = "J";
//			else if(hand[1].val == 12) 
//				b = "Q";
//			else if(hand[1].val == 13) 
//				b = "K";
//			else if(hand[1].val == 14) 
//				b = "A";
//			else
//				b = Integer.toString(hand[1].val);
//			
//			if(hand[2].val == 10) 
//				c = "T";
//			else if(hand[2].val == 11)
//				c = "J";
//			else if(hand[2].val == 12) 
//				c = "Q";
//			else if(hand[2].val == 13) 
//				c = "K";
//			else if(hand[2].val == 14) 
//				c = "A";
//			else
//				c = Integer.toString(hand[2].val);
//
//			return threeSuited.get(a + b + c);
//		}
		
		double[] handVal = new double[3];
		handVal[0] = getEquityTwoCardHand(hand[0], hand[1]);
		handVal[1] = getEquityTwoCardHand(hand[0], hand[2]);
		handVal[2] = getEquityTwoCardHand(hand[1], hand[2]);
		
		Arrays.sort(handVal);
		
		int r1 = 2;
		int r2 = 3;
		int r3 = 6;
		
		return (r1 * handVal[0] + r2 * handVal[1] + r3 * handVal[2]) / (r1 + r2 + r3);
	}
}