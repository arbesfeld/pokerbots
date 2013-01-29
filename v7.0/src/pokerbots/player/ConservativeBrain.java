package pokerbots.player;

public class ConservativeBrain extends Brain {
	
	ConservativeBrain(Historian maj, Card[] hand, double timebank, boolean callRaise, boolean checkRaise) {
		super(maj, hand, timebank, callRaise, checkRaise);
		
		callConstant = 1.5;
		
		//should range from 0.35 to 0.85, they do not have to be equally spaced
		eVals = new double[]{0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85}; 
		
		//should range from maj.stackSize / 64 to maj.stackSize, play around with these a lot
		sVals = new double[]{maj.stackSize / 80.0, maj.stackSize / 40.0, maj.stackSize / 32.0, maj.stackSize / 16.0, maj.stackSize / 12.0, maj.stackSize / 8.0, maj.stackSize / 6.0, maj.stackSize}; 
		
		//should range from 0.05 to 3.0, play around with these a lot
		pVals = new double[]{0.15, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.5, 1.7, 1.9, 2.1, 2.3, 2.5};
	}
	protected Action actPreFlopButton() { //small blind acts first
		if(callRaise) {
			callRaise = false;
			return call();
		}
		
		if(eL(1)) {
			return checkFold();
		}
		else if(eL(2)) {
			return checkFold();
		}
		else if(eL(3)) { 
			return checkFoldCallPotOdds();
		}
		else if(eL(4)) {
			return checkFoldCallPotOdds();
		}
		else if(eL(5)) {
			return putLin(dory.liqEquity(), eVals[4], eVals[5], sVals[2], sVals[3]);
		}
		else if(eL(6)) {
			return putLin(dory.liqEquity(), eVals[5], eVals[6], sVals[3], sVals[4]);
		}
		else {
			return putLin(dory.liqEquity(), eVals[6], eVals[7], sVals[4], sVals[5]);
		}
	}
	
	protected Action actPreFlopNotButton() { //big blind acts second
		if(eL(1)) {
			return checkFold();
		}
		else if(eL(2)) {
			return checkFold();
		}
		else if(eL(3)) { 
			return checkFoldCallPotOdds();
		}
		else if(eL(4)) {
			return checkFoldCallPotOdds();
		}
		else if(eL(5)) {
			return putLin(dory.liqEquity(), eVals[4], eVals[5], sVals[2], sVals[3]);
		}
		else if(eL(6)) {
			return putLin(dory.liqEquity(), eVals[5], eVals[6], sVals[3], sVals[4]);
		}
		else {
			return putLin(dory.liqEquity(), eVals[6], eVals[7], sVals[4], sVals[5]);
		}
	}
	
	////////////////////////////////////////////////////
	protected Action actPostFlop() {
		return button ? actPostFlopButton() : actPostFlopNotButton();
	}
	
	protected Action actPostFlopButton() {    //acts second
		if(checkRaise) {
			checkRaise = false;
			return check();
		}
		if(eL(3)) {
			if(dory.theirBetsThisStreet - dory.myBetsThisStreet < pV(0))
				return checkFoldCallPotOdds();
			return checkFold();
		}
		else if(eL(3)) {
			return checkFold();
			//return checkFoldCallPotOdds();
			//return putPotPercentage(dory.liqEquity(), eVals[2], eVals[3], pVals[1], pVals[2]);
		}
		else if(eL(4)) {
			return checkFoldCallPotOdds();
			//return putPotPercentage(dory.liqEquity(), eVals[3], eVals[4], pVals[2], pVals[3]);
		}
		else if(eL(5)) {
			return checkFoldCallPotOdds();
		}

		else if(eL(6)) {
			return putPotPercentage(dory.liqEquity(), eVals[4], eVals[5], pVals[3], pVals[4]);
		}
		else if(eL(7)) {
			return putPotPercentage(dory.liqEquity(), eVals[5], eVals[6], pVals[4], pVals[5]); 
		}
		else {
			return putPotPercentage(dory.liqEquity(), eVals[6], eVals[7], pVals[5], pVals[6]);
		}
	}
	
	protected Action actPostFlopNotButton() { //acts first
		if(checkRaise) {
			checkRaise = false;
			return check();
		}
		if(eL(3)) {
			if(dory.theirBetsThisStreet - dory.myBetsThisStreet < pV(0))
				return checkFoldCallPotOdds();
			return checkFold();
		}
		else if(eL(3)) {
			return checkFold();
			//return checkFoldCallPotOdds();
			//return putPotPercentage(dory.liqEquity(), eVals[2], eVals[3], pVals[1], pVals[2]);
		}
		else if(eL(4)) {
			return checkFoldCallPotOdds();
			//return putPotPercentage(dory.liqEquity(), eVals[3], eVals[4], pVals[2], pVals[3]);
		}
		else if(eL(5)) {
			return checkFoldCallPotOdds();
		}

		else if(eL(6)) {
			return putPotPercentage(dory.liqEquity(), eVals[4], eVals[5], pVals[3], pVals[4]);
		}
		else if(eL(7)) {
			return putPotPercentage(dory.liqEquity(), eVals[5], eVals[6], pVals[4], pVals[5]); 
		}
		else {
			return putPotPercentage(dory.liqEquity(), eVals[6], eVals[7], pVals[5], pVals[6]);
		}
	}
}
