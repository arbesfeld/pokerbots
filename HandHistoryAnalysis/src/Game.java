import java.util.ArrayList;

/**
 * Represents one game in a match against an opponent. 
 * One hand per game.
 * @author akhil
 *
 */
public class Game {
	
	private ArrayList<String> gameHist;
	
	public enum GameState {
		PREFLOP,
		PRETURN,
		PRERIVER,
		POSTRIVER
	}
	
	private String opponent;
	private GameState gState;
	private String winner;
	private int winAmount;
	private Card[] hand;
	private EquityCalculator ec;
	private double preFlopEquity;
	private double postFlopEquity;
	private boolean pfr;
	
	private final String FLOP = "*** FLOP ***";
	private final String TURN = "*** TURN ***";
	private final String RIVER = "*** RIVER ***";
	private final String WIN = "wins the pot";
	private final String DEALT = "Dealt";
	
	public Game(ArrayList<String> gHist, String op) {
		opponent = op;
		gameHist = gHist;
		gState = GameState.PREFLOP;
		winner = "";
		winAmount = 0;
		preFlopEquity = -1;
		postFlopEquity = -1;
		pfr = false;
		processHistory();
	}
	
	private void processHistory() {
		for (String line : gameHist) {
			processLine(line);
		}
	}
	
	private void processLine(String line) {
		//System.out.println("here!!");
		if (line.length() == 0) return;
		//System.out.println("Line: " + line);
		if (line.charAt(0) == '*') {
			changeGameState(line);
		} else if (line.contains(WIN)) {
			//System.out.println("got here 2");
			processWinner(line);
		} else if (line.contains(DEALT)) {
			//System.out.println("got here");
			processHand(line);
		} else if (line.contains(opponent + " raises") &&
				gState == GameState.PREFLOP) {
			int r = 0;
			try {
				String[] tokens = line.split(" ");
				r = Integer.parseInt(tokens[tokens.length - 1]);
			} catch (Exception e) {
				pfr = true;
			}
			if (r >= 11) {
				pfr = true;
			}
		}
		
	}
	
	private void processHand(String line) {
		String[] tokens = line.split(" ");
		hand = new Card[] {
				CardUtils.getCardByString(tokens[3].substring(1)),
				CardUtils.getCardByString(tokens[4]),
				CardUtils.getCardByString(tokens[5].substring(0,2))
		};
		ec = new EquityCalculator(hand, null);
		preFlopEquity = ec.calculateTotalEquity();
	}
	
	public boolean pfr() {
		return pfr;
	}
	
	public double getPreFlopEquity() {
		return preFlopEquity;
	}
	
	public double getPostFlopEquity() {
		return postFlopEquity;
	}
	
	private void changeGameState(String line) {
		if (line.contains(FLOP)) {
			gState = GameState.PRETURN;
			String[] tokens = line.split(" ");
			int len = tokens.length;
			ec.setBoard(new Card[] {
					CardUtils.getCardByString(tokens[len - 3].substring(1)),
					CardUtils.getCardByString(tokens[len - 2]),
					CardUtils.getCardByString(tokens[len - 1].substring(0, 2)),
					null,
					null
			});
			postFlopEquity = ec.calculateTotalEquity();
		} else if (line.contains(TURN)) {
			gState = GameState.PRERIVER;
		} else if (line.contains(RIVER)) {
			gState = GameState.POSTRIVER;
		}
	}
	
	private void processWinner(String line) {
		String[] tokens = line.split(" ");
		winner = tokens[0];
		String amt = tokens[tokens.length - 1];
		try {
			winAmount = Integer.parseInt(amt.substring(1, amt.length() - 1));
		} catch (Exception e) {
			System.out.println("Error parsing win amount");
		}
		
	}
	
	public String getWinner() {
		return winner;
	}
	
	//returns what round of betting the game ends on
	public GameState getFinalRound() {
		return gState;
	}
	
	public int getWinAmount() {
		return winAmount;
	}
	
	

}
