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
		if (roundWinMap.containsKey(m.getOpponent())) {
			double[] prev = roundWinMap.get(m.getOpponent());
			double[] next = m.winRatesByRound();
			for (int i = 0; i < 4; i++) {
				prev[i] = (prev[i] + next[i]) / 2;
			}
		} else {
			roundWinMap.put(m.getOpponent(), m.winRatesByRound());
		}
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
	}

}
