package client.joinpanel;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ButtonPanel extends JPanel {

	public ButtonPanel(JButton loginButton, JButton registerButton) {
		this.setLayout(new BorderLayout());
		loginButton.setSize(100, 20);
		registerButton.setSize(100, 20);
		this.add(loginButton, BorderLayout.WEST);
		this.add(registerButton, BorderLayout.EAST);
		this.setBorder(new EmptyBorder(10, 30, 10, 30));
	}
}