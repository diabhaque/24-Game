package client.joinpanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class FormPasswordInputPanel extends JPanel {

	private JLabel label;
	private JPasswordField pwdField;

	public FormPasswordInputPanel(String fieldName) {
		this.setLayout(new BorderLayout());
		label = new JLabel(fieldName);
		pwdField = new JPasswordField(20);
		this.add(label, BorderLayout.WEST);
		this.add(pwdField, BorderLayout.EAST);
	}

	public String getPassword() {
		return new String(pwdField.getPassword());
	}
}