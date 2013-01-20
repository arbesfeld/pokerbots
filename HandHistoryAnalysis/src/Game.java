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
	
	private GameState gState;
	private String winner;
	
	private final String FLOP = "*** FLOP ***";
	private final String TURN = "*** TURN ***";
	private final String RIVER = "*** RIVER ***";
	private final String WIN = "wins the pot";
	
	public Game(ArrayList<String> gHist) {
		gameHist = gHist;
		gState = GameState.PREFLOP;
		winner = "";
		processHistory();
	}
	
	private void processHistory() {
		for (String line : gameHist) {
			processLine(line);
		}
	}
	
	private void processLine(String line) {
		if (line.length() == 0) return;
		if (line.charAt(0) == '*') {
			changeGameState(line);
		} else if (line.contains(WIN)) {
			processWinner(line);
		}
		
	}
	
	private void changeGameState(String line) {
		if (line.contains(FLOP)) {
			gState = GameState.PRETURN;
		} else if (line.contains(TURN)) {
			gState = GameState.PRERIVER;
		} else if (line.contains(RIVER)) {
			gState = GameState.POSTRIVER;
		}
	}
	
	private void processWinner(String line) {
		winner = line.split(" ")[0];
	}
	
	public String getWinner() {
		return winner;
	}
	
	//returns what round of betting the game ends on
	public GameState getFinalRound() {
		return gState;
	}
	
	

}
