package client.profilepanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.components.HeaderPanel;
import client.components.NavigationPanel;
import common.user.User;

public class ProfilePanel extends JPanel {

	private NavigationPanel navPanel;
	private JPanel mainPanel;

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
		mainPanel.add(new JLabel("Rank: #" + String.valueOf(user.getRank())));
		this.add(mainPanel, BorderLayout.SOUTH);
	}
}
