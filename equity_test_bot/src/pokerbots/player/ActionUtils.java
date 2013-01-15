package pokerbots.player;

public class ActionUtils {

	public static LegalAction getLegalActionByString(String s) {
		String[] arr = s.split(":");
		if (arr[0].equalsIgnoreCase("BET") || arr[0].equalsIgnoreCase("RAISE")) {
			return new LegalAction(arr[0], Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
		} else {
			return new LegalAction(arr[0]);
		}
	}
	
	public static PerformedAction getPerformedActionByString(String s) {
		String[] arr = s.split(":");
		PerformedAction pa = new PerformedAction(arr[0]);
		if ("BET".equalsIgnoreCase(arr[0]) || "POST".equalsIgnoreCase(arr[0])
				|| "RAISE".equalsIgnoreCase(arr[0]) || "REFUND".equalsIgnoreCase(arr[0])
				|| "TIE".equalsIgnoreCase(arr[0]) || "WIN".equalsIgnoreCase(arr[0])) {
			pa.setAmount(Integer.parseInt(arr[1]));
			pa.setActor(arr[2]);
		} else if ("CALL".equalsIgnoreCase(arr[0]) || "CHECK".equalsIgnoreCase(arr[0])
				|| "FOLD".equalsIgnoreCase(arr[0])) {
			pa.setActor(arr[1]);
		} else if ("DEAL".equalsIgnoreCase(arr[0])) {
			pa.setStreet(arr[1]);
		} else if ("DISCARD".equalsIgnoreCase(arr[0])) {
			pa.setDiscard(CardUtils.getCardByString(arr[1]));
		} else if ("SHOW".equalsIgnoreCase(arr[0])) {
			pa.setShowCards(CardUtils.getCardByString(arr[1]), CardUtils.getCardByString(arr[2]));
			pa.setActor(arr[3]);
		}
		
		return pa;
	}
}
