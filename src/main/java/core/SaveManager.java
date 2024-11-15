package core;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class SaveManager {
    private static final String EXCELIBUR_IMPORT_FOLDER_STRING = "exceliburLastImportFolder";
    private static final String EXCELIBUR_EXPORT_FOLDER_STRING = "exceliburLastExportFolder";
    private static final String JSON_IMPORT_FOLDER_STRING = "jSONLastImportFolder";
    private static final String JSON_EXPORT_FOLDER_STRING = "jSONLastExportFolder";

    private static final SaveManager instance = new SaveManager();
    private Preferences prefs;
    public UserSettings userSettings = new UserSettings();

    private SaveManager() {
    }

    public static SaveManager get() {
        return instance;
    }

    public void register(JFrame frame) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                save(frame);
            }
        });
    }

    private void save(JFrame frame) {
        if (frame != null) {
            Point loc = frame.getLocation();
            prefs.putInt("X", loc.x);
            prefs.putInt("Y", loc.y);
            Dimension size = frame.getSize();
            prefs.putInt("W", size.width);
            prefs.putInt("H", size.height);
            prefs.putInt("EXTENDED_STATE", frame.getExtendedState());
            prefs.putBoolean("bUseDarkMode", userSettings.bUseDarkMode);
            prefs.put(EXCELIBUR_IMPORT_FOLDER_STRING, userSettings.exceliburLastImportFolder);
            prefs.put(EXCELIBUR_EXPORT_FOLDER_STRING, userSettings.exceliburLastExportFolder);
            prefs.put(JSON_IMPORT_FOLDER_STRING, userSettings.jSONLastImportFolder);
            prefs.put(JSON_EXPORT_FOLDER_STRING, userSettings.jSONLastExportFolder);
        } else {
            System.err.println("SaveManager::Save -> JFrame not found!");
        }
    }

    public void load(JFrame frame) {
        if (frame != null) {

            prefs = Preferences.userRoot().node(App.class.getSimpleName() + "-" + "MyExcelibur");
            int x = prefs.getInt("X", 100);
            int y = prefs.getInt("Y", 100);
            int w = Math.max(prefs.getInt("W", 800), 500);
            int h = Math.max(prefs.getInt("H", 600), 500);
            int extendedState = prefs.getInt("EXTENDED_STATE", JFrame.MAXIMIZED_BOTH);
            userSettings.bUseDarkMode = prefs.getBoolean("USE_DARK_MODE", true);
            userSettings.exceliburLastImportFolder = prefs.get(EXCELIBUR_IMPORT_FOLDER_STRING, "");
            userSettings.exceliburLastExportFolder = prefs.get(EXCELIBUR_EXPORT_FOLDER_STRING, "");
            userSettings.jSONLastImportFolder = prefs.get(JSON_IMPORT_FOLDER_STRING, "");
            userSettings.jSONLastExportFolder = prefs.get(JSON_EXPORT_FOLDER_STRING, "");

            // We need to clamp so that the frame never opens outside of the monitor
            // boundaries
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            int maxW = 0, maxH = 0;
            for (GraphicsDevice monitor : env.getScreenDevices()) {
                maxW += monitor.getDisplayMode().getWidth();
                if (monitor.getDisplayMode().getHeight() > maxH) maxH = monitor.getDisplayMode().getHeight();
            }

            w = Math.min(w, maxW);
            h = Math.min(h, maxH);
            if (x + w > maxW) {
                x = maxW - w;
            }
            if (y + h > maxH) {
                y = maxH - h;
            }
            frame.setLocation(x, y);
            frame.setSize(w, h);
            frame.setExtendedState(extendedState);
            frame.setMinimumSize(new Dimension(500, 500));
        }
    }
}
