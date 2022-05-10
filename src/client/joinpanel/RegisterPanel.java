package client.joinpanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

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

public class RegisterPanel extends JPanel {

	private Client client;

	private HeaderPanel headerPanel;

	private JPanel formPanel;
	private FormTextInputPanel nameInput;
	private FormPasswordInputPanel passwordInput;
	private FormPasswordInputPanel confirmPasswordInput;

	private ButtonPanel buttonPanel;
	private JButton loginButton;
	private JButton registerButton;

	public RegisterPanel(Client client) {

		this.client = client;
		this.setLayout(new BorderLayout());

		headerPanel = new HeaderPanel("Register");
		headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.add(headerPanel, BorderLayout.NORTH);

		formPanel = new JPanel();
		formPanel.setLayout(new GridLayout(3, 1));
		nameInput = new FormTextInputPanel("Login Name: ");
		passwordInput = new FormPasswordInputPanel("Password: ");
		confirmPasswordInput = new FormPasswordInputPanel("Confirm Password: ");
		formPanel.add(nameInput);
		formPanel.add(passwordInput);
		formPanel.add(confirmPasswordInput);
		formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.add(formPanel, BorderLayout.CENTER);

		loginButton = new JButton("Login");
		registerButton = new JButton("Register");
		buttonPanel = new ButtonPanel(loginButton, registerButton);
		this.add(buttonPanel, BorderLayout.SOUTH);

		loginButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				client.setPanel(Panel.LOGIN);
			}

		});

		registerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String username = nameInput.getInput();
				String password = passwordInput.getPassword();
				String confirmPassword = confirmPasswordInput.getPassword();

				if (!(username.length() > 0 && password.length() > 0 && confirmPassword.length() > 0)) {
					JOptionPane.showMessageDialog(client.getGameFrame(), "Error: All fields are required!");
					return;
				}

				if (username.contains(";") || password.contains(";")) {
					JOptionPane.showMessageDialog(client.getGameFrame(),
							"Username and password cannot contain the character ';'");
					return;
				}

				if (!password.equals(confirmPassword)) {
					System.out.println("Passwords do not match");
					JOptionPane.showMessageDialog(client.getGameFrame(), "Error: Passwords do not match!");
					return;
				}

				if (client.getGameServer() != null) {

					SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {

						@Override
						protected User doInBackground() throws Exception {
							return (User) client.getGameServer().register(username, password);
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
//						User user = (User) client.getGameServer().register(username, password);
//						client.setCurrentUser(user);
//						client.setPanel(Panel.PROFILE);
//					} catch (Exception e1) {
//						JOptionPane.showMessageDialog(client.getGameFrame(), "Error: " + e1.getMessage());
//					}
				}

			}

		});
	}
}

//-Djava.security.policy="${workspace_loc:*******/security.policy}"
