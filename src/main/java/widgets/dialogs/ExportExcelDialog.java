package widgets.dialogs;

import core.SaveManager;
import translations.I18nCSB;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Calendar;

public class ExportExcelDialog {
    private final SaveManager saveManager = SaveManager.get();
    JFileChooser chooser;

    public int showDialog(Component parentComponent, I18nCSB csb) {
        chooser = new JFileChooser(saveManager.userSettings.exceliburLastExportFolder);

        String suggestedFileName = csb.brands.get(0).name + "_Workbook_" + Calendar.getInstance().get(Calendar.YEAR);
        chooser.setSelectedFile(new File(suggestedFileName));
        chooser.setPreferredSize(new Dimension(800, 600));
        // This sets the default folder view to 'details'
        Action details = chooser.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);
        return chooser.showSaveDialog(parentComponent);
    }

    public File getSelectedFile() {
        return chooser.getSelectedFile();
    }
}
