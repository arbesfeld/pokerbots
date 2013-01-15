package pokerbots.player;

public class Action {
	String type;
	
	//Our actions:
	int minBet, maxBet;     //BET
	int minRaise, maxRaise; //RAISE
							//CALL
							//FOLD
							//DISCARD
	
	//Performed actions:
	int betAmount; String actor; //BET
								 //CALL
								 //CHECK
	String street; 				 //DEAL
	String card; 				 //DISCARD
								 //FOLD
	
}
