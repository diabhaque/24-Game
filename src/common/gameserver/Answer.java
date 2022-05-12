package common.gameserver;

import java.io.Serializable;
import java.util.UUID;

public class Answer implements Serializable {

	private UUID gameID;
	private String expr;
	private Double value;
	private String username;
	private int timeToWin;

	public UUID getGameID() {
		return gameID;
	}

	public String getExpr() {
		return expr;
	}

	public Double getValue() {
		return value;
	}

	public String getUsername() {
		return username;
	}

	public int getTimeToWin() {
		return timeToWin;
	}

	public Answer(UUID gameID, String expr, Double value, String username, int timeToWin) {
		this.gameID = gameID;
		this.expr = expr;
		this.value = value;
		this.username = username;
		this.timeToWin = timeToWin;
	}
}
