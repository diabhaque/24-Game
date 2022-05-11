package server;

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;

import common.gameserver.GameServer;
import common.management.JoinException;
import common.user.User;

public class Server extends UnicastRemoteObject implements GameServer {

	private static final String DB_HOST = "sophia";
	private static final String DB_USER = "dhaque";
	private static final String DB_PASS = "sabian66";
	private static final String DB_NAME = "dhaque";

	private Connection conn;

	public static void main(String[] args) {
		try {
			Server app = new Server();
			System.setSecurityManager(new SecurityManager());
			Naming.rebind("24-game-server", app);
			System.out.println("Game Server registered");

		} catch (Exception ex) {
			System.err.println("Exception thrown in main: " + ex);
		}
	}

	public Server() throws RemoteException, ServerException {
		super();
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(
					"jdbc:mysql://" + DB_HOST + "/" + DB_NAME + "?user=" + DB_USER + "&password=" + DB_PASS);
			System.out.println("Database connection successful.");
			User.clearOnlineUsers(conn);

		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		}
	}

	@Override
	public User login(String username, String password) throws RemoteException, JoinException, ServerException {

		try {
			User user = null;

			// check if user is already logged in
			if (User.isUserOnline(username, conn)) {
				throw new JoinException("User already logged in!");
			}

			// check if user exists and passwords match
			user = User.getUser(username, password, conn);
			if (user == null) {
				throw new JoinException("Incorrect username or password");
			}

			// add to online user
			User.addOnlineUser(username, conn);
			return user;
		} catch (JoinException ex) {
			throw ex;
		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		}
	}

	@Override
	public User register(String username, String password) throws RemoteException, JoinException, ServerException {

		try {
			// Check if user already exists
			if (User.getUser(username, "", conn) != null) {
				throw new JoinException("Username already exists!");
			}

			User user = new User(username);
			User.registerUser(user, password, conn);
			User.addOnlineUser(username, conn);

			return user;
		} catch (JoinException ex) {
			throw ex;
		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		}

	}

	@Override
	public void logout(User user) throws RemoteException, ServerException {
		try {

			// check if user is already logged out
			if (!User.isUserOnline(user.getUsername(), conn)) {
				throw new JoinException("User already logged out!");
			}

			// delete user from online users
			User.removeOnlineUser(user.getUsername(), conn);

		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		}
	}

	@Override
	public ArrayList<User> getAllUsers() throws RemoteException, ServerException {
		try {
			return User.getAllUsers(conn);
		} catch (Exception ex) {
			System.err.println("Exception thrown: " + ex);
			throw new ServerException("Server Exception");
		}
	}

	@Override
	public ArrayList<User> getOnlineUsers() throws RemoteException, ServerException {
		// TODO Auto-generated method stub
		return null;
	}

}
