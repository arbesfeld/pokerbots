package pokerbots.player;

public class Brain {
	//instantiated every hand
	public int handId, myBank, oppBank;
	public boolean button;
	public Card[] hand;
	
	//instantiated every action
	public int potSize, numBoardCards, numLastActions, numLegalActions; //brain
	public Card[] board; //brain
	public Action[] lastActions, legalActions; //brain
	public double timebank;
	
	private Historian maj;
	private Dory dory;
	
	private EquityCalculator ec;
	
	Brain(Historian maj) {
		this.maj = maj;
		dory = new Dory();
		ec = new EquityCalculator(hand, null);
	}
	
	Action act() {
		ec.setBoard(board);
		maj.update(this);
		dory.update(this);
		
		if(board[2] == null) 
			return actPreFlop();
		
		else if(board[3] == null) 
			return actPostFlop();
		
		else if(board[4] == null) 
			return actPostTurn();
		
		else
			return actPostRiver();
	}
	
	Action actPreFlop() {
		
	}
	
	Action actPostFlop() {
		double equity = ec.calculateTotalEquity();
	}
	
	Action actPostTurn() {
		double equity = ec.calculateTotalEquity();
	}
	
	Action actPostRiver() {
		double equity = ec.calculateTotalEquity();
	}
}
