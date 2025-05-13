package widgets;

import core.App;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class LoginScreen extends JPanel {
    App app;
    JLabel codeLabel = new JLabel("None");
    JLabel urlLabel = new JLabel("None");
    JLabel urlMessageLabel = new JLabel("Öffnen Sie Ihren Browser:");
    JLabel codeMessageLabel = new JLabel("und geben Sie die angezeigte Nummer ein, um sich anzumelden.");
    JButton loginButton = new JButton("Login");

    public LoginScreen(App owner) {
        app = owner;
        owner.setStatus("Please Login to your Microsoft Account..", App.NORMAL_MESSAGE);
        setLayout(new BorderLayout());
        loginButton.addActionListener(e -> doLogin());

        JPanel flowPanel = new JPanel();

        flowPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        Border newBorder = BorderFactory.createLineBorder(UIConstants.Black);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(5, 1));

        codeLabel.setBackground(UIConstants.DodgerBlue);
        codeLabel.setBorder(newBorder);
        codeLabel.setOpaque(true);

        urlLabel.setBackground(UIConstants.DodgerBlue);
        urlLabel.setBorder(newBorder);
        urlLabel.setOpaque(true);

        gridPanel.add(loginButton);
        gridPanel.add(urlMessageLabel);
        gridPanel.add(urlLabel);
        gridPanel.add(codeMessageLabel);
        gridPanel.add(codeLabel);

        add(flowPanel, BorderLayout.CENTER);
        flowPanel.add(gridPanel);
    }

    void doLogin() {
        app.auth.loginNew(this);
        app.auth.getUser();
    }

    public void showVerificationCode(String code) {
        codeLabel.setText(code);

    }

    public void showVerificationURL(String url) {
        SwingUtilities.invokeLater(() -> urlLabel.setText(url));
    }
}
