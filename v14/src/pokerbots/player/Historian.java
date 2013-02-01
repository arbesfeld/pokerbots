package pokerbots.player;

public class Historian {
	//instantiated every match
	public String myName, oppName; //historian
	public int stackSize, bb; //historian
	
	private PerformedAction[] lastActions;
	//hand state variables
	public GameState currentState;
	public int myRaiseCount; // how many raises in this round of betting
	public int theirRaiseCount; // how many raises in this round of betting
	public int roundRaiseCount; // how many raises in this round of betting
	public boolean preFlopRaiser; // whether or not opponent the initial pre-flop raiser
	public boolean cBetting; //indicates whether we are cBetting
	public boolean oppCBetting; //whether opponent c-betted in this hand
	public boolean barrelling; //indicates whether we are second or third barrelling
	public boolean oppBarrelling; //whether opp is barrelling
	public double startingTime;
	
	// game stats
	public int numHandsPlayed; //hands played so far 
	private int winCount; //your win rate
	public int instantFold;
	
	// pre-flop stats
	private int pfrCount; //pre-flop raise
	private int callRaiseCount;
	private boolean hasCalledPreflop;
	
	//post-flop stats
	private double aggressionFactor; //(Raise% + Bet%) / Call% , calculated for post-flop only
	private int raiseCount, betCount, callCount, foldCount, actionCount; // post-flop rates
	private int showdownCount; //how often we go to showdown in total
	private int seenFlopCount; //number of times we've made it to the flop
	
	private int twoBCount; //how often opponent makes another continuation bet after the first one (second barrel)
	private int threeBCount; // re-raise pre-flop
	private int pfTwoBCount;
	private int pfThreeBCount;
	
	private int poss3Bet;
	private int poss2Bet;
	private int pfPoss3Bet;
	private int pfPoss2Bet;
	
	private int checkRaiseCount;
	private boolean oppCheckThisStreet;
	private int checkCount;
	private int sdwCount;
	
	// default stats (the "placeholder" values we use when calculating stuff")
	
	private double pfr = 0.5;
	
	private double pfthreeB = 0.25;
	private double pftwoB = 0.2;
	private double threeB = 0.25;
	private double twoB = 0.2;
	
	private double aggression = 1.5;
	private double aggrofreq = 0.5;
	
	private double sdw = 0.5;
	
	private double callraise = 0.0;
	private double checkraise = 0.0;
	
	private Dory dory;
	private Brain brain;
	enum GameState {
		PREFLOP,
		PRETURN,
		PRERIVER,
		POSTRIVER
	}
	
	public Historian(String myName, String oppName, int stackSize, int bb, double startingTime) {
		this.myName = myName;
		this.oppName = oppName;
		this.stackSize = stackSize;
		this.bb = bb;
		this.numHandsPlayed = 0;
		this.currentState = GameState.PREFLOP;
		this.startingTime = startingTime;
		this.roundRaiseCount = 0;
		this.myRaiseCount = 0;
		this.theirRaiseCount = 0; 
		
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
		callRaiseCount = 0;
		hasCalledPreflop = false;
		pfPoss3Bet = 0;
		pfPoss2Bet = 0;
		pfTwoBCount = 0;
		pfThreeBCount = 0; 
		
		//post-flop stats
		aggressionFactor = 0.0; //(Raise% + Bet%) / Call% , calculated for post-flop only
		seenFlopCount = 0; //number of times we've made it to the flop

		poss3Bet = 0;
		poss2Bet = 0;
		twoBCount = 0;
		threeBCount = 0;
		
		sdwCount = 0;
		showdownCount = 0; //how often we go to showdown in total
		
		foldCount = 0;
		checkCount = 0;
		raiseCount = 0;
		betCount = 0; 
		callCount = 0; 
		actionCount = 0; // post-flop rates
		oppCheckThisStreet = false;
	}
	
	public void setHand(Dory dory, Brain brain) {
		this.dory = dory;
		this.brain = brain;
	}
	
	public void newHand() {
		currentState = GameState.PREFLOP;
		roundRaiseCount = 0;
		preFlopRaiser = false;
		cBetting = false;
		barrelling = false;
		oppBarrelling = false;
		oppCBetting = false;
		hasCalledPreflop = false;

		oppCheckThisStreet = false;
		oppBetThisStreet = false;
		myRaiseCount = 0;
		theirRaiseCount = 0;
		
	}
	
	public double getPFR() {
		double pfrRate = ((double)pfrCount) / Math.max(1, (numHandsPlayed-instantFold));
		return (25*pfr + pfrRate*(numHandsPlayed-instantFold)) / ((numHandsPlayed-instantFold) + 25);
	}
	
	public double getCallRaise() {
		double callRaiseRate = ((double)callRaiseCount) / Math.max(1, (numHandsPlayed-instantFold));
		return (50*callraise + callRaiseRate*(numHandsPlayed-instantFold)) / ((numHandsPlayed-instantFold) + 50);
	}
	
	public double getCheckRaise() {
		double checkRaiseRate = ((double)checkRaiseCount) / Math.max(1, seenFlopCount);
		return (50*callraise + checkRaiseRate*seenFlopCount) / (seenFlopCount + 50);
	}
	
	public double get3BetRate() {
		double threeBRate = ((double)threeBCount) / Math.max(1, poss3Bet);
		return (50*threeB + threeBRate*poss3Bet) / (poss3Bet + 50);
	}
	public double get2BetRate() {
		double twoBRate = ((double)twoBCount) / Math.max(1, poss2Bet);
//		System.out.println("TWOBCOUNT: " + twoBCount + " poss2Bet: " + poss2Bet);
		return (50*twoB + twoBRate*poss2Bet) / (poss2Bet + 50);
	}
	public double getPF3BetRate() { //preflop
		double threeBRate = ((double)pfThreeBCount) / Math.max(1, pfPoss3Bet);
		return (50*pfthreeB + threeBRate*pfPoss3Bet) / (pfPoss3Bet + 50);
	}
	public double getPF2BetRate() { //preflop
		double twoBRate = ((double)pfTwoBCount) / Math.max(1, pfPoss2Bet);
		return (50*pftwoB + twoBRate*pfPoss2Bet) / (pfPoss2Bet + 50);
	}
	public double getAggression() {
		int actions = raiseCount + betCount + callCount + foldCount + checkCount;
		double rate = (double) (raiseCount + betCount) / Math.max(1, callCount);
		return (200 * aggression + rate * actions) / (200 + actions);
	}
	public double getAggressionFrequency() {
		int actions = raiseCount + betCount + callCount + foldCount + checkCount;
		System.out.println("R: " + raiseCount + " B: " + betCount + " C: " + callCount + " F: " + foldCount + " Check: " + checkCount);
		
		double rate = (double) (raiseCount + betCount) / Math.max(1, actions);
		return (100 * aggrofreq + rate * actions) / (100 + actions);
	}
	
