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
	
	private int[] preFlopEquityHist; //how many hands had this equity that end pre-flop
	private int[] preFlopEquityWinsHist; //histogram showing how many games we win pre-flop for different equity ranges
	private int[] preFlopEquityWinAmts; //how much we win (net) pre-flop with different equity
	private int[] postFlopEquityHist; //histogram for how many hands have this equity that end post-flop
	private int[] postFlopEquityWinsHist; //how many wins post-flop for different equities
	private int[] postFlopEquityWinAmts; //net win amount for diff equities for games ending post-flop
	private int[] totalWinsPreEquity; //histogram by pre-flop equity for wins
	private int[] totalHandsPreEquity; //how many hands of different pre-flop equity were played
	private int[] totalAmtPreEquity; //net amt won by pre-flop equity
	
	private int pfrCount;
	
	public Match(String m, String op, String f) {
		opponent = op;
		me = m;
		fileName = f;
		gameCount = 0;
		roundCount = new int[4];
		winCount = new int[4];
		preFlopEquityHist = new int[10];
		preFlopEquityWinAmts = new int[10];
		preFlopEquityWinsHist = new int[10];
		postFlopEquityHist = new int[10];
		postFlopEquityWinsHist = new int[10];
		postFlopEquityWinAmts = new int[10];
		totalWinsPreEquity = new int[10];
		totalHandsPreEquity = new int[10];
		totalAmtPreEquity = new int[10];
		pfrCount = 0;
	}
	
	public void processMatchHistory() {
		//System.out.println("Hi");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			//System.out.println(line);
			//System.out.println("Hi" + (line == null));
			ArrayList<String> game = new ArrayList<String>();
			while (line != null) {
				//System.out.println(line);
				if (line.startsWith("6.S912")) {
					line = br.readLine();
					continue;
				}
				if (!line.startsWith("Hand #")) {
					game.add(line);
				} else {
					//System.out.println("Got here " + opponent);
					Game g = new Game(game, opponent);
					processGame(g);
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
	
	private void processGame(Game g) {
		String winner = g.getWinner();
		boolean won = winner.equals(me);
		int i = 0;
		switch(g.getFinalRound()) {
		case PREFLOP:
			i = 0; 
			processPreFlopGame(g, won);
			break;
		case PRETURN:
			i = 1; 
			processPostFlopGame(g, won);
			break;
		case PRERIVER:
			i = 2; 
			processPostFlopGame(g, won);
			break;
		case POSTRIVER:
			i = 3; 
			processPostFlopGame(g, won);
			break;
		}
		double equity = g.getPreFlopEquity();
		int index = (int)(equity * 10);
		totalHandsPreEquity[index]++;
		roundCount[i]++;
		if (won) {
			totalWinsPreEquity[index]++;
			totalAmtPreEquity[index]+=g.getWinAmount();
			winCount[i]++;
		} else {
			totalAmtPreEquity[index] -= g.getWinAmount();
		}
		if (g.pfr()) {
			pfrCount++;
		}
	}
	
	private void processPreFlopGame(Game g, boolean won) {
		double equity = g.getPreFlopEquity();
		int index = (int)(equity * 10);
		preFlopEquityHist[index]++;
		if (won) {
			preFlopEquityWinsHist[index]++;
			preFlopEquityWinAmts[index] += g.getWinAmount();
		} else {
			preFlopEquityWinAmts[index] -= g.getWinAmount();
		}
	}
	
	private void processPostFlopGame(Game g, boolean won) {
		double equity = g.getPostFlopEquity();
		int index = (int)(equity * 10);
		postFlopEquityHist[index]++;
		if (won) {
			postFlopEquityWinsHist[index]++;
			postFlopEquityWinAmts[index] += g.getWinAmount();
		} else {
			postFlopEquityWinAmts[index] -= g.getWinAmount();
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
	
	public double[] getWinPercentageByPreEquityHistogram() {
		double[] hist = new double[10];
		for (int i = 0; i < totalWinsPreEquity.length; i++) {
			hist[i] = ((double)totalWinsPreEquity[i]) / (totalHandsPreEquity[i] == 0 ? 1 : totalHandsPreEquity[i]);
		}
		return hist;
	}
	
	public int[] getPreEquityWinCountPreFlop() {
		return preFlopEquityWinsHist;
	}
	
	public int[] getPreEquityHandCountPreFlop() {
		return preFlopEquityHist;
	}
	
	public int[] getWinCountByPreEquityHistogram() {
		return totalWinsPreEquity;
	}
	
	public int[] getNetAmtWonByPreEquityHistogram() {
		return totalAmtPreEquity;
	}
	
	public int[] getTotalCountByPreEquityHistogram() {
		return totalHandsPreEquity;
	}
	
	public double[] getPreFlopWinPercentageByEquity() {
		double[] hist = new double[10];
		for (int i = 0; i < preFlopEquityWinsHist.length; i++) {
			hist[i] = ((double)preFlopEquityWinsHist[i]) / (preFlopEquityHist[i] == 0 ? 1 : preFlopEquityHist[i]);
		}
		return hist;
	}
	
	public int[] getPreFlopNetWinAmtByEquity() {
		return preFlopEquityWinAmts;
	}
	
	public double[] getPostFlopWinPercentageByPostEquity() {
		double[] hist = new double[10];
		for (int i = 0; i < postFlopEquityWinsHist.length; i++) {
			hist[i] = ((double)postFlopEquityWinsHist[i]) / (postFlopEquityHist[i] == 0 ? 1 : postFlopEquityHist[i]);
		}
		return hist;
	}
	
	public int[] getPostFlopNetWinningsByPostEquity() {
		return postFlopEquityWinAmts;
	}
	
	public int[] getPostFlopPostEquityWinCount() {
		return postFlopEquityWinsHist;
	}
	
	public int[] getPostFlopPostEquityHandCount() {
		return postFlopEquityHist;
	}
	
	public double pfr() {
		return ((double)pfrCount) / gameCount;
	}
	
	public int getGameCount() {
		return gameCount;
	}
	
	public String getOpponent() {
		return opponent;
	}
	
}
