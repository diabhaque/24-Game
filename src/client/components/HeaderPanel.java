package client.components;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class HeaderPanel extends JPanel {
	private JLabel headerLabel;

	public HeaderPanel(String header) {
		this.setLayout(new BorderLayout());
		headerLabel = new JLabel(header);
		headerLabel.setFont(new Font("Courier", Font.BOLD, 16));
		this.add(headerLabel);
	}
	
	public HeaderPanel(String header, int fontSize) {
		this.setLayout(new BorderLayout());
		headerLabel = new JLabel(header);
		headerLabel.setFont(new Font("Courier", Font.BOLD, fontSize));
		this.add(headerLabel);
	}
}