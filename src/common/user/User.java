package common.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import common.management.JoinException;
import server.ServerException;

public class User implements Serializable {

	private String username;
	private int wins;
	private int games;
	private int timeToWin;
	private int rank;

	public User(String username) {
		this.username = username;
		this.wins = 0;
		this.games = 0;
		this.timeToWin = 0;
		this.rank = 0;
	}

	public User(String username, int wins, int games, int timeToWin, int rank) {
		this.username = username;
		this.wins = wins;
		this.games = games;
		this.timeToWin = timeToWin;
		this.rank = rank;
	}

	public String getUsername() {
		return this.username;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getGames() {
		return games;
	}

	public void setGames(int games) {
		this.games = games;
	}

	public int getTimeToWin() {
		return timeToWin;
	}

	public void setTimeToWin(int timeToWin) {
		this.timeToWin = timeToWin;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public static void registerUser(User user, String password, Connection conn) throws ServerException {
		try {
			PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO UserInfo (username, wins, games, timeToWin, rank, password) VALUES (?, ?, ?, ?, ?, ?)");
			stmt.setString(1, user.getUsername());
			stmt.setInt(2, user.getWins());
			stmt.setInt(3, user.getGames());
			stmt.setInt(4, user.getTimeToWin());
			stmt.setInt(5, user.getRank());
			stmt.setString(6, password);
			stmt.execute();
			System.out.println("Record created");
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Error inserting record: " + e);
			throw new ServerException("Server Exception");
		}

	}

	public static void addOnlineUser(String username, Connection conn) throws ServerException {
		try {
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO OnlineUsers (username) VALUES (?)");
			stmt.setString(1, username);
			stmt.execute();
			System.out.println("Online user added");
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Error adding online user: " + e);
			throw new ServerException("Server Exception");
		}
	}

	public static void removeOnlineUser(String username, Connection conn) throws ServerException {
		try {
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM OnlineUsers WHERE username = ?");
			stmt.setString(1, username);
			stmt.executeUpdate();
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Error deleting record: " + e);
			throw new ServerException("Server Exception");
		}
	}

	public static Boolean isUserOnline(String username, Connection conn) throws ServerException {
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT username FROM OnlineUsers WHERE username = ?");
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			System.err.println("Error reading record: " + e);
			throw new ServerException("Server Exception");
		}
	}

	public static User getUser(String username, String password, Connection conn) throws ServerException {
		try {
			PreparedStatement stmt = conn.prepareStatement(
					"SELECT username, wins, games, timeToWin, rank, password FROM UserInfo WHERE username = ?");
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (!rs.next()) {
				return null;
			}

			if (!password.isEmpty() && !password.equals(rs.getString(6))) {
				return null;
			}

			return new User(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5));
		} catch (SQLException e) {
			System.err.println("Error reading record: " + e);
			throw new ServerException("Server Exception");
		}
	}

	public static void clearOnlineUsers(Connection conn) throws ServerException {
		try {
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM OnlineUsers");
			stmt.execute();
			System.out.println("Clears online users");
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Error clearing online users: " + e);
			throw new ServerException("Server Exception");
		}
	}

	public static ArrayList<User> getAllUsers(Connection conn) throws ServerException {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT username, wins, games, timeToWin, rank FROM UserInfo");
			ArrayList<User> users = new ArrayList<User>();

			while (rs.next()) {
				users.add(new User(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5)));
			}
			return users;
		} catch (SQLException e) {
			System.err.println("Error listing records: " + e);
			throw new ServerException("Server Exception");
		}
	}

	public static void writeUserToFile(User user, String password, PrintWriter pWriter) {
		pWriter.println(user.getUsername() + ";" + user.getWins() + ";" + user.getGames() + ";" + user.getTimeToWin()
				+ ";" + user.getRank() + ";" + password);
		pWriter.flush();
	}

	public static void addToOnlineUsers(String username, PrintWriter pWriter) {
		pWriter.println(username);
		pWriter.flush();
	}

}
