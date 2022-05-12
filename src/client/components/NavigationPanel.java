package client.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import client.Client;
import client.Panel;
import common.management.JoinException;
import common.user.User;
import server.ServerException;

public class NavigationPanel extends JPanel {

	private JButton profileButton;
	private JButton gameButton;
	private JButton leaderBoardButton;
	private JButton logoutButton;

	public NavigationPanel(Client client) {
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		profileButton = new JButton("User Profile");
		gameButton = new JButton("Play Game");
		leaderBoardButton = new JButton("Leader Board");
		logoutButton = new JButton("Logout");

		this.add(profileButton, c);
		this.add(gameButton, c);
		this.add(leaderBoardButton, c);
		this.add(logoutButton, c);

		profileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (client.getNavigationEnabled()) {
					client.setPanel(Panel.PROFILE);
				}
			}

		});

		gameButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (client.getNavigationEnabled()) {
					client.setPanel(Panel.GAME_NEW);
				}
			}

		});

		leaderBoardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (client.getGameServer() != null && client.getNavigationEnabled()) {

					SwingWorker<ArrayList<User>, Void> worker = new SwingWorker<ArrayList<User>, Void>() {

						@Override
						protected ArrayList<User> doInBackground() throws Exception {
							return (ArrayList<User>) client.getGameServer().getAllUsers();

						}

						@Override
						protected void done() {
							try {
								ArrayList<User> users = (ArrayList<User>) get();
								client.setAllUsers(users);
								client.setPanel(Panel.LEADERBOARD);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e.getMessage());
							}
						}
					};

					worker.execute();
				}

			}

		});

		logoutButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (client.getGameServer() != null && client.getNavigationEnabled()) {

					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

						@Override
						protected Void doInBackground() throws Exception {
							client.getGameServer().logout(client.getCurrentUser());
							return null;
						}

						@Override
						protected void done() {
							try {
								client.setCurrentUser(null);
								client.setPanel(Panel.LOGIN);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e.getMessage());
							}
						}
					};

					worker.execute();

//					try {
//						client.getGameServer().logout(client.getCurrentUser());
//						client.setCurrentUser(null);
//						client.setPanel(Panel.LOGIN);
//					} catch (Exception e1) {
//						JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e1.getMessage());
//					}
				}
			}

		});
	}
}
