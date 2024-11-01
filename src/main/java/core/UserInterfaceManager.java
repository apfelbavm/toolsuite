package core;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class UIManager {
    private final SaveManager saveManager;

    public UIManager(SaveManager saveManager) {
        this.saveManager = saveManager;
    }

    UnsupportedLookAndFeelException setThemeDark(App app) {
        saveManager.userSettings.bUseDarkMode = true;
        try {
            javax.swing.UIManager.setLookAndFeel(new FlatDarkLaf());
            SwingUtilities.updateComponentTreeUI(app);
        } catch (UnsupportedLookAndFeelException e) {
            return e;
        }
        return null;
    }

    UnsupportedLookAndFeelException setThemeLight(App app) {
        saveManager.userSettings.bUseDarkMode = false;
        try {
            javax.swing.UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(app);
        } catch (UnsupportedLookAndFeelException e) {
            return e;
        }
        return null;
    }
}
