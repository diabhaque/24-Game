package client.gamepanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.Panel;
import client.components.HeaderPanel;
import client.components.NavigationPanel;
import common.gameserver.Answer;
import common.gameserver.Game;
import common.user.User;

public class GameOverPanel extends JPanel {

	private NavigationPanel navPanel;
	private JPanel mainPanel;
	private JButton nextGameButton;
	private Answer answer;

	public GameOverPanel(Client client) {
		answer = client.getCurrentAnswer();
		User user = client.getCurrentUser();

		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());

		navPanel = new NavigationPanel(client);
		this.add(navPanel, BorderLayout.NORTH);

		GridLayout layout = new GridLayout(4, 1);
		layout.setVgap(5);
		mainPanel = new JPanel();
		mainPanel.setLayout(layout);
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(new HeaderPanel("Game Over"));
		mainPanel.add(new JLabel("Winner: " + answer.getUsername()));
		mainPanel.add(new JLabel("Winning Expression: " + answer.getExpr()));
		mainPanel.add(new JLabel("Time to win: " + answer.getTimeToWin() + "s"));
		this.add(mainPanel, BorderLayout.CENTER);

		nextGameButton = new JButton("Next game");
		this.add(nextGameButton, BorderLayout.SOUTH);

		nextGameButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (client.getGameServer() != null) {

					SwingWorker<Game, Void> worker = new SwingWorker<Game, Void>() {

						@Override
						protected Game doInBackground() throws Exception {

							client.setPanel(Panel.GAME_JOINING);
							client.setNavigationEnabled(false);
							client.getGameStartQSender().sendMessage(user.getUsername());

							Game game = client.getGamePlayTopicSubscriber().receiveGame();
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
