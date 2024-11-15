package core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
import javax.swing.ToolTipManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import widgets.MainMenu;
import widgets.Throbber;
import widgets.UIConstants;

public class App extends JFrame {
    public static final String TOOL_NAME = "Toolsuite 2.0.0";
    private static App instance;
    public static final int NORMAL_MESSAGE = 0;
    public static final int WARNING_MESSAGE = 1;
    public static final int ERROR_MESSAGE = 2;

    SaveManager saveManager = SaveManager.get();
    UserInterfaceManager userInterfaceManager = UserInterfaceManager.get();

    private BorderLayout layout = new BorderLayout();

    private static final long serialVersionUID = 1L;

    private JLabel statusBar = new JLabel();
    private JPanel colorLabel = new JPanel(new BorderLayout());

    Throbber throbber;

    private JCheckBoxMenuItem itemDark;
    private JCheckBoxMenuItem itemLight;
    JPanel grid = new JPanel(new GridLayout(5, 5, 1, 1));

    private App() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        { // Create Menu Bar
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
            itemDark.addActionListener(e -> userInterfaceManager.setThemeDark(this));
            itemLight.addActionListener(e -> userInterfaceManager.setThemeLight(this));
            appearance.add(itemDark);
            appearance.add(itemLight);
        }
        saveManager.register(this);
        saveManager.load(this);
        if (saveManager.userSettings.bUseDarkMode) {
            userInterfaceManager.setThemeDark(this);
        } else {
            userInterfaceManager.setThemeLight(this);
        }
        getContentPane().setLayout(layout);

        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

        CompoundBorder b = new CompoundBorder(statusBar.getBorder(), new EmptyBorder(4, 8, 4, 8));
        statusBar.setBorder(b);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(colorLabel, BorderLayout.WEST);
        panel.add(statusBar, BorderLayout.CENTER);
        statusBar.setBackground(UIConstants.DodgerBlue);
        statusBar.setForeground(Color.WHITE);
        statusBar.setOpaque(true);
        colorLabel.setOpaque(true);
        colorLabel.setBackground(UIConstants.Red);
        colorLabel.setPreferredSize(new Dimension(24, 24));

        ImageIcon ico = new ImageIcon(loadResource("icon_loading_circle.png"));
        throbber = new Throbber(ico.getImage(), 20, 20, 2, 2);

