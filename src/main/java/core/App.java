package core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import widgets.MainMenu;
import widgets.Throbber;

public class App extends JFrame
{
	public static final String TOOL_NAME = "Toolsuite 1.0.4";
	private static App INSTANCE;
	public static final int NORMAL_MESSAGE = 0;
	public static final int WARNING_MESSAGE = 1;
	public static final int ERROR_MESSAGE = 2;

	private BorderLayout layout = new BorderLayout();

	private static final long serialVersionUID = 1L;
	private Preferences prefs;
	private JLabel statusBar = new JLabel();
	private JPanel colorLabel = new JPanel(new BorderLayout());

	private boolean useDarkMode = false;
	Throbber throbber;

	private JCheckBoxMenuItem itemDark;
	private JCheckBoxMenuItem itemLight;

	public App()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenuBar();
		registerFrame("MyExcelibur", 100, 100, 800, 600, JFrame.MAXIMIZED_BOTH);
		getContentPane().setLayout(layout);

		ToolTipManager.sharedInstance().setInitialDelay(100);
		ToolTipManager.sharedInstance().setDismissDelay(10000);

		CompoundBorder b = new CompoundBorder(statusBar.getBorder(), new EmptyBorder(4, 8, 4, 8));
		statusBar.setBorder(b);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(colorLabel, BorderLayout.WEST);
		panel.add(statusBar, BorderLayout.CENTER);
		statusBar.setBackground(new Color(31, 144, 255, 255));
		statusBar.setForeground(Color.WHITE);
		statusBar.setOpaque(true);
		colorLabel.setOpaque(true);
		colorLabel.setBackground(new Color(255, 0, 0));
		colorLabel.setPreferredSize(new Dimension(24, 24));

		ImageIcon ico = new ImageIcon(loadResource("icon_loading_circle.png"));
		throbber = new Throbber(ico.getImage(), 20, 20, 2, 2);

		colorLabel.add(throbber, BorderLayout.CENTER);
		addScreen(new MainMenu(this), TOOL_NAME);
		getContentPane().add(panel, BorderLayout.SOUTH);