	public double getSDWRate() {
//		double rate = (double) sdwCount / Math.max(1, showdownCount);
//		return (300 *sdw + rate*showdownCount) / (showdownCount + 300);
		return 0.5;
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
				if(pa.getAmount() == bb * 3 / 2 && pa.getActor().equalsIgnoreCase(oppName)) {
					instantFold++;
				}
			}
		}
	}
	
	void processPreflopAction(PerformedAction pa) {
		if (pa.getType().equalsIgnoreCase("RAISE")) {
			if(Math.abs((double)(pa.getAmount() - dory.theirBetsThisStreet) / (brain.potSize - pa.getAmount())) < 0.2)
				return;
			
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				if(theirRaiseCount == 0) {
					preFlopRaiser = true;
					pfrCount++;

					if(hasCalledPreflop) {
						callRaiseCount++;
					}
				}
				
				else if(theirRaiseCount > 0 && myRaiseCount > 0) {
					pfThreeBCount++;
				}
				else if(theirRaiseCount == 0 && myRaiseCount == 1) {
					pfTwoBCount++;
				}
				
				theirRaiseCount++;
			} 
			else {
				if(myRaiseCount == 0) {
					//System.out.println("POSS 2 BET");
					pfPoss2Bet++;
				}
				else if(myRaiseCount > 0 && theirRaiseCount > 0) {
					pfPoss3Bet++;
				}
				
				myRaiseCount++;
			}
		} 
		
		else if (pa.getType().equalsIgnoreCase("FOLD")) {
		
		} 
		
		else if (pa.getType().equalsIgnoreCase("CALL")) {
			if(pa.getActor().equalsIgnoreCase(oppName)) {
				hasCalledPreflop = true;
			}
		}
		
		else if (pa.getType().equalsIgnoreCase("CHECK")) {
			
		} 
		
		else if (pa.getType().equalsIgnoreCase("POST")) {
			if(pa.getActor().equalsIgnoreCase(myName))
				newHand();
		} 
		
		else if (pa.getType().equalsIgnoreCase("DEAL")) {
			if (pa.getStreet().equalsIgnoreCase("FLOP")) {
				currentState = GameState.PRETURN;
				myRaiseCount = 0;
				theirRaiseCount = 0;
				seenFlopCount++;
			}
		}
	}
	boolean oppBetThisStreet;
	void processPostFlopAction(PerformedAction pa) {
		if (pa.getType().equalsIgnoreCase("RAISE")) {
			if(Math.abs((double)(pa.getAmount() - dory.theirBetsThisStreet) / (brain.potSize - pa.getAmount())) < 0.2)
				return;
			
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				raiseCount++;
				
				if(oppCheckThisStreet && myRaiseCount == 1) {
					checkRaiseCount++;
					twoBCount++;
				}
				else if(myRaiseCount == 1 && theirRaiseCount == 0) {
					twoBCount++;
				}
				else if(myRaiseCount > 0 && theirRaiseCount > 0) {
					threeBCount++;
				}
				theirRaiseCount++;
			}
			else {
				if(theirRaiseCount > 0) {
					poss3Bet++;
				}
				else if(theirRaiseCount == 0) {
					//System.out.println("POSS 2 BET");
					poss2Bet++;
				}
				myRaiseCount++;
			}
		} 
		else if (pa.getType().equalsIgnoreCase("FOLD")) {
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				foldCount++;
			}
		} 
		else if (pa.getType().equalsIgnoreCase("BET")) {
			if(Math.abs((double) pa.getAmount() / (brain.potSize - pa.getAmount())) < 0.2)
				return;
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				betCount++;
				theirRaiseCount++;
			}
			else {
				poss2Bet++;
				myRaiseCount++;
			}
		}
		else if (pa.getType().equalsIgnoreCase("CALL")) {
			if (pa.getActor().equalsIgnoreCase(oppName)) {
				callCount++;
			}
		} 
		else if (pa.getType().equalsIgnoreCase("CHECK")) {
			if(pa.getActor().equalsIgnoreCase(oppName)) {
				oppCheckThisStreet = true;
				checkCount++;
			}
		} 
		else if (pa.getType().equalsIgnoreCase("DEAL")) {
			oppCheckThisStreet = false;
			oppBetThisStreet = false;
			
			myRaiseCount = 0;
			theirRaiseCount = 0;
			if (pa.getStreet().equalsIgnoreCase("TURN")) {
				currentState = GameState.PRERIVER;
			} else if (pa.getStreet().equalsIgnoreCase("RIVER")) {
				currentState = GameState.POSTRIVER;
			}
		} 
		else if (pa.getType().equalsIgnoreCase("SHOW")) {
			if(lastActions[lastActions.length - 1].getActor().equalsIgnoreCase(myName))
				sdwCount++;
			showdownCount++;
		}
	}

	public String getValueToSave(String key) {
		if(key.equalsIgnoreCase("PFR")) {
			return getPFR() + "";
		}
		else if(key.equalsIgnoreCase("SDW")) {
			return getSDWRate() + "";
		}
		else if(key.equalsIgnoreCase("AGGRO")) {
			return getAggression() + "";
		}
		else if(key.equalsIgnoreCase("AGGROFREQ")) {
			return getAggressionFrequency() + "";
		}
		else if(key.equalsIgnoreCase("CALLRAISE")) {
			return getCallRaise() + "";
		}
		else if(key.equalsIgnoreCase("CHECKRAISE")) {
	 		return getCheckRaise() + "";
		}
		else if(key.equalsIgnoreCase("3B")) {
			return get3BetRate() + "";
		}
		else if(key.equalsIgnoreCase("2B")) {
			return get2BetRate() + "";
		}
		else if(key.equalsIgnoreCase("PF3B")) {
			System.out.println("PUTTING PF3B: " + getPF3BetRate());
			return getPF3BetRate() + "";
		}
		else if(key.equalsIgnoreCase("PF2B")) {
			return getPF2BetRate() + "";
		}
		else
			return "";
	}

	public void notifyValue(String name, String key, String val) { //key value pair. the key is the opponent name right now
		if(name.equalsIgnoreCase(oppName)) {
			if(key.equalsIgnoreCase("PFR")) {
				pfr = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("SDW")) {
				sdw = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("AGGRO")) {
				aggression = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("AGGROFREQ")) {
				aggrofreq = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("CALLRAISE")) {
				callraise = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("CHECKRAISE")) {
				checkraise = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("3B")) {
				threeB = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("2B")) {
				twoB = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("PF3B")) {
				pfthreeB = Double.parseDouble(val);
			}
			else if(key.equalsIgnoreCase("PF2B")) {
				pftwoB = Double.parseDouble(val);
			}
		}
	}
}
