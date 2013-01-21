package pokerbots.player;

public class Historian {
	//instantiated every match
	public String myName, oppName; //historian
	public int stackSize, bb; //historian
	
	private PerformedAction[] lastActions;
	//hand state variables
	public GameState currentState;
	public int roundRaiseCount; // how many raises in this round of betting
	public boolean preFlopRaiser; // whether or not opponent the initial pre-flop raiser
	public boolean cBetting; //indicates whether we are cBetting
	public boolean oppCBetting; //whether opponent c-betted in this hand
	public boolean barrelling; //indicates whether we are second or third barrelling
	public boolean oppBarrelling; //whether opp is barrelling
	
	// game stats
	public int numHandsPlayed; //hands played so far 
	private int winCount; //your win rate
	public int instantFold;
	
	// pre-flop stats
	private int pfrCount; //pre-flop raise
	private int vpipCount; //call or raise pre-flop
	private int initFoldCount; //related to vpip, counts when they don't play their hand.
	private int threeBCount; // re-raise pre-flop
	private int myPFRCount; //how many times we pfr
	private int my3BetCount; //how many times we 3-bet
	private int pfrFoldCount; //fold to initial pre-flop raise
	private int f3Count; // fold to a re-raise pre-flop
	
	//post-flop stats
	private double aggressionFactor; //(Raise% + Bet%) / Call% , calculated for post-flop only
	private int raiseCount, betCount, callCount, actionCount; // post-flop rates
	private int wtsdCount; //how often opponent goes to showdown after seeing flop (can be used with aggression)
	private int showdownCount; //how often we go to showdown in total
	private int seenFlopCount; //number of times we've made it to the flop
	private int cbCount; //how often opponent continuation bets as the pre-flop raiser
	private int twoBCount; //how often opponent makes another continuation bet after the first one (second barrel)
	private int myCBet; //how many times we c-bet
	private int myBarrel; //how many times we double barrel
	private int fbCount; // fold to continuation bets
	private int f2Count; // fold to second barrels
	private int sdwCount;
	
	// default stats (the "placeholder" values we use when calculating stuff")
	private double vpip = 0.8;
	private double pfr = 0.5;
	private double threeB = 0.3;
	private double pfrFold = 0.3;
	private double threeBFold = 0.4;
	private double wtsd = 0.5;
	private double cbet = 0.2;
	private double db = 0.15;
	private double cbFold = 0.4;
	private double bFold = 0.4;
	private double aggression = 1.5;
	
	
	enum GameState {
		PREFLOP,
		PRETURN,
		PRERIVER,
		POSTRIVER
	}
	
	public Historian(String myName, String oppName, int stackSize, int bb) {
		this.myName = myName;
		this.oppName = oppName;
		this.stackSize = stackSize;
		this.bb = bb;
		this.numHandsPlayed = 0;
		this.currentState = GameState.PREFLOP;
		this.roundRaiseCount = 0;
		
		roundRaiseCount = 0; // how many raises in this round of betting
		preFlopRaiser = false; // whether or not opponent the initial pre-flop raiser
		cBetting = false; //indicates whether we are cBetting
		oppCBetting = false; //whether opponent c-betted in this hand
		barrelling = false; //indicates whether we are second or third barrelling
		oppBarrelling = false; //whether opp is barrelling
		
		// game stats
		numHandsPlayed = 0; //hands played so far 
		winCount = 0; //your win rate
		instantFold = 0;
		
		// pre-flop stats
	    pfrCount = 0; //pre-flop raise
		vpipCount = 0; //call or raise pre-flop
		initFoldCount = 0; //related to vpip, counts when they don't play their hand.
		threeBCount = 0; // re-raise pre-flop
		myPFRCount = 0; //how many times we pfr
		my3BetCount = 0; //how many times we 3-bet
		pfrFoldCount = 0; //fold to initial pre-flop raise
		f3Count = 0; // fold to a re-raise pre-flop
		
		//post-flop stats
		aggressionFactor = 0.0; //(Raise% + Bet%) / Call% , calculated for post-flop only
		raiseCount = 0;
		betCount = 0; 
		callCount = 0; 
		actionCount = 0; // post-flop rates
		wtsdCount = 0; //how often opponent goes to showdown after seeing flop (can be used with aggression)
		showdownCount = 0; //how often we go to showdown in total
		seenFlopCount = 0; //number of times we've made it to the flop
		cbCount = 0; //how often opponent continuation bets as the pre-flop raiser
		twoBCount = 0; //how often opponent makes another continuation bet after the first one (second barrel)
		myCBet = 0; //how many times we c-bet
		myBarrel = 0; //how many times we double barrel
		fbCount = 0; // fold to continuation bets
		f2Count = 0; // fold to second barrels
		sdwCount = 0;
	}
	
	
	public void newHand() {
		currentState = GameState.PREFLOP;
		roundRaiseCount = 0;
		preFlopRaiser = false;
		cBetting = false;
		barrelling = false;
		oppBarrelling = false;
		oppCBetting = false;
	}
	
