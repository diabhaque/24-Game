package common.gameserver;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class PokerCard implements Serializable {
	private String icon_url;
	private int value;

	public ImageIcon getIcon() {
		return new ImageIcon(icon_url);
	}

	public int getValue() {
		return value;
	}

	public PokerCard(String cardId) {
		value = Integer.valueOf(cardId.substring(1));
		icon_url = "Images/card_" + cardId + ".gif";
	}

	public static ArrayList<String> getAllCards() {
		ArrayList<String> allCards = new ArrayList<String>();
		for (int i = 1; i < 5; i++) {
			for (int x = 1; x < 14; x++) {
				String id = "";
				id += Integer.toString(i);
				id += Integer.toString(x);
				allCards.add(id);
			}
		}
		return allCards;
	}

}
