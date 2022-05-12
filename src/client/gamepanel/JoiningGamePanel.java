package client.gamepanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.components.NavigationPanel;

public class JoiningGamePanel extends JPanel {

	private NavigationPanel navPanel;
	private JLabel label;
	
	public JoiningGamePanel(Client client) {
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());

		navPanel = new NavigationPanel(client);
		this.add(navPanel, BorderLayout.NORTH);
		
		label = new JLabel("Waiting for players...");
		label.setBorder(new EmptyBorder(10, 10, 10, 10));

		this.add(label, BorderLayout.CENTER);
	}
}
