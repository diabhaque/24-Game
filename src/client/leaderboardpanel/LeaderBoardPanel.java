package client.leaderboardpanel;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.Panel;
import client.components.NavigationPanel;
import common.user.User;

public class LeaderBoardPanel extends JPanel {

	private NavigationPanel navPanel;
	private JTable leaderBoard;
	private JPanel mainPanel;
	private ArrayList<String[]> data;

	private String[] columnNames = { "Rank", "Player", "Games Won", "Games Played", "Avg Winning Time (s)" };

	public LeaderBoardPanel(Client client) {
		ArrayList<User> users = client.getAllUsers();
		users.sort((o1, o2) -> Integer.valueOf(o1.getWins()).compareTo(Integer.valueOf(o2.getWins())));
		Collections.reverse(users);
			
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());

		navPanel = new NavigationPanel(client);
		this.add(navPanel, BorderLayout.NORTH);

		data = new ArrayList<String[]>();

		int i = 1;
		for (User user : users) {
			String[] userData = { String.valueOf(i), user.getUsername(), String.valueOf(user.getWins()),
					String.valueOf(user.getGames()), String.valueOf(user.getTimeToWin()) };
			data.add(userData);
			i += 1;
		}

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		leaderBoard = new JTable(data.toArray(new String[0][0]), columnNames);
		mainPanel.add(leaderBoard.getTableHeader(), BorderLayout.NORTH);
		mainPanel.add(leaderBoard, BorderLayout.CENTER);


		this.add(mainPanel, BorderLayout.CENTER);
	}
}
