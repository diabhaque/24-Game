package client.joinpanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FormTextInputPanel extends JPanel {

	private JLabel label;
	private JTextField textField;

	public FormTextInputPanel(String fieldName) {
		this.setLayout(new BorderLayout());
		label = new JLabel(fieldName);
		textField = new JTextField(20);
		this.add(label, BorderLayout.WEST);
		this.add(textField, BorderLayout.EAST);
	}

	public String getInput() {
		return textField.getText();
	}
}