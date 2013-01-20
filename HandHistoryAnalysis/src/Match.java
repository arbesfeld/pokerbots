import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Represents one match against am opponent. Many games/hands in one match
 * @author akhil
 *
 */
public class Match {
	private String opponent;
	private String me;
	private String fileName;
	private int gameCount;
	
	private int[] roundCount; //counts how many hands end in each of the rounds of betting (pre-flop, etc.)
	private int[] winCount; //count how many times we win in each round
	
	public Match(String m, String op, String f) {
		opponent = op;
		me = m;
		fileName = f;
		gameCount = 0;
		roundCount = new int[4];
		winCount = new int[4];
	}
	
	public void processMatchHistory() {
		//System.out.println("Hi");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			//System.out.println(line);
			//System.out.println("Hi" + (line == null));
			ArrayList<String> game = new ArrayList<String>();
			while (line != null) {
				if (!line.startsWith("Hand #")) {
					game.add(line);
				} else {
					//System.out.println("Got here " + opponent);
					Game g = new Game(game);
					String winner = g.getWinner();
					boolean won = winner.equals(me);
					int i = 0;
					switch(g.getFinalRound()) {
					case PREFLOP:
						i = 0; break;
					case PRETURN:
						i = 1; break;
					case PRERIVER:
						i = 2; break;
					case POSTRIVER:
						i = 3; break;
					}
					roundCount[i]++;
					if (won) {
						winCount[i]++;
					}
					game = new ArrayList<String>();
					gameCount++;
				}
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double[] winRatesByRound() {
		return new double[] {
				((double)winCount[0]) / (roundCount[0] == 0 ? 1 : roundCount[0]),
				((double)winCount[1]) / (roundCount[1] == 0 ? 1 : roundCount[1]),
				((double)winCount[2]) / (roundCount[2] == 0 ? 1 : roundCount[2]),
				((double)winCount[3]) / (roundCount[3] == 0 ? 1 : roundCount[3])
		};
	}
	
	//how often we get to each round of betting
	public double[] roundRates() { 
		return new double[] {
				((double)roundCount[0]) / gameCount,
				((double)roundCount[1]) / gameCount,
				((double)roundCount[2]) / gameCount,
				((double)roundCount[3]) / gameCount
		};
	}
	
	public int getGameCount() {
		return gameCount;
	}
	
	public String getOpponent() {
		return opponent;
	}
	
}
