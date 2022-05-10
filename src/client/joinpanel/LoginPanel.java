package client.joinpanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.Panel;
import client.components.HeaderPanel;
import common.management.JoinException;
import common.user.User;
import server.ServerException;

public class LoginPanel extends JPanel {

	private Client client;

	private HeaderPanel headerPanel;

	private JPanel formPanel;
	private FormTextInputPanel nameInput;
	private FormPasswordInputPanel passwordInput;

	private ButtonPanel buttonPanel;
	private JButton loginButton;
	private JButton registerButton;

	public LoginPanel(Client client) {
		this.client = client;

		this.setLayout(new BorderLayout());

		headerPanel = new HeaderPanel("Login");
		headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.add(headerPanel, BorderLayout.NORTH);

		formPanel = new JPanel();
		formPanel.setLayout(new GridLayout(2, 1));
		nameInput = new FormTextInputPanel("Login Name: ");
		passwordInput = new FormPasswordInputPanel("Password: ");
		formPanel.add(nameInput);
		formPanel.add(passwordInput);
		formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.add(formPanel, BorderLayout.CENTER);

		loginButton = new JButton("Login");
		registerButton = new JButton("Register");
		buttonPanel = new ButtonPanel(loginButton, registerButton);
		this.add(buttonPanel, BorderLayout.SOUTH);

		loginButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String username = nameInput.getInput();
				String password = passwordInput.getPassword();

				if (!(username.length() > 0 && password.length() > 0)) {
					JOptionPane.showMessageDialog(client.getGameFrame(), "Error: All fields are required!");
					return;
				}

				// Move to worker thread
				if (client.getGameServer() != null) {

					SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {

						@Override
						protected User doInBackground() throws Exception {
							return (User) client.getGameServer().login(username, password);
						}

						@Override
						protected void done() {
							try {
								User user = (User) get();
								client.setCurrentUser(user);
								client.setPanel(Panel.PROFILE);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e.getMessage());
							}
						}
					};

					worker.execute();

//					try {
//						User user = (User) client.getGameServer().login(username, password);
//						client.setCurrentUser(user);
//						client.setPanel(Panel.PROFILE);
//					} catch (Exception e1) {
//						JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e1.getMessage());
//					}

				}
			}
		});

		registerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				client.setPanel(Panel.REGISTER);
			}

		});
	}

}
