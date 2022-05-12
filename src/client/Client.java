package client;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import client.Panel;
import client.gamepanel.GameOverPanel;
import client.gamepanel.JoiningGamePanel;
import client.gamepanel.NewGamePanel;
import client.gamepanel.PlayingGamePanel;
import client.joinpanel.*;
import client.leaderboardpanel.LeaderBoardPanel;
import client.profilepanel.ProfilePanel;
import common.gameserver.Answer;
import common.gameserver.Game;
import common.gameserver.GameServer;
import common.gameserver.QueueSender;
import common.gameserver.TopicPublisher;
import common.gameserver.TopicSubscriber;
import common.user.User;
import server.ServerException;

public class Client {

	private JFrame gameFrame;
	private GameServer gameserver;
	private User currentUser;
	private ArrayList<User> allUsers;
	private boolean navigationEnabled = true;

	private QueueSender gameStartQSender;
	private QueueSender gamePlayQSender;
	private TopicSubscriber gamePlayTopicSubscriber;
	private TopicSubscriber answerTopicSubscriber;

	private Game currentGame;
	private Answer currentAnswer;

	public JFrame getGameFrame() {
		return gameFrame;
	}

	public GameServer getGameServer() {
		return gameserver;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User user) {
		currentUser = user;
	}

	public ArrayList<User> getAllUsers() {
		return allUsers;
	}

	public void setAllUsers(ArrayList<User> users) {
		allUsers = users;
	}

	public boolean getNavigationEnabled() {
		return navigationEnabled;
	}

	public void setNavigationEnabled(boolean b) {
		navigationEnabled = b;
	}

	public void setGameStartQSender(QueueSender qs) {
		gameStartQSender = qs;
	}

	public QueueSender getGameStartQSender() {
		return gameStartQSender;
	}

	public void setGamePlayQSender(QueueSender qs) {
		gamePlayQSender = qs;
	}

	public QueueSender getGamePlayQSender() {
		return gamePlayQSender;
	}

	public TopicSubscriber getGamePlayTopicSubscriber() {
		return gamePlayTopicSubscriber;
	}

	public void setGamePlayTopicSubscriber(TopicSubscriber ts) {
		gamePlayTopicSubscriber = ts;
	}

	public TopicSubscriber getAnswerTopicSubscriber() {
		return answerTopicSubscriber;
	}

	public void setAnswerTopicSubscriber(TopicSubscriber ts) {
		answerTopicSubscriber = ts;
	}

	public Game getCurrentGame() {
		return currentGame;
	}

	public void setCurrentGame(Game game) {
		currentGame = game;
	}

	public Answer getCurrentAnswer() {
		return currentAnswer;
	}

	public void setCurrentAnswer(Answer answer) {
		currentAnswer = answer;
	}

	public static void main(String[] args) {
		Client app = new Client(args[0]);
		try {
			app.setGameStartQSender(new QueueSender(args[0], "jms/GameStartConnectionFactory", "jms/GameStartQueue"));
			app.setGamePlayQSender(new QueueSender(args[0], "jms/GamePlayConnectionFactory", "jms/GamePlayQueue"));
			app.setGamePlayTopicSubscriber(
					new TopicSubscriber(args[0], "jms/GamePlayConnectionFactory", "jms/GamePlayTopic"));
			app.setAnswerTopicSubscriber(
					new TopicSubscriber(args[0], "jms/AnswerConnectionFactory", "jms/AnswerTopic"));
			app.go();
		} catch (NamingException | JMSException e) {
			System.err.println("Program aborted");
		}
	}

	public Client(String host) {
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			gameserver = (GameServer) registry.lookup("24-game-server");
		} catch (Exception e) {
			System.err.println("Failed accessing RMI: " + e);
		}
	}

	public void go() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				generateGUI();
			}
		});
	}

	public void setPanel(Panel panel) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				getGameFrame().setContentPane(getPanel(panel));
				getGameFrame().invalidate();
				getGameFrame().validate();
				getGameFrame().pack();
			}
		});
	}

	public void generateGUI() {
		Client client = this;
		gameFrame = new JFrame("24 Game");
		gameFrame.add(getPanel(Panel.LOGIN));
		gameFrame.pack();
		gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gameFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (client.getGameServer() != null && client.getCurrentUser() != null) {
					try {
						client.getGameServer().logout(client.getCurrentUser());
					} catch (Exception e) {
					}
				}
				gameFrame.dispose();

				if (client.getGameStartQSender() != null) {
					try {
						System.out.println("Closing GameStart Connection");
						client.getGameStartQSender().close();

					} catch (Exception e) {
					}
				}

				if (client.getGamePlayQSender() != null) {
					try {
						System.out.println("Closing GamePlay Q Connection");
						client.getGamePlayQSender().close();

					} catch (Exception e) {
					}
				}

				if (client.getGamePlayTopicSubscriber() != null) {
					try {
						System.out.println("Closing Game Play Topic Connection");
						client.getGamePlayTopicSubscriber().close();

					} catch (Exception e) {
					}
				}

				if (client.getAnswerTopicSubscriber() != null) {
					try {
						System.out.println("Closing Game Play Topic Connection");
						client.getAnswerTopicSubscriber().close();

					} catch (Exception e) {
					}
				}

				System.exit(0);
			}
		});
		getGameFrame().setVisible(true);
	}

	public JPanel getPanel(Panel panel) {
		switch (panel) {
		case LOGIN:
			return new LoginPanel(this);
		case REGISTER:
			return new RegisterPanel(this);
		case PROFILE:
			return new ProfilePanel(this);
		case GAME_NEW:
			return new NewGamePanel(this);
		case GAME_JOINING:
			return new JoiningGamePanel(this);
		case GAME_PLAYING:
			return new PlayingGamePanel(this);
		case GAME_OVER:
			return new GameOverPanel(this);
		case LEADERBOARD:
			return new LeaderBoardPanel(this);
		default:
			return null;
		}
	}

}
