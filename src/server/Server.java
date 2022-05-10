package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import common.gameserver.GameServer;
import common.management.JoinException;
import common.management.UserManagement;
import common.user.User;

public class Server extends UnicastRemoteObject implements GameServer {

	private static final String DB_HOST = "sophia";
	private static final String DB_USER = "dhaque";
	private static final String DB_PASS = "sabian66";
	private static final String DB_NAME = "dhaque";

	private Connection conn;

	String onlineUsersFile = "OnlineUser.txt";
	BufferedReader userInfoFileReader;
	PrintWriter userInfoFileWriter;

	String userInfoFile = "UserInfo.txt";
	BufferedReader onlineUsersFileReader;
	PrintWriter onlineUsersFileWriter;

	public static void main(String[] args) {
		try {
			Server app = new Server();
			System.setSecurityManager(new SecurityManager());
			Naming.rebind("24-game-server", app);
			System.out.println("Game Server registered");

		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
		}
	}

	public Server() throws RemoteException, ServerException, SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super();
		try {
			onlineUsersFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(onlineUsersFile)));
			onlineUsersFileWriter.flush();

			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(
					"jdbc:mysql://" + DB_HOST + "/" + DB_NAME + "?user=" + DB_USER + "&password=" + DB_PASS);
			System.out.println("Database connection successful.");

		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		}
	}

	@Override
	public User login(String username, String password) throws RemoteException, JoinException, ServerException {

		try {
			User user = null;
			Boolean found = false;
			userInfoFileReader = new BufferedReader(new FileReader(userInfoFile));
			onlineUsersFileReader = new BufferedReader(new FileReader(onlineUsersFile));
			onlineUsersFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(onlineUsersFile, true)));

			// check if user already logged in
			for (String line = onlineUsersFileReader.readLine(); line != null; line = onlineUsersFileReader
					.readLine()) {
				if (line.equals(username)) {
					throw new JoinException("User already logged in!");
				}
			}

			if (User.isUserOnline(username, conn)) {
				throw new JoinException("User already logged in!");
			}

			// check if user exists and passwords match
			user = User.getUser(username, password, conn);
			if (user == null) {
				throw new JoinException("Incorrect username or password");
			}

			for (String line = userInfoFileReader.readLine(); line != null; line = userInfoFileReader.readLine()) {
				String[] userinfo = line.split(";");
				if (userinfo[0].equals(username)) {
					if (userinfo[5].equals(password)) {
						found = true;
						user = new User(userinfo[0], Integer.valueOf(userinfo[1]), Integer.valueOf(userinfo[2]),
								Integer.valueOf(userinfo[3]), Integer.valueOf(userinfo[4]));
					} else {
						throw new JoinException("Incorrect Password");
					}
				}
			}

			if (!found) {
				throw new JoinException("User with username " + username + " does not exist!");
			}

			// add to online user
			User.addToOnlineUsers(user.getUsername(), onlineUsersFileWriter);
			return user;
		} catch (JoinException ex) {
			throw ex;
		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		} finally {
			try {
				userInfoFileReader.close();
				onlineUsersFileWriter.close();
			} catch (Exception ex2) {
				System.err.println("Exception thrown: " + ex2);
				throw new ServerException("Server Exception");
			}
		}
	}

	@Override
	public User register(String username, String password) throws RemoteException, JoinException, ServerException {

		try {
			userInfoFileReader = new BufferedReader(new FileReader(userInfoFile));
			userInfoFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(userInfoFile, true)));
			onlineUsersFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(onlineUsersFile, true)));

			// Check if user already exists
			if (User.getUser(username, "", conn) != null) {
				throw new JoinException("Username already exists!");
			}

			for (String line = userInfoFileReader.readLine(); line != null; line = userInfoFileReader.readLine()) {
				if (line.split(";")[0].equals(username)) {
					throw new JoinException("Username already exists!");
				}
			}

			User user = new User(username);
			User.writeUserToFile(user, password, userInfoFileWriter);
			User.addToOnlineUsers(username, onlineUsersFileWriter);

			return user;
		} catch (JoinException ex) {
			throw ex;
		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		} finally {
			try {
				userInfoFileReader.close();
				userInfoFileWriter.close();
				onlineUsersFileWriter.close();
			} catch (Exception ex2) {
				System.err.println("Exception thrown: " + ex2);
				throw new ServerException("Server Exception");
			}
		}

	}

	@Override
	public void logout(User user) throws RemoteException, ServerException {
		try {
			onlineUsersFileReader = new BufferedReader(new FileReader(onlineUsersFile));
			ArrayList<String> onlineUsers = new ArrayList<String>();
			for (String line = onlineUsersFileReader.readLine(); line != null; line = onlineUsersFileReader
					.readLine()) {
				if (!line.equals(user.getUsername())) {
					onlineUsers.add(line);
				}
			}

			onlineUsersFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(onlineUsersFile)));
			onlineUsersFileWriter.flush();

			for (String line : onlineUsers) {
				User.addToOnlineUsers(line, onlineUsersFileWriter);
			}

		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		} finally {
			try {
				onlineUsersFileReader.close();
				onlineUsersFileWriter.close();
			} catch (Exception ex2) {
				System.err.println("Exception thrown: " + ex2);
				throw new ServerException("Server Exception");
			}
		}
	}

	@Override
	public ArrayList<User> getAllUsers() throws RemoteException, ServerException {

		try {
			userInfoFileReader = new BufferedReader(new FileReader(userInfoFile));
			ArrayList<User> users = new ArrayList<User>();

			for (String line = userInfoFileReader.readLine(); line != null; line = userInfoFileReader.readLine()) {
				String[] userinfo = line.split(";");
				users.add(new User(userinfo[0], Integer.valueOf(userinfo[1]), Integer.valueOf(userinfo[2]),
						Integer.valueOf(userinfo[3]), Integer.valueOf(userinfo[4])));
			}
			return users;
		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		} finally {
			try {
				userInfoFileReader.close();
			} catch (Exception ex2) {
				System.err.println("Exception thrown: " + ex2);
				throw new ServerException("Server Exception");
			}
		}
	}

	@Override
	public ArrayList<User> getOnlineUsers() throws RemoteException, ServerException {
		// TODO Auto-generated method stub
		return null;
	}

}
