import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads in hand history text files and spits out statistics on them.
 * Gives statistics on a day of play (all matches played that day).
 * Current statistics it gives:
 * 
 * @author akhil
 *
 */
public class HandHistoryAnalyzer {

	private static HashMap<String, double[]> roundWinMap = new HashMap<String, double[]>();
	private static HashMap<String, double[]> preEquityWinMap = new HashMap<String, double[]>(); //maps opponent to a histogram of 
																							// pre-flop equity to pre-flop win percentage
	private static HashMap<String, double[]> postEquityWinMap = new HashMap<String, double[]>(); //maps opponent to a histogram of 
																								// post-flop equity to post-flop win percentage
	private static HashMap<String, double[]> equityWinMap = new HashMap<String, double[]>(); //maps opponent to a histogram of 
																								// pre-flop equity to win percentage
	private static HashMap<String, int[]> preFlopNetAmt = new HashMap<String, int[]>(); //maps opponent to a histogram of 
																							// pre-flop equity to pre-flop net winnings
	private static HashMap<String, int[]> postFlopNetAmt = new HashMap<String, int[]>(); //maps opponent to a histogram of 
																								// post-flop equity to post-flop net winnings
	private static HashMap<String, int[]> totalNetAmtMap = new HashMap<String, int[]>(); //maps opponent to a histogram of 
																								// pre-flop equity to total net winnings
	private static HashMap<String, Match[]> opponentMap = new HashMap<String, Match[]>();
	private static int[] preFlopPreEquityWins = new int[10];
	private static int[] preFlopPreEquityHands = new int[10];
	private static int[] postFlopPostEquityWins = new int[10];
	private static int[] postFlopPostEquityHands = new int[10];
	private static int[] preEquityWins = new int[10];
	private static int[] preEquityHands = new int[10];
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("need to supply arguments of where the txt files are located");
			return;
		}
		File historyDir = new File(args[0]);
		String[] txtFiles;
		if (historyDir.isDirectory()) {
			txtFiles = historyDir.list();
			Pattern p2 = Pattern.compile("([a-zA-Z0-9]+)_vs_bAllin");
			Pattern p1 = Pattern.compile("bAllin_vs_([a-zA-Z0-9]+)");
			Matcher m1, m2;
			boolean done = false;
			for (String txt:txtFiles) {
				//if (done) {return;}
				String opponent;
				m1 = p1.matcher(txt);
				if (m1.find()) {
					opponent = m1.group(1);
				} else {
					m2 = p2.matcher(txt);
					if (m2.find()) {
						opponent = m2.group(1);
					} else {
						System.out.println("Didn't find opponent name: " + txt);
						continue;
					}
				}				
				Match m = new Match("bAllin", opponent, args[0] + 
						System.getProperty("file.separator") + txt);
				compileMatchStats(m);
				//done = true;
			}
			printStats();
		} else {
			System.out.println("Need to supply a valid directory of the files.");
		}

	}
	
	private static void compileMatchStats(Match m) {
		m.processMatchHistory();
		
		if (opponentMap.containsKey(m.getOpponent())) {
			opponentMap.get(m.getOpponent())[1] = m;
		} else {
			Match[] mArr = new Match[2];
			mArr[0] = m;
			opponentMap.put(m.getOpponent(), mArr);
		}
		
		if (roundWinMap.containsKey(m.getOpponent())) {
			double[] prev = roundWinMap.get(m.getOpponent());
			double[] next = m.winRatesByRound();
			for (int i = 0; i < 4; i++) {
				prev[i] = (prev[i] + next[i]) / 2;
			}
		} else {
			roundWinMap.put(m.getOpponent(), m.winRatesByRound());
		}
		
		/*
		if (preEquityWinMap.containsKey(m.getOpponent())) {
			double[] prev = preEquityWinMap.get(m.getOpponent());
			double[] next = m.getPreFlopWinPercentageByEquity();
			for (int i = 0; i < 10; i++) {
				prev[i] = (prev[i] + next[i]) / 2;
			}
		} else {
			preEquityWinMap.put(m.getOpponent(), m.getPreFlopWinPercentageByEquity());
		}
		
		if (postEquityWinMap.containsKey(m.getOpponent())) {
			double[] prev = postEquityWinMap.get(m.getOpponent());
			double[] next = m.getPostFlopWinPercentageByPostEquity();
			for (int i = 0; i < 10; i++) {
				prev[i] = (prev[i] + next[i]) / 2;
			}
		} else {
			postEquityWinMap.put(m.getOpponent(), m.getPostFlopWinPercentageByPostEquity());
		}
		
		if (equityWinMap.containsKey(m.getOpponent())) {
			double[] prev = equityWinMap.get(m.getOpponent());
			double[] next = m.getWinPercentageByPreEquityHistogram();
			for (int i = 0; i < 10; i++) {
				prev[i] = (prev[i] + next[i]) / 2;
			}
		} else {
			equityWinMap.put(m.getOpponent(), m.getWinPercentageByPreEquityHistogram());
		}
		
		if (preFlopNetAmt.containsKey(m.getOpponent())) {
			int[] prev = preFlopNetAmt.get(m.getOpponent());
			int[] next = m.getPreFlopNetWinAmtByEquity();
			for (int i = 0; i < 10; i++) {
				prev[i] += next[i];
			}
		} else {
			preFlopNetAmt.put(m.getOpponent(), m.getPreFlopNetWinAmtByEquity());
		}
		
		if (postFlopNetAmt.containsKey(m.getOpponent())) {
			int[] prev = postFlopNetAmt.get(m.getOpponent());
			int[] next = m.getPostFlopNetWinningsByPostEquity();
			for (int i = 0; i < 10; i++) {
				prev[i] += next[i];
			}
		} else {
			postFlopNetAmt.put(m.getOpponent(), m.getPostFlopNetWinningsByPostEquity());
		}
		
		if (totalNetAmtMap.containsKey(m.getOpponent())) {
			int[] prev = totalNetAmtMap.get(m.getOpponent());
			int[] next = m.getNetAmtWonByPreEquityHistogram();
			for (int i = 0; i < 10; i++) {
				prev[i] += next[i];
			}
		} else {
			totalNetAmtMap.put(m.getOpponent(), m.getNetAmtWonByPreEquityHistogram());
		}
		*/
		
		
	}
	
	private static void printStats() {
		System.out.println("Win percentages by round of betting");
		double[] total = new double[4];
		int count = 0;
		for (String opponent : roundWinMap.keySet()) {
			double[] arr = roundWinMap.get(opponent);
			System.out.println(opponent + ":\tPre-flop: " + arr[0] +
					"\tPre-turn: " + arr[1] +
					"\tPre-river: " + arr[2] +
					"\tPost-river: " + arr[3]);
			for (int i = 0; i < 4; i++) {
				total[i] += arr[i];
			}
			count++;
		}
		System.out.println("\nTotal win percentage by round:");
		System.out.println("Pre-flop: " + (total[0] / count) +
					"\tPre-turn: " + (total[1] / count) +
					"\tPre-river: " + (total[2] / count) +
					"\tPost-river: " + (total[3] / count));
		
		printPreFlopEquityMaps();
		printPostFlopEquityMaps();
		printEquityMaps(); //pre-flop equity, but count all hands, not just those ending in pre-flop
		
		//following are histograms based on equity
		printNetPreFlopWinnings();
		printNetPostFlopWinnings();
		printTotalNetWinnings();
		
		//print pfr
		printPFR();
	}
	
	private static void printPFR() {
		System.out.println("\nPrinting out pfr of each opponent");
		for (String opponent : opponentMap.keySet()) {
			Match[] mArr = opponentMap.get(opponent);
			Match m1 = mArr[0];
			Match m2 = mArr[1];
			System.out.println(opponent + ":\t" + ((m1.pfr() + m2.pfr()) / 2));
		}
	}

	private static void printTotalNetWinnings() {
		System.out.println("\nPrinting out the net winnings for hands of different pre-flop equity ranges.");
		System.out.println("We are looking at all hands.");
		
		int[] total = new int[10];
		int count = 0;
		for (String opponent : opponentMap.keySet()) {
			Match[] mArr = opponentMap.get(opponent);
			Match m1 = mArr[0];
			Match m2 = mArr[1];
			System.out.print(opponent + ":\t");
			for (int i = 0; i < 10; i++) {
				System.out.print((m1.getNetAmtWonByPreEquityHistogram()[i] + m2.getNetAmtWonByPreEquityHistogram()[i]) + "\t");
				total[i] += m1.getNetAmtWonByPreEquityHistogram()[i] + m2.getNetAmtWonByPreEquityHistogram()[i];
			}
			System.out.print("\n");
			
		}
		System.out.print("\nTotal:\t");
		for (int i = 0; i < 10; i++) {
			System.out.print(total[i] + "\t");
		}
		System.out.println("\n");
		
	}

	private static void printNetPostFlopWinnings() {
		System.out.println("\nPrinting out the net winnings for hands of different post-flop equity ranges.");
		System.out.println("We are only looking at hands ending post-flop.");
		
		int[] total = new int[10];
		int count = 0;
		for (String opponent : opponentMap.keySet()) {
			Match[] mArr = opponentMap.get(opponent);
			Match m1 = mArr[0];
			Match m2 = mArr[1];
			System.out.print(opponent + ":\t");
			for (int i = 0; i < 10; i++) {
				System.out.print((m1.getPostFlopNetWinningsByPostEquity()[i] + m2.getPostFlopNetWinningsByPostEquity()[i]) + "\t");
				total[i] += m1.getPostFlopNetWinningsByPostEquity()[i] + m2.getPostFlopNetWinningsByPostEquity()[i];
			}
			System.out.print("\n");
			
		}
		System.out.print("\nTotal:\t");
		for (int i = 0; i < 10; i++) {
			System.out.print(total[i] + "\t");
		}
		System.out.println("\n");
		
	}

	private static void printNetPreFlopWinnings() {
		System.out.println("\nPrinting out the net winnings for hands of different pre-flop equity ranges.");
		System.out.println("We are only looking at hands ending pre-flop.");
		
		int[] total = new int[10];
		int count = 0;
		for (String opponent : opponentMap.keySet()) {
			Match[] mArr = opponentMap.get(opponent);
			Match m1 = mArr[0];
			Match m2 = mArr[1];
			System.out.print(opponent + ":\t");
			for (int i = 0; i < 10; i++) {
				System.out.print((m1.getPreFlopNetWinAmtByEquity()[i] + m2.getPreFlopNetWinAmtByEquity()[i]) + "\t");
				total[i] += m1.getPreFlopNetWinAmtByEquity()[i] + m2.getPreFlopNetWinAmtByEquity()[i];
			}
			System.out.print("\n");
			
		}
		System.out.print("\nTotal:\t");
		for (int i = 0; i < 10; i++) {
			System.out.print(total[i] + "\t");
		}
		System.out.println("\n");
		
	}

	private static void printEquityMaps() {
		System.out.println("\nPrinting out the winning percentage for hands of different pre-flop equity ranges.");
		System.out.println("We are looking at all hands.");
		
		double[] totalWins = new double[10];
		double[] totalHands = new double[10];
		int count = 0;
		for (String opponent : opponentMap.keySet()) {
			Match[] mArr = opponentMap.get(opponent);
			Match m1 = mArr[0];
			Match m2 = mArr[1];
			int[] wins = new int[10];
			int[] hands = new int[10];
			System.out.print(opponent + ":\t");
			for (int i = 0; i < 10; i++) {
				wins[i] += m1.getWinCountByPreEquityHistogram()[i];
				wins[i] += m2.getWinCountByPreEquityHistogram()[i];
				hands[i] += m1.getTotalCountByPreEquityHistogram()[i];
				hands[i] += m2.getTotalCountByPreEquityHistogram()[i];
				totalWins[i] += wins[i];
				totalHands[i] += hands[i];
				System.out.print(((double)wins[i] / hands[i]) + "\t");
			}
			System.out.print("\n");
			
		}
		System.out.print("\nTotal:\t");
		for (int i = 0; i < 10; i++) {
			System.out.print(((double)totalWins[i] / totalHands[i]) + "\t");
		}
		System.out.println("\n");
		
	}

	private static void printPostFlopEquityMaps() {
		System.out.println("\nPrinting out the winning percentage for hands of different post-flop equity ranges.");
		System.out.println("We are only looking at hands that also end post-flop.");
		
		double[] totalWins = new double[10];
		double[] totalHands = new double[10];
		int count = 0;
		for (String opponent : opponentMap.keySet()) {
			Match[] mArr = opponentMap.get(opponent);
			Match m1 = mArr[0];
			Match m2 = mArr[1];
			int[] wins = new int[10];
			int[] hands = new int[10];
			System.out.print(opponent + ":\t");
			for (int i = 0; i < 10; i++) {
				wins[i] += m1.getPostFlopPostEquityWinCount()[i];
				wins[i] += m2.getPostFlopPostEquityWinCount()[i];
				hands[i] += m1.getPostFlopPostEquityHandCount()[i];
				hands[i] += m2.getPostFlopPostEquityHandCount()[i];
				totalWins[i] += wins[i];
				totalHands[i] += hands[i];
				System.out.print(((double)wins[i] / hands[i]) + "\t");
			}
			System.out.print("\n");
			
		}
		System.out.print("\nTotal:\t");
		for (int i = 0; i < 10; i++) {
			System.out.print(((double)totalWins[i] / totalHands[i]) + "\t");
		}
		System.out.println("\n");
		
	}

	private static void printPreFlopEquityMaps() {
		System.out.println("\nPrinting out the winning percentage for hands of different pre-flop equity ranges.");
		System.out.println("We are only looking at hands that also end pre-flop.");
		
		double[] totalWins = new double[10];
		double[] totalHands = new double[10];
		int count = 0;
		for (String opponent : opponentMap.keySet()) {
			Match[] mArr = opponentMap.get(opponent);
			Match m1 = mArr[0];
			Match m2 = mArr[1];
			int[] wins = new int[10];
			int[] hands = new int[10];
			System.out.print(opponent + ":\t");
			for (int i = 0; i < 10; i++) {
				wins[i] += m1.getPreEquityWinCountPreFlop()[i];
				wins[i] += m2.getPreEquityWinCountPreFlop()[i];
				hands[i] += m1.getPreEquityHandCountPreFlop()[i];
				hands[i] += m2.getPreEquityHandCountPreFlop()[i];
				totalWins[i] += wins[i];
				totalHands[i] += hands[i];
				System.out.print(((double)wins[i] / hands[i]) + "\t");
			}
			System.out.print("\n");
			
		}
		System.out.print("\nTotal:\t");
		for (int i = 0; i < 10; i++) {
			System.out.print(((double)totalWins[i] / totalHands[i]) + "\t");
		}
		System.out.println("\n");
		
	}
	
	

}
