package common.gameserver;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import common.user.User;
import server.ServerException;

public class Game implements Serializable {
	private ArrayList<User> players;
	private PokerCard[] pokerCards;
	private UUID gameID;

	public ArrayList<User> getPlayers() {
		return players;
	}

	public PokerCard[] getPokerCards() {
		return pokerCards;
	}
	
	public UUID getGameID() {
		return gameID;
	}

	public Game(ArrayList<User> players) {
		this.players = players;
		this.pokerCards = getCards();
		this.gameID = UUID.randomUUID();
	}

	private PokerCard[] getCards() {
		ArrayList<String> allCards = PokerCard.getAllCards();
		Collections.shuffle(allCards);
		return new PokerCard[] { new PokerCard(allCards.get(0)), new PokerCard(allCards.get(1)),
				new PokerCard(allCards.get(2)), new PokerCard(allCards.get(3)) };
	}

	public void updatePlayerStatistics(Answer answer, Connection conn) throws ServerException {
		String winner = answer.getUsername();
		for (User player : players) {
			player.setGames(player.getGames() + 1);
			if (player.getUsername().equals(winner)) {
				player.setTimeToWin(
						(player.getTimeToWin() * player.getWins() + answer.getTimeToWin()) / (player.getWins() + 1));
				player.setWins(player.getWins() + 1);

			}
			User.updateUser(player, conn);
		}
	}

	public boolean containsPlayer(User user) {
		for (User player : players) {
			if (player.getUsername().equals(user.getUsername())) {
				return true;
			}
		}
		return false;
	}

	// have GamePlay thread that takes in inputs from users
	// and communicates with users
}
