package widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;

import core.App;

public class MainMenu extends JPanel
{
	private static final long serialVersionUID = 1L;

	App owner;

	public MainMenu(App owner)
	{
		this.owner = owner;

		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 0;
		con.insets = new Insets(8, 0, 8, 0);
		setLayout(grid);
		JButton button = App.createButtonWithTextAndIcon("Excelibur", "icon_excel.png");
		button.addActionListener(e -> owner.addScreen(new Excelibur(owner), "Excelibur"));
		button.setPreferredSize(new Dimension(200, 50));
		add(button, con);
		con.gridy = 1;
		JButton helpButton = App.createButtonWithTextAndIcon("Documentation", "icon_help.png");
		helpButton.setPreferredSize(new Dimension(200, 50));
		helpButton.addActionListener(e -> owner.showDocumentation());
		add(helpButton, con);
		owner.setStatus("Welcome to the Toolsuite...", App.NORMAL_MESSAGE);
	}
}
