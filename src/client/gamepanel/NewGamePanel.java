package client.gamepanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.Panel;
import client.components.NavigationPanel;
import common.gameserver.Game;
import common.user.User;

public class NewGamePanel extends JPanel {
	private JButton newGameButton;
	private NavigationPanel navPanel;
	private Client client;

	public NewGamePanel(Client client) {
		this.client = client;
		User user = client.getCurrentUser();

		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());

		navPanel = new NavigationPanel(client);
		this.add(navPanel, BorderLayout.NORTH);

		newGameButton = new JButton("New Game");
		newGameButton.setSize(100, 60);
		this.add(newGameButton, BorderLayout.CENTER);

		newGameButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (client.getGameServer() != null) {

					SwingWorker<Game, Void> worker = new SwingWorker<Game, Void>() {

						@Override
						protected Game doInBackground() throws Exception {

							client.setPanel(Panel.GAME_JOINING);
							client.setNavigationEnabled(false);
							client.getGameStartQSender().sendMessage(user.getUsername());
							Game game;
							while (true) {
								game = client.getGamePlayTopicSubscriber().receiveGame();
								if (game.containsPlayer(user)) {
									break;
								}
							}
							return game;
						}

						@Override
						protected void done() {
							try {
								// Game Started
								System.out.println("starting game");
								Game game = (Game) get();
								client.setCurrentGame(game);
								client.setPanel(Panel.GAME_PLAYING);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e.getMessage());
							}
						}
					};

					worker.execute();

				}

			}

		});
	}

}
