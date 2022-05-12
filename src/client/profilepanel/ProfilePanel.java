package client.profilepanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.Panel;
import client.components.HeaderPanel;
import client.components.NavigationPanel;
import common.user.User;

public class ProfilePanel extends JPanel {

	private NavigationPanel navPanel;
	private JPanel mainPanel;
	private JLabel rankLabel;

	public ProfilePanel(Client client) {
		User user = client.getCurrentUser();
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());

		navPanel = new NavigationPanel(client);
		this.add(navPanel, BorderLayout.NORTH);

		GridLayout layout = new GridLayout(5, 1);
		layout.setVgap(5);
		mainPanel = new JPanel();
		mainPanel.setLayout(layout);
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(new HeaderPanel(user.getUsername()));
		mainPanel.add(new JLabel("Number of wins: " + String.valueOf(user.getWins())));
		mainPanel.add(new JLabel("Number of games: " + String.valueOf(user.getGames())));
		mainPanel.add(new JLabel("Average time to win: " + String.valueOf(user.getTimeToWin())));
//		mainPanel.add(new JLabel("Rank: #" + String.valueOf(user.getRank())));
		rankLabel = new JLabel("Rank: Loading...");
		mainPanel.add(rankLabel);
		this.add(mainPanel, BorderLayout.CENTER);

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
					users.sort((o1, o2) -> Integer.valueOf(o1.getWins()).compareTo(Integer.valueOf(o2.getWins())));
					Collections.reverse(users);
					rankLabel.setText("Rank: #" + String.valueOf(getRank(users, user)));

				} catch (Exception e) {
					JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e.getMessage());
				}
			}
		};

		worker.execute();
	}

	private int getRank(ArrayList<User> users, User currentUser) {
		User user;
		for (int i = 0; i < users.size(); i++) {
			user = users.get(i);
			if (user != null && user.getUsername().equals(currentUser.getUsername())) {
				return i + 1;
			}
		}
		return 0;// not there is list
	}
}
