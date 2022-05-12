package client.gamepanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import javax.jms.JMSException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.Panel;
import client.components.HeaderPanel;
import client.components.NavigationPanel;
import client.joinpanel.ButtonPanel;
import common.gameserver.Answer;
import common.gameserver.Game;
import common.gameserver.PokerCard;
import common.user.User;

public class PlayingGamePanel extends JPanel {
	private NavigationPanel navPanel;
	private Client client;
	private Game game;
	private User user;
	private long startTime;

	public PlayingGamePanel(Client client) {
		this.client = client;
		user = client.getCurrentUser();
		game = client.getCurrentGame();
		PokerCard[] allowedCards = game.getPokerCards();

		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());

		navPanel = new NavigationPanel(client);
		this.add(navPanel, BorderLayout.NORTH);

		JPanel playerList = new PlayerListPanel(game.getPlayers());

		JPanel pokerPanel = new JPanel();
		pokerPanel.setLayout(new BorderLayout());
		JPanel pokerCards = new PokerCardPanel(allowedCards);
		JPanel answerPanel = new AnswerPanel(client, allowedCards);
		pokerPanel.add(pokerCards, BorderLayout.CENTER);
		pokerPanel.add(answerPanel, BorderLayout.SOUTH);

		this.add(playerList, BorderLayout.EAST);
		this.add(pokerPanel, BorderLayout.CENTER);

		startTime = System.currentTimeMillis() / 1000l;

		SwingWorker<Answer, Void> answerReceiver = new SwingWorker<Answer, Void>() {

			@Override
			protected Answer doInBackground() throws Exception {
				// Wait for answer from yourself or other players
				Answer answer;
				while (true) {
					answer = client.getAnswerTopicSubscriber().receiveAnswer();
					if (answer.getGameID().equals(game.getGameID())) {
						break;
					}
				}

				return answer;
			}

			@Override
			protected void done() {
				try {
					// Some player won. Game over.
					System.out.println("Game Over");
					Answer answer = (Answer) get();
					client.setCurrentGame(null);
					client.setCurrentAnswer(answer);
					client.setPanel(Panel.GAME_OVER);
					client.setNavigationEnabled(true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e.getMessage());
				}
			}
		};

		answerReceiver.execute();

	}

	public class PlayerListPanel extends JPanel {
		public PlayerListPanel(ArrayList<User> players) {
			GridLayout layout = new GridLayout(4, 1);
			layout.setVgap(2);
			this.setBorder(new EmptyBorder(10, 10, 10, 10));
			this.setLayout(layout);
			for (User player : players) {
				this.add(new PlayerCardPanel(player));
			}
		}
	}

	public class PlayerCardPanel extends JPanel {
		public PlayerCardPanel(User player) {
			GridLayout layout = new GridLayout(2, 1);
			layout.setVgap(1);
			this.setLayout(layout);
			this.setBorder(new EmptyBorder(10, 10, 10, 10));
			this.add(new HeaderPanel(player.getUsername(), 13));
			this.add(new JLabel(
					"Win: " + player.getWins() + "/" + player.getGames() + "; Avg: " + player.getTimeToWin() + "s"));
		}
	}

	public class PokerCardPanel extends JPanel {
		public PokerCardPanel(PokerCard[] cards) {
			GridLayout layout = new GridLayout(1, 4);
			layout.setHgap(8);
			this.setLayout(layout);
			this.setBorder(new EmptyBorder(10, 10, 10, 10));

			JLabel cardLabels[] = new JLabel[4];

			for (int i = 0; i < 4; i++) {
				cardLabels[i] = new JLabel();
				cardLabels[i].setIcon(cards[i].getIcon());
				this.add(cardLabels[i]);
			}

		}
	}

	public class AnswerPanel extends JPanel {
		private JPanel textPanel;
		private JPanel subTextPanel;
		private JTextField textField;
		private JLabel calcLabel;
		private JLabel equalsLabel;
		private JPanel buttonPanel;
		private JPanel subButtonPanel;
		private JButton calcButton;
		private JButton submitButton;

		private PokerCard[] allowedCards;

		public AnswerPanel(Client client, PokerCard[] cards) {
			allowedCards = cards;

			this.setLayout(new BorderLayout());

			textField = new JTextField(20);
			equalsLabel = new JLabel(" = ");
			calcLabel = new JLabel("0.0");
			textPanel = new JPanel();
			textPanel.setLayout(new BorderLayout());
			subTextPanel = new JPanel();
			subTextPanel.add(textField);
			subTextPanel.add(equalsLabel);
			subTextPanel.add(calcLabel);

			textPanel.add(subTextPanel, BorderLayout.WEST);
			this.add(textPanel, BorderLayout.CENTER);

			calcButton = new JButton("Calculate");
			submitButton = new JButton("Submit");
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new BorderLayout());
			subButtonPanel = new JPanel();
			subButtonPanel.add(calcButton);
			subButtonPanel.add(submitButton);
			buttonPanel.add(subButtonPanel, BorderLayout.WEST);
			this.add(buttonPanel, BorderLayout.SOUTH);

			calcButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					double result = Math.round(calculateValue() * 100.0) / 100.0;
					System.out.println(result);
					calcLabel.setText(String.valueOf(result));
				}

			});

			submitButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					double result = Math.round(calculateValue() * 100.0) / 100.0;
					if (result == 24) {
						long timeToWin = System.currentTimeMillis() / 1000l - startTime;
						if (client.getGameServer() != null) {

							try {
								client.getGamePlayQSender().sendAnswer(new Answer(game.getGameID(), getInput(), result,
										user.getUsername(), (int) timeToWin));
							} catch (JMSException e1) {
								JOptionPane.showMessageDialog(client.getGameFrame(), "Error: Unable to send answer");
							}
						}
					} else {
						JOptionPane.showMessageDialog(client.getGameFrame(),
								"Error: Expression must evaluate to 24. Try again.");
					}
				}

			});
		}

		public String getInput() {
			return textField.getText();
		}

		public double calculateValue() {
			// Player can use same card twice

			String expression = getInput();
			String[] splitExp = expression.split("[*\\-+/\\(\\) ]");
			ArrayList<Integer> possibleIntegers = new ArrayList<Integer>();

			for (PokerCard card : allowedCards) {
				possibleIntegers.add(card.getValue());
			}

			for (String v : splitExp) {
				if (v.isEmpty()) {
					continue;
				}
				// put in list, check for existence, remove, repeat
				if (!v.matches("\\d+") || !possibleIntegers.contains(Integer.valueOf(v))) {
					JOptionPane.showMessageDialog(client.getGameFrame(), "Error: Invalid Expression");
					return 0;
				} else {
					possibleIntegers.remove(new Integer(Integer.valueOf(v)));
				}
			}

			// check if input makes sense 4 integers only.
			return calculate(expression);
		}

		public double calculate(String s) {
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			try {
				Object result = engine.eval(s);
				if (result instanceof Double) {
					return (Double) result;
				} else if (result instanceof Integer) {
					return Double.valueOf(String.valueOf(result));
				}
			} catch (ScriptException e) {
				JOptionPane.showMessageDialog(client.getGameFrame(), "Error: Invalid Expression");
			}

			return 0;
		}
	}
}