	public double getVPIP() {
		double vpipRate = ((double)(numHandsPlayed - initFoldCount)) / numHandsPlayed;
		return (25*vpip + vpipRate*numHandsPlayed) / (numHandsPlayed + 25);
	}
	
	public double getPFR() {
		double pfrRate = ((double)pfrCount) / Math.max(1, (numHandsPlayed-instantFold));
		//System.out.println("(25*pfr + pfrRate*numHandsPlayed) / (numHandsPlayed + 25): "  + (25*pfr + pfrRate*numHandsPlayed) / (numHandsPlayed + 25));
		//return 0.5;
		return (25*pfr + pfrRate*(numHandsPlayed-instantFold)) / ((numHandsPlayed-instantFold) + 25);
	}
	
	public double get3BetRate() {
		double threeBRate = ((double)threeBCount) / numHandsPlayed;
		return (50*threeB + threeBRate*numHandsPlayed) / (numHandsPlayed + 50);
	}
	
	public double getPFRFold() {
		double pfrFoldR = ((double)pfrFoldCount) / myPFRCount;
		return (25*pfrFold + pfrFoldR*myPFRCount) / (myPFRCount + 25);
	}
	
	public double get3BetFold() {
		double rate = ((double)f3Count) / my3BetCount;
		return (20*threeBFold + rate*my3BetCount) / (my3BetCount + 20);
	}
	
	public double getAggression() {
		double raise = ((double)raiseCount) / actionCount;
		double bet = ((double)betCount) / actionCount;
		double call = ((double)callCount) / actionCount;
		return (50 * aggression + (raise + bet) / call) / (50 + (raise + bet) / call);
	}
	
	public double getWTSD() {
		double rate = ((double)showdownCount) / seenFlopCount;
		return (50*wtsd + rate*seenFlopCount) / (seenFlopCount + 50);
	}
	
	public double getCBet() {
		double rate = ((double)cbCount) / numHandsPlayed;
		return (50*cbet + rate*numHandsPlayed) / (numHandsPlayed + 50);
	}
	
	public double getDoubleBarrel() {
		double rate = ((double)twoBCount) / numHandsPlayed;
		return (50*db + rate*numHandsPlayed) / (numHandsPlayed + 50);
	}
	
	public double cBetFoldRate() {
		double rate = ((double)fbCount) / myCBet;
		return (25*cbFold + rate*myCBet) / (myCBet + 25);
	}
	
	public double barrelFoldRate() {
		double rate = ((double)f2Count) / myBarrel;
		return (25*bFold + rate*myBarrel) / (myBarrel + 25);
	}
	
	public double getSDWRate() {
		double rate = (double) sdwCount / showdownCount;
		return (50 * 0.5 + rate*showdownCount) / (showdownCount + 50);
	}
	void update(PerformedAction[] lastActions) {
		this.lastActions = lastActions;
		for (int i = 0; i < lastActions.length; i++) {
			PerformedAction pa = lastActions[i];
			if(pa.getType().equalsIgnoreCase("POST")) {
				currentState = GameState.PREFLOP;
			}
			if (currentState == GameState.PREFLOP) {
				processPreflopAction(pa);
			} else {
				processPostFlopAction(pa);
			}
			if(pa.getType().equalsIgnoreCase("WIN")) {
				if(pa.getAmount() == bb * 3 / 2 && pa.getActor().equalsIgnoreCase(oppName))
					instantFold++;
			}
		}
	}
	
