package client;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import client.Panel;
import client.gamepanel.GamePanel;
import client.joinpanel.*;
import client.leaderboardpanel.LeaderBoardPanel;
import client.profilepanel.ProfilePanel;
import common.gameserver.GameServer;
import common.user.User;
import server.ServerException;

public class Client {

	private JFrame gameFrame;
	private GameServer gameserver;
	private User currentUser;
	private ArrayList<User> allUsers;

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

	public static void main(String[] args) {
		Client app = new Client(args[0]);
		app.go();
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
		case GAME:
			return new GamePanel(this);
		case LEADERBOARD:
			return new LeaderBoardPanel(this);
		default:
			return null;
		}
	}

}
