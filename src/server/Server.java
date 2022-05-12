package server;

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.naming.NamingException;

import java.sql.Connection;
import java.sql.DriverManager;

import common.gameserver.Answer;
import common.gameserver.Game;
import common.gameserver.GameServer;
import common.gameserver.QueueReceiver;
import common.gameserver.TopicPublisher;
import common.management.JoinException;
import common.user.User;

public class Server extends UnicastRemoteObject implements GameServer {

	private static final String DB_HOST = "sophia";
	private static final String DB_USER = "dhaque";
	private static final String DB_PASS = "sabian66";
	private static final String DB_NAME = "dhaque";

	private Connection conn;
	private QueueReceiver gameStartQReceiver;
	private QueueReceiver gamePlayQReceiver;
	private TopicPublisher gamePlayTopicPublisher;
	private TopicPublisher answerTopicPublisher;

	public QueueReceiver getGameStartQReceiver() {
		return gameStartQReceiver;
	}

	public void setGameStartQReceiver(QueueReceiver qr) {
		gameStartQReceiver = qr;
	}

	public QueueReceiver getGamePlayQReceiver() {
		return gamePlayQReceiver;
	}

	public void setGamePlayQReceiver(QueueReceiver qr) {
		gamePlayQReceiver = qr;
	}

	public TopicPublisher getGamePlayTopicPublisher() {
		return gamePlayTopicPublisher;
	}

	public void setGamePlayTopicPublisher(TopicPublisher tp) {
		gamePlayTopicPublisher = tp;
	}

	public TopicPublisher getAnswerTopicPublisher() {
		return answerTopicPublisher;
	}

	public void setAnswerTopicPublisher(TopicPublisher tp) {
		answerTopicPublisher = tp;
	}

	public static void main(String[] args) {
		String host = "localhost";
		QueueReceiver gsqr = null;
		QueueReceiver gpqr = null;
		TopicPublisher gptp = null;
		TopicPublisher atp = null;
		try {
			Server app = new Server();
			// System.setSecurityManager(new SecurityManager());
			Naming.rebind("24-game-server", app);
			System.out.println("Game Server registered");

			gsqr = new QueueReceiver(host, "jms/GameStartConnectionFactory", "jms/GameStartQueue");
			gpqr = new QueueReceiver(host, "jms/GamePlayConnectionFactory", "jms/GamePlayQueue");
			gptp = new TopicPublisher(host, "jms/GamePlayConnectionFactory", "jms/GamePlayTopic");
			atp = new TopicPublisher(host, "jms/AnswerConnectionFactory", "jms/AnswerTopic");
			app.setGameStartQReceiver(gsqr);
			app.setGamePlayQReceiver(gpqr);
			app.setGamePlayTopicPublisher(gptp);
			app.setAnswerTopicPublisher(atp);

			int i = 0;
			while (true) {
				System.out.println("Waiting for game " + i);
				Game game = app.getGameStartQReceiver().waitForGame(app.conn);
				// start game
				app.getGamePlayTopicPublisher().sendGame(game);

				// while answer incorrect
				while (true) {
					Answer answer = app.getGamePlayQReceiver().receiveAnswer();
					if (answer.getValue() == 24) {
						app.getAnswerTopicPublisher().sendAnswer(answer);
						game.updatePlayerStatistics(answer, app.conn);
						break;
					}
				}

				// one game at a time now
				i++;
			}

		} catch (Exception ex) {
			System.err.println("Exception thrown in main: " + ex);
			ex.printStackTrace();
		} finally {
			if (gsqr != null) {
				try {
					gsqr.close();
				} catch (Exception e) {
				}
			}
			if (gpqr != null) {
				try {
					gpqr.close();
				} catch (Exception e) {
				}
			}
			if (gptp != null) {
				try {
					gpqr.close();
				} catch (Exception e) {
				}
			}
			if (atp != null) {
				try {
					gpqr.close();
				} catch (Exception e) {
				}
			}
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

			if (User.isUserOnline(username, conn)) {
				throw new JoinException("User already logged in!");
			}

			user = User.getUser(username, password, conn);
			if (user == null) {
				throw new JoinException("Incorrect username or password");
			}

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

			if (!User.isUserOnline(user.getUsername(), conn)) {
				throw new JoinException("User already logged out!");
			}

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