        colorLabel.add(throbber, BorderLayout.CENTER);
        addScreen(new MainMenu(this), TOOL_NAME);
        getContentPane().add(panel, BorderLayout.SOUTH);
        setFocusable(true);
        setVisible(true);
    }

    public static BufferedImage loadResource(String resource) {
        // Try 868.154. all other ways to place, load and put dependencies for resources
        // DID NOT WORK.
        // FUCK JAVA. This is used for a packaged build
        InputStream in = App.class.getResourceAsStream("/resources/" + resource);
        if (in == null) {
            // This is used inside the IDE. Fuck java.
            in = App.class.getResourceAsStream("/" + resource);
        }
        if (in == null) return null;
        try {
            return ImageIO.read(in);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void addCell(int row, int col, Color background, Color foreground, String str) {

        JLabel label = new JLabel(str);
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        grid.add(label);
    }

    public void addScreen(JPanel panel, String title) {
        setTitle(title);
        Component c = layout.getLayoutComponent(BorderLayout.CENTER);
        if (c != null) {
            getContentPane().remove(c);
        }
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    public static JButton createButtonWithTextAndIcon(String text, String iconPath) {
        BufferedImage url = App.loadResource(iconPath);
        JButton button;
        if (url != null) {
            button = new JButton(text, new ImageIcon(url));
        } else {
            button = new JButton(text);
        }
        button.setBackground(UIConstants.DodgerBlue);
        button.setForeground(UIConstants.White);
        button.setPreferredSize(new Dimension(200, 32));
        button.setHorizontalAlignment(JButton.LEFT);
        button.setIconTextGap(24);

        return button;
    }

    public static JButton createButtonWithIcon(String iconPath, Color color) {
        BufferedImage url = App.loadResource(iconPath);
        JButton button;
        if (url != null) {
            button = new JButton(new ImageIcon(url));
        } else {
            button = new JButton("Img not found");
        }
        if(color == null)
        {
            color = UIConstants.DodgerBlue;
        }
        button.setBackground(color);
        button.setForeground(UIConstants.White);
        button.setPreferredSize(new Dimension(32, 32));
        button.setHorizontalAlignment(JButton.CENTER);
        button.setVerticalAlignment(JButton.CENTER);
        return button;
    }

    public static App get() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            throbber.startAnimation();
        } else {
            throbber.stopAnimation();
        }
    }

    public void setStatus(String message, int type) {
        statusBar.setText(message);
        switch (type) {
            default:
            case NORMAL_MESSAGE:
                colorLabel.setBackground(UIConstants.Goblin);
                break;
            case WARNING_MESSAGE:
                colorLabel.setBackground(UIConstants.Marigold);
                break;
            case ERROR_MESSAGE:
                colorLabel.setBackground(UIConstants.BitterSweet);
                break;
        }
    }

    public void showDocumentation() {
        String message = "<html><body><h1>Excelibur</h1><br>You can access this window all the time via the <b bgcolor=\"#282828\">Help</b> menu in the top left corner.<br>"
                + "<h3>Preparation:</h3><br><ul><li>Follow the naming scheme shown in the table below to set up the excel sheet(s)</li><li>Use <b bgcolor=\"#282828\">HTML ISO locales</b>"
                + "<li>Rows with an empty <b bgcolor=\"#282828\">component</b> or <b bgcolor=\"#282828\">key</b> cell are skipped during the import</li>"
                + "<li>When importing excel sheets this tool finds all data automatically. The tool <br>searches up to <b bgcolor=\"#282828\">column=AS</b> and <b bgcolor=\"#282828\">row=20</b> for any locales, 'component' and 'key' in every file and sheet</li></ul>"
                + "<h3>Result:</h3><ul><li>All found components and keys are extracted and sorted with the values in every language</li></ul>";

        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel infoText = new JLabel();
        infoText.setText(message);
        CompoundBorder b = new CompoundBorder(infoText.getBorder(), new EmptyBorder(8, 0, 24, 0));
        infoText.setBorder(b);
        mainPanel.add(infoText, BorderLayout.NORTH);

        grid.setOpaque(true);
        grid.setBackground(UIConstants.Red);


        addCell(0, 0, UIConstants.Mercury, UIConstants.Black, "");
        addCell(0, 1, UIConstants.Mercury, UIConstants.Black, "A");
        addCell(0, 2, UIConstants.Mercury, UIConstants.Black, "B");
        addCell(0, 3, UIConstants.Mercury, UIConstants.Black, "C");
        addCell(0, 4, UIConstants.Mercury, UIConstants.Black, "D");

        addCell(1, 0, UIConstants.Mercury, UIConstants.Black, "1");
        addCell(1, 1, UIConstants.White, UIConstants.Red2, "BRAND_NAME");
        addCell(1, 2, UIConstants.White, UIConstants.Black, "");
        addCell(1, 3, UIConstants.White, UIConstants.Black, "");
        addCell(1, 4, UIConstants.White, UIConstants.Black, "");

        addCell(2, 0, UIConstants.Mercury, UIConstants.Black, "2");
        addCell(2, 1, UIConstants.White, UIConstants.Red2, "component");
        addCell(2, 2, UIConstants.White, UIConstants.Red2, "key");
        addCell(2, 3, UIConstants.White, UIConstants.Red2, "de_DE");
        addCell(2, 4, UIConstants.White, UIConstants.Red2, "en_US");

        addCell(3, 0, UIConstants.Mercury, UIConstants.Black, "3");
        addCell(3, 1, UIConstants.White, UIConstants.Red2, "job_dialog");
        addCell(3, 2, UIConstants.White, UIConstants.Red2, "loading_heading");
        addCell(3, 3, UIConstants.White, UIConstants.Black, "Beispieltext 1");
        addCell(3, 4, UIConstants.White, UIConstants.Black, "Exampletext 2");

        addCell(4, 0, UIConstants.Mercury, UIConstants.Black, "4");
        addCell(4, 1, UIConstants.White, UIConstants.Red2, "job_dialog");
        addCell(4, 2, UIConstants.White, UIConstants.Red2, "loading_title");
        addCell(4, 3, UIConstants.White, UIConstants.Black, "Beispieltext 1");
        addCell(4, 4, UIConstants.White, UIConstants.Black, "Exampletext 2");

        mainPanel.add(grid, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(getContentPane(), mainPanel, "Documentation", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        App.get();
    }
}