		setVisible(true);

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				Point loc = getLocation();
				prefs.putInt("X", loc.x);
				prefs.putInt("Y", loc.y);
				Dimension size = getSize();
				prefs.putInt("W", size.width);
				prefs.putInt("H", size.height);
				prefs.putInt("EXTENDED_STATE", getExtendedState());
				prefs.putBoolean("USE_DARK_MODE", useDarkMode);
			}
		});
	}

	public static BufferedImage loadResource(String resource)
	{
		// Try 868.154. all other ways to place, load and put dependencies for resources
		// DID NOT WORK.
		// FUCK JAVA. This is used for a packaged build
		InputStream in = App.class.getResourceAsStream("/resources/" + resource);
		if (in == null)
		{
			// This is used inside the IDE. Fuck java.
			in = App.class.getResourceAsStream("/" + resource);
		}
		if (in == null) return null;
		try
		{
			return ImageIO.read(in);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Help Menu
		JMenu menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);
		JMenuItem itemDocs = new JMenuItem("Documentation");
		itemDocs.addActionListener(e -> showDocumentation());
		menuHelp.add(itemDocs);

		// Theme Menu
		JMenu menuWindow = new JMenu("Window");
		menuBar.add(menuWindow);
		JMenu appearance = new JMenu("Appearence");
		menuWindow.add(appearance);
		itemDark = new JCheckBoxMenuItem("Dark Theme");
		itemLight = new JCheckBoxMenuItem("Light Theme");
		itemDark.addActionListener(e -> setThemeDark());
		itemLight.addActionListener(e -> setThemeLight());
		appearance.add(itemDark);
		appearance.add(itemLight);
	}

	void setThemeDark()
	{
		itemDark.setSelected(true);
		itemLight.setSelected(false);
		useDarkMode = true;
		try
		{
			UIManager.setLookAndFeel(new FlatDarkLaf());
			SwingUtilities.updateComponentTreeUI(this);
		}
		catch (UnsupportedLookAndFeelException e)
		{
			setStatus(e.getLocalizedMessage(), ERROR_MESSAGE);
		}
	}

	void setThemeLight()
	{
		itemDark.setSelected(false);
		itemLight.setSelected(true);
		useDarkMode = false;
		try
		{
			UIManager.setLookAndFeel(new FlatLightLaf());
			SwingUtilities.updateComponentTreeUI(this);
		}
		catch (UnsupportedLookAndFeelException e)
		{
			setStatus(e.getLocalizedMessage(), ERROR_MESSAGE);
		}
	}

	public static JButton createButtonWithTextAndIcon(String text, String iconPath)
	{
		BufferedImage url = App.loadResource(iconPath);
		JButton button;
		if (url != null)
		{
			button = new JButton(text, new ImageIcon(url));
		}
		else
		{
			button = new JButton(text);
		}
		button.setBackground(new Color(31, 144, 255));
		button.setForeground(Color.WHITE);
		button.setPreferredSize(new Dimension(200, 32));
		button.setHorizontalAlignment(JButton.LEFT);
		button.setIconTextGap(24);

		return button;
	}

	public void setLoading(boolean isLoading)
	{
		if (isLoading)
		{
			throbber.startAnimation();
		}
		else
		{
			throbber.stopAnimation();
		}
	}

	public void showDocumentation()
	{
		String message = "<html><body><h1>Excelibur</h1><br>You can access this window all the time via the <b bgcolor=\"#282828\">Help</b> menu in the top left corner.<br>"
			+ "<h3>Preparation:</h3><br><ul><li>Follow the naming scheme shown in the table below to set up the excel sheet(s)</li><li>Use <b bgcolor=\"#282828\">HTML ISO locales</b>"
			+ "<li>Rows with an empty <b bgcolor=\"#282828\">component</b> or <b bgcolor=\"#282828\">key</b> cell are skipped during the import</li>"
			+ "<li>When importing excel sheets this tool finds all data automatically. For that the tool <br>searches up to <b bgcolor=\"#282828\">column=AS</b> and <b bgcolor=\"#282828\">row=20</b> for any locales, 'component' and 'key' in every file and sheet</li></ul>"
			+ "<h3>Result:</h3><ul><li>All found components and keys are extracted and sorted with the values in every language</li></ul>";

		JPanel mainPanel = new JPanel(new BorderLayout());
		JLabel infoText = new JLabel();
		infoText.setText(message);
		CompoundBorder b = new CompoundBorder(infoText.getBorder(), new EmptyBorder(8, 0, 24, 0));
		infoText.setBorder(b);
		mainPanel.add(infoText, BorderLayout.NORTH);

		JPanel grid = new JPanel(new GridLayout(3, 4, 1, 1));
		grid.setOpaque(true);
		grid.setBackground(new Color(255, 0, 0));

		Color colorHeader = new Color(230, 230, 230);
		Color colorHeaderText = new Color(0, 0, 0);

		String[] headers = new String[] { "component", "key", "de_DE", "en_US" };
		for (String cell : headers)
		{
			JLabel label = new JLabel(cell);
			label.setOpaque(true);
			label.setBackground(colorHeader);
			label.setForeground(colorHeaderText);
			grid.add(label);
		}

		Color colorCell = new Color(255, 255, 255);

		String[] cells = new String[] { "job_dialog", "loading_heading", "Beispieltext 1", "Exampletext 2", "job_dialog", "loading_title", "Beispieltext 1", "Exampletext 2" };
		for (String cell : cells)
		{
			JLabel label = new JLabel(cell);
			label.setOpaque(true);
			label.setBackground(colorCell);
			label.setForeground(colorHeaderText);
			grid.add(label);
		}

		mainPanel.add(grid, BorderLayout.CENTER);

		JOptionPane.showMessageDialog(getContentPane(), mainPanel, "Documentation", JOptionPane.INFORMATION_MESSAGE);
	}

	public static App get()
	{
		if (INSTANCE == null) INSTANCE = new App();
		return INSTANCE;
	}

	public void addScreen(JPanel panel, String title)
	{
		setTitle(title);
		Component c = layout.getLayoutComponent(BorderLayout.CENTER);
		if (c != null)
		{
			getContentPane().remove(c);
		}
		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	private void registerFrame(String frameUniqueID, int defaultX, int defaultY, int defaultW, int defaultH, int defaultExtendedState)
	{
		prefs = Preferences.userRoot().node(App.class.getSimpleName() + "-" + frameUniqueID);
		int x = prefs.getInt("X", defaultX);
		int y = prefs.getInt("Y", defaultY);
		int w = Math.max(prefs.getInt("W", defaultW), 500);
		int h = Math.max(prefs.getInt("H", defaultH), 500);
		int extendedState = prefs.getInt("EXTENDED_STATE", defaultExtendedState);
		useDarkMode = prefs.getBoolean("USE_DARK_MODE", true);

		if (useDarkMode)
		{
			setThemeDark();
		}
		else
		{
			setThemeLight();
		}

		// We need to clamp so that the frame never opens outside of the monitor
		// boundaries
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		int maxW = 0, maxH = 0;
		for (GraphicsDevice monitor : env.getScreenDevices())
		{
			maxW += monitor.getDisplayMode().getWidth();
			if (monitor.getDisplayMode().getHeight() > maxH) maxH = monitor.getDisplayMode().getHeight();
		}

		w = Math.min(w, maxW);
		h = Math.min(h, maxH);
		if (x + w > maxW)
		{
			x = maxW - w;
		}
		if (y + h > maxH)
		{
			y = maxH - h;
		}
		setLocation(x, y);
		setSize(w, h);
		setExtendedState(extendedState);
		setMinimumSize(new Dimension(500, 500));
	}

	public void setStatus(String message, int type)
	{
		statusBar.setText(message);
		switch (type)
		{
		default:
		case NORMAL_MESSAGE:
			colorLabel.setBackground(new Color(65, 125, 88));
			break;
		case WARNING_MESSAGE:
			colorLabel.setBackground(new Color(178, 139, 35));
			break;
		case ERROR_MESSAGE:
			colorLabel.setBackground(new Color(255, 110, 110));
			break;
		}
	}

	public static void main(String[] args)
	{
		App.get();
	}
}
