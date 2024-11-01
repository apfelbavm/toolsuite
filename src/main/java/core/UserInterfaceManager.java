package core;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class UserInterfaceManager {
    private static final UserInterfaceManager instance = new UserInterfaceManager();
    private final SaveManager saveManager = SaveManager.get();

    private UserInterfaceManager() {
    }

    public static UserInterfaceManager get() {
        return instance;
    }

    UnsupportedLookAndFeelException setThemeDark(JFrame frame) {
        saveManager.userSettings.bUseDarkMode = true;
        try {
            javax.swing.UIManager.setLookAndFeel(new FlatDarkLaf());
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (UnsupportedLookAndFeelException e) {
            return e;
        }
        return null;
    }

    UnsupportedLookAndFeelException setThemeLight(JFrame frame) {
        saveManager.userSettings.bUseDarkMode = false;
        try {
            javax.swing.UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (UnsupportedLookAndFeelException e) {
            return e;
        }
        return null;
    }
}
