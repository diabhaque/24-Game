package client.gamepanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.components.NavigationPanel;
import common.user.User;

public class GamePanel extends JPanel {

	private NavigationPanel navPanel;
	private JPanel mainPanel;

	public GamePanel(Client client) {
		User user = client.getCurrentUser();
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());

		navPanel = new NavigationPanel(client);
		this.add(navPanel, BorderLayout.NORTH);
	}
}