	void processPreflopAction(PerformedAction pa) {
		if (pa.getType().equalsIgnoreCase("RAISE")) {
			if(pa.getAmount() < 11)
				return;
			if (roundRaiseCount == 0) {
				preFlopRaiser = pa.getActor().equalsIgnoreCase(oppName);
				if (preFlopRaiser) {
					pfrCount++;
					roundRaiseCount++;
				} else {
					myPFRCount++;
				}
			} else {
				if (pa.getActor().equalsIgnoreCase(oppName)) {
					threeBCount++;
					roundRaiseCount++;
				} else {
					my3BetCount++;
				}
			}
		} else if (pa.getType().equalsIgnoreCase("FOLD")) {
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				if (roundRaiseCount == 0) {
					initFoldCount++; //opp didn't play hand
				} else if (roundRaiseCount == 1) {
					pfrFoldCount++;
				} else {
					f3Count++;
				}
			}
		} else if (pa.getType().equalsIgnoreCase("CALL")) {
			
		} else if (pa.getType().equalsIgnoreCase("CHECK")) {
			
		} else if (pa.getType().equalsIgnoreCase("POST")) {
			if(pa.getActor().equalsIgnoreCase(myName))
				newHand();
			roundRaiseCount = 0;
		} else if (pa.getType().equalsIgnoreCase("DEAL")) {
			if (pa.getStreet().equalsIgnoreCase("FLOP")) {
				currentState = GameState.PRETURN;
				roundRaiseCount = 0;
				seenFlopCount++;
			}
		}
	}
	
	void processPostFlopAction(PerformedAction pa) {
		if (pa.getType().equalsIgnoreCase("RAISE")) {
			roundRaiseCount++;
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				raiseCount++;
				actionCount++;
				if (preFlopRaiser && currentState == GameState.PRETURN) {
					cbCount++;
					oppCBetting = true;
				} else if (currentState != GameState.PRETURN && oppCBetting) {
					twoBCount++;
				}
			} else {
				if (currentState == GameState.PRETURN) {
					cBetting = !preFlopRaiser; //we are c-betting if we were the pre-flop raiser
					if (cBetting) {
						myCBet++;
					}
				} else {
					barrelling = cBetting;
					if (barrelling) {
						myBarrel++;
					}
				}
			}
		} else if (pa.getType().equalsIgnoreCase("FOLD")) {
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				if (cBetting && currentState == GameState.PRETURN) {
					fbCount++;
				} else if (barrelling) {
					f2Count++;
				}
			}
		} else if (pa.getType().equalsIgnoreCase("BET")) {
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				betCount++;
				actionCount++;
				if (currentState == GameState.PRETURN && preFlopRaiser) {
					cbCount++;
					oppCBetting = true;
				} else if (currentState != GameState.PRETURN && oppCBetting) {
					oppBarrelling = true;
					twoBCount++;
				}
			} else {
				if (currentState == GameState.PRETURN) {
					cBetting = !preFlopRaiser; //we are c-betting if we were the pre-flop raiser
					if (cBetting) {
						myCBet++;
					}
				} else {
					if (cBetting) {
						barrelling = true;
						myBarrel++;
					}
				}
			} 
		} else if (pa.getType().equalsIgnoreCase("CALL")) {
			if (pa.getType().equalsIgnoreCase(oppName)) {
				actionCount++;
				callCount++;
			}
		} else if (pa.getType().equalsIgnoreCase("CHECK")) {
			
		} else if (pa.getType().equalsIgnoreCase("DEAL")) {
			if (pa.getStreet().equalsIgnoreCase("TURN")) {
				currentState = GameState.PRERIVER;
			} else if (pa.getStreet().equalsIgnoreCase("RIVER")) {
				currentState = GameState.POSTRIVER;
			}
			roundRaiseCount++;
		} else if (pa.getType().equalsIgnoreCase("SHOW")) {
			if(lastActions[lastActions.length - 1].getActor().equalsIgnoreCase(myName))
				sdwCount++;
			showdownCount++;
		}
	}

	public String getValueToSave() {
		return "" + pfr; //save the pfr rate
	}

	public void notifyValue(String string, String string2) { //key value pair. the key is the opponent name right now
		if(string.equalsIgnoreCase(oppName))
			pfr = Double.parseDouble(string2);
	}
}
