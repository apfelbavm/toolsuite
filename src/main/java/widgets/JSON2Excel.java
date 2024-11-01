package widgets;

import core.SaveManager;
import core.TranslationMgr;
import reader.FileReader;
import core.App;
import reader.OnBrandMissing;
import reader.OnLocaleMissing;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class JSON2Excel extends JPanel implements OnLocaleMissing, OnBrandMissing {
    private static final long serialVersionUID = 1L;

    App owner;
    JButton returnButton, importButton;
    JSplitPane horSplit = new JSplitPane();
    FileReader reader;
    SaveManager saveManager = SaveManager.get();

    public JSON2Excel(App owner) {
        this.owner = owner;
        owner.setStatus("Welcome to Excelibur..", App.NORMAL_MESSAGE);
        setLayout(new BorderLayout());
        reader = new FileReader();
        reader.bindOnRequestLocale(this);
        reader.bindOnRequestBrand(this);

        returnButton = App.createButtonWithTextAndIcon("Back", "icon_return.png");
        returnButton.addActionListener(e -> owner.addScreen(new MainMenu(owner), App.TOOL_NAME));

        {
            importButton = App.createButtonWithTextAndIcon("Choose JSON files...", "icon_import.png");
            importButton.setToolTipText("Import files via a selection dialog. If any new file is imported the current selection of files will be removed and the table content is refreshed.");
            importButton.addActionListener(e -> openInputDialog());
        }

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
        tempPanel.add(returnButton, BorderLayout.NORTH);
        tempPanel.add(importButton);

        JSplitPane leftSplitPane = new JSplitPane();
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setEnabled(false);
        leftSplitPane.setDividerSize(0);

        leftSplitPane.setTopComponent(tempPanel);

        horSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        horSplit.setEnabled(false);
        horSplit.setDividerSize(0);
        horSplit.setLeftComponent(leftSplitPane);

        add(horSplit, BorderLayout.CENTER);
    }

    void openInputDialog() {
        owner.setStatus("Choosing files to import...", App.NORMAL_MESSAGE);
        FileFilter filter = new FileNameExtensionFilter("JavaScript Object Notation (*.json)", "json");

        if (saveManager.userSettings.jSONLastImportFolder.isBlank() || saveManager.userSettings.jSONLastImportFolder.isEmpty()) {
            String userDir = System.getProperty("user.home");
            saveManager.userSettings.jSONLastImportFolder = userDir + "/Desktop";
        }
        JFileChooser fileChooser = new JFileChooser(saveManager.userSettings.jSONLastImportFolder);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(filter);
        fileChooser.setPreferredSize(new Dimension(800, 600));

        // This sets the default folder view to 'details'
        Action details = fileChooser.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);

        int choice = fileChooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFiles().length > 0) {
                File files[] = fileChooser.getSelectedFiles();
                saveManager.userSettings.jSONLastImportFolder = files[0].getParent();
                reader.read(files);
            }
        } else if (choice == JFileChooser.CANCEL_OPTION) {
            owner.setStatus("Aborted import...", App.NORMAL_MESSAGE);
        }
    }

    @Override
    public String onLocaleMissing(File file) {
        return showDialogForLocale(file);
    }

    @Override
    public String onBrandMissing(File file) {
        return showDialogForBrand(file);
    }

    private String showDialogForLocale(File file) {
        String arr[] = TranslationMgr.ISO_CODES.toArray(new String[0]);
        Arrays.sort(arr);
        return (String) JOptionPane.showInputDialog(
                owner,
                "Could'nt detect locale, please specify manually for:\n" + file.getAbsolutePath() + "\n",
                "Choose Locale",
                JOptionPane.PLAIN_MESSAGE,
                null,
                arr, "en_us");
    }

    private String showDialogForBrand(File file) {

        return (String) JOptionPane.showInputDialog(
                owner,
                "Could'nt detect brand, please specify manually for:\n" + file.getAbsolutePath() + "\n",
                "Choose Brand",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null, null);
    }
}
