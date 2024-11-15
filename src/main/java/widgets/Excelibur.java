package widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import core.App;
import core.SaveManager;
import core.TranslationMgr;
import core.TranslationMgrFlags;
import reader.OnBrandMissing;
import reader.OnLocaleMissing;
import widgets.table.LanguageTable;
import widgets.table.GroupableTableCtrl;

public class Excelibur extends JPanel implements OnLocaleMissing, OnBrandMissing {

    private TranslationMgr translationMgr = new TranslationMgr();
    private static final long serialVersionUID = 1L;
    private SaveManager saveManager = SaveManager.get();
    App owner;
    JList<String> fileList = new JList<String>();

    JButton importButton, exportButton, returnButton, reloadButton;

    JSplitPane horSplit = new JSplitPane();

    JCheckBox checkBoxAutoResize = new JCheckBox("Auto Resize Table");
    JCheckBox checkBoxMergeCompAndKey = new JCheckBox("Concat. 'Component' and 'Key'");
    JCheckBox checkBoxUseHyperlinkIfAvailable = new JCheckBox("Get hyperlink");
    JCheckBox checkIncludeHiddenSheets = new JCheckBox("Include hidden sheets");
    GroupableTableCtrl tableCtrl = new GroupableTableCtrl();

    JComboBox<String> comboFolderNaming;

    public Excelibur(App owner) {

        this.owner = owner;
        owner.setStatus("Welcome to Excelibur..", App.NORMAL_MESSAGE);
        setLayout(new BorderLayout());
        returnButton = App.createButtonWithTextAndIcon("Back", "icon_return.png");
        returnButton.addActionListener(e -> owner.addScreen(new MainMenu(owner), App.TOOL_NAME));

        // TABLE
        DefaultTableModel model = new DefaultTableModel(new String[]{"Component", "Key", "Locale"}, 0);
        //table = new JTable(model);
        //table.setFillsViewportHeight(true);

        JPanel infoPanel = new JPanel();
        GridLayout grid = new GridLayout(13, 1, 8, 0);
        infoPanel.setLayout(grid);
        CompoundBorder b = new CompoundBorder(infoPanel.getBorder(), new EmptyBorder(4, 4, 4, 4));
        infoPanel.setBorder(b);

        checkBoxAutoResize.setSelected(true);
        checkBoxAutoResize.addItemListener(e -> tableCtrl.updateTableAutoResizing(checkBoxAutoResize.isSelected()));
        checkBoxAutoResize.setToolTipText("Change how the data is displayed in the table. Either fit to the window's size (enabled) or match each column's width to it's content (disabled)");

        checkBoxMergeCompAndKey.setSelected(false);
        checkBoxMergeCompAndKey.setToolTipText("Concatenates component and key. That means 'dialog' and 'heading' become 'dialog_heading'.\nThis eventually reduces the json tree depth by 1");
        checkBoxUseHyperlinkIfAvailable.setSelected(false);
        checkBoxUseHyperlinkIfAvailable.setToolTipText("Replaces cell content with hyperlink if any");
        checkIncludeHiddenSheets.setSelected(false);
        checkIncludeHiddenSheets.setToolTipText("Consider hidden and very hidden sheets in excel during import. Usually this can be toggled off.");

        createOutputFolderComboBox();

        infoPanel.add(new JLabel("View settings:"));
        infoPanel.add(checkBoxAutoResize);
        infoPanel.add(new JLabel(""));
        infoPanel.add(new JLabel("Import settings:"));
        infoPanel.add(checkBoxUseHyperlinkIfAvailable);
        infoPanel.add(new JLabel(""));
        infoPanel.add(new JLabel("Export settings:"));
        infoPanel.add(checkBoxMergeCompAndKey);
        infoPanel.add(checkIncludeHiddenSheets);
        JLabel outputFolderRuleLabel = new JLabel("Output folder (struct):");
        outputFolderRuleLabel.setForeground(new Color(128, 128, 128));
        infoPanel.add(outputFolderRuleLabel);
        infoPanel.add(comboFolderNaming);
        infoPanel.add(new JLabel());

        JLabel infoImportedFiles = new JLabel("Imported file(s):");
        b = new CompoundBorder(infoImportedFiles.getBorder(), new EmptyBorder(4, 4, 4, 4));
        infoImportedFiles.setBorder(b);

        JPanel filePane = new JPanel();
        filePane.setLayout(new BorderLayout());
        filePane.add(infoImportedFiles, BorderLayout.NORTH);
        filePane.add(new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        JSplitPane leftSplitPane = new JSplitPane();
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setEnabled(false);
        leftSplitPane.setDividerSize(0);

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
        tempPanel.add(returnButton, BorderLayout.NORTH);
        tempPanel.add(infoPanel, BorderLayout.CENTER);

        leftSplitPane.setBottomComponent(filePane);
        leftSplitPane.setTopComponent(tempPanel);

        leftSplitPane.setMinimumSize(new Dimension(200, 5));
        leftSplitPane.setMaximumSize(new Dimension(200, 2000));
        leftSplitPane.setPreferredSize(new Dimension(200, 2000));

        horSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        horSplit.setEnabled(false);
        horSplit.setDividerSize(0);
        horSplit.setLeftComponent(leftSplitPane);
        horSplit.setRightComponent(new JScrollPane(tableCtrl, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        reloadButton = App.createButtonWithTextAndIcon("Reload tables...", "icon_refresh.png");
        reloadButton.setToolTipText("Reimport the selected files. Be sure to have all imported Excel files closed or they won't be imported as Excel blocks the files when opened.");
        reloadButton.addActionListener(e -> updateTableView());
        importButton = App.createButtonWithTextAndIcon("Choose Excel files...", "icon_import.png");
        importButton.setToolTipText("Import files via a selection dialog. If any new file is imported the current selection of files will be removed and the table content is refreshed.");
        importButton.addActionListener(e -> openInputDialog());
        exportButton = App.createButtonWithTextAndIcon("Export", "icon_export.png");
        exportButton.setToolTipText("Bulk export every language to a .json. The locale is appended to the filename so 'translation' changes to 'translation_de_DE' etc.");
        exportButton.addActionListener(e -> openOutputDialog());

        JPanel importPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        importPanel.add(reloadButton);
        importPanel.add(importButton);

        filePane.add(importPanel, BorderLayout.SOUTH);
        FlowLayout flow = new FlowLayout(FlowLayout.RIGHT);
        flow.setHgap(0);
        flow.setVgap(0);

        JPanel leftButtonPanel = new JPanel(flow);
        leftButtonPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel rightButtonPanel = new JPanel(flow);
        rightButtonPanel.add(exportButton);
        rightButtonPanel.setAlignmentX(RIGHT_ALIGNMENT);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setEnabled(false);
        split.setDividerSize(0);
        split.setLeftComponent(leftButtonPanel);
        split.setRightComponent(rightButtonPanel);

        add(horSplit, BorderLayout.CENTER);
        add(split, BorderLayout.SOUTH);
        enableUserInput(true);
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
        String arr1[] = TranslationMgr.ISO_CODES.toArray(new String[0]);
        String arr2[] = TranslationMgr.SUCCESSFACTOR_CODES.toArray(new String[0]);

        String[] mergedArray = new String[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, mergedArray, 0, arr1.length);
        System.arraycopy(arr2, 0, mergedArray, arr1.length, arr2.length);
        Arrays.sort(mergedArray);

        String Result = null;
        while (Result == null || Result.isBlank() || Result.isEmpty()) {
            Result = (String) JOptionPane.showInputDialog(
                    owner,
                    "Could'nt detect locale, please specify manually for:\n" + file.getAbsolutePath() + "\n",
                    "Choose Locale",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    mergedArray, "en_us");
        }
        return Result;
    }

    private String showDialogForBrand(File file) {

        String Result = null;
        while (Result == null || Result.isBlank() || Result.isEmpty()) {
            Result = (String) JOptionPane.showInputDialog(
                    owner,
                    "Could'nt detect brand, please specify manually for:\n" + file.getAbsolutePath() + "\n",
                    "Choose Brand",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null, null);
        }
        return Result;
    }

    private void createOutputFolderComboBox() {
        String[] list = new String[3];
        int i = 0;
        for (TranslationMgrFlags.FolderNaming rule : TranslationMgrFlags.FolderNaming.values()) {
            switch (rule) {
                case LOCALE_BRAND:
                    list[i] = "locale_Brand";
                    break;
                case BRAND_LOCALE:
                    list[i] = "Brand_locale";
                    break;
                case BRAND_AND_LOCALE_AS_SUBFOLDER:
                    list[i] = "Brand / locale";
                    break;
            }
            ++i;
        }
        comboFolderNaming = new JComboBox<String>(list);
    }

    void openInputDialog() {
        owner.setStatus("Choosing files to import...", App.NORMAL_MESSAGE);
        FileFilter xlsxfilter = new FileNameExtensionFilter("Microsoft Excel Documents (*.xlsx)", "xlsx");
        FileFilter jsonfilter = new FileNameExtensionFilter("JavaScript Object Notation (*.json)", "json");

        if (saveManager.userSettings.exceliburLastImportFolder.isBlank() || saveManager.userSettings.exceliburLastImportFolder.isEmpty()) {
            String userDir = System.getProperty("user.home");
            saveManager.userSettings.exceliburLastImportFolder = userDir + "/Desktop";
        }
        // https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
        JFileChooser fileChooser = new JFileChooser(saveManager.userSettings.exceliburLastImportFolder);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(xlsxfilter);
        fileChooser.addChoosableFileFilter(jsonfilter);
        fileChooser.setPreferredSize(new Dimension(800, 600));

        // This sets the default folder view to 'details'
        Action details = fileChooser.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);

        int choice = fileChooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFiles().length > 0) {
                translationMgr.files = fileChooser.getSelectedFiles();
                saveManager.userSettings.exceliburLastImportFolder = translationMgr.files[0].getParent();
                updateListView();
                updateTableView();
            }
        } else if (choice == JFileChooser.CANCEL_OPTION) {
            owner.setStatus("Aborted import...", App.NORMAL_MESSAGE);
        }
    }

    private void openOutputDialog() {
        if (translationMgr.getNumSelectedFiles() == 0) {
            JOptionPane.showMessageDialog(this, "Please import Excel sheets first", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        owner.setStatus("Selecting output folder...", App.NORMAL_MESSAGE);

        if (saveManager.userSettings.exceliburLastExportFolder.isBlank() || saveManager.userSettings.exceliburLastExportFolder.isEmpty()) {
            String userDir = System.getProperty("user.home");
            saveManager.userSettings.exceliburLastExportFolder = userDir + "/Desktop";
        }
        JFileChooser chooser = new JFileChooser(saveManager.userSettings.exceliburLastExportFolder);
        chooser.setSelectedFile(new File("translations"));
        chooser.setPreferredSize(new Dimension(800, 600));
        // This sets the default folder view to 'details'
        Action details = chooser.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);
        int choice = chooser.showSaveDialog(this);
        translationMgr.startTimeTrace();
        enableUserInput(false);
        if (choice == JFileChooser.APPROVE_OPTION) {
            new Thread(() -> {
                String outputFolder = chooser.getSelectedFile().toString();
                saveManager.userSettings.exceliburLastExportFolder = chooser.getSelectedFile().getParent();
                int i = outputFolder.lastIndexOf(System.getProperty("file.separator"));
                String fileName = outputFolder.substring(i + 1, outputFolder.length());
                outputFolder = outputFolder.substring(0, i);
                boolean success = exportData(outputFolder, fileName);
                if (success) {
                    translationMgr.stopTimeTrace();
                    double seconds = (double) translationMgr.getCalculationTime();
                    String secondsString = String.format("%.2f", seconds);
                    owner.setStatus("Sucessfully exported Excel sheet(s) within " + secondsString + "s...", App.NORMAL_MESSAGE);
                }
                enableUserInput(true);
            }).start();
        } else {
            owner.setStatus("Aborted export...", App.NORMAL_MESSAGE);
            enableUserInput(true);
        }
    }

    private void updateListView() {
        fileList.setVisibleRowCount(-1);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        String[] fileNames = new String[translationMgr.getNumSelectedFiles()];
        int i = 0;
        for (File file : translationMgr.files) {
            fileNames[i] = translationMgr.getFileName(file.getName());
            ++i;
        }
        fileList.setListData(fileNames);
        fileList.setSelectedIndex(0);
    }

    private void updateTableView() {
        if (translationMgr.getNumSelectedFiles() == 0) return;

        translationMgr.startTimeTrace();
        owner.setStatus("Extracting data...", App.NORMAL_MESSAGE);

        enableUserInput(false);
        owner.setLoading(true);
        new Thread(() -> {
            importData();
            enableUserInput(true);
            owner.setLoading(false);
        }).start();
    }

    private void importData() {
        translationMgr.setFlag(TranslationMgrFlags.Import.USE_HYPERLINK_IF_AVAILABLE, checkBoxUseHyperlinkIfAvailable.isSelected());
        translationMgr.setFlag(TranslationMgrFlags.Import.INCLUDE_HIDDEN_SHEETS, checkIncludeHiddenSheets.isSelected());
        LanguageTable languageTable = translationMgr.importFiles(this);
        Component comp = horSplit.getRightComponent();
        if (comp != null) horSplit.remove(comp);

        tableCtrl.createTable(languageTable);
        tableCtrl.updateTableAutoResizing(checkBoxAutoResize.isSelected());

        horSplit.setRightComponent(tableCtrl);

        translationMgr.stopTimeTrace();
        double seconds = (double) translationMgr.getCalculationTime();
        String secondsString = String.format("%.2f", seconds);
        switch (translationMgr.statNumEmptyCells) {
            case 0:
                owner.setStatus("Sucessfully imported Excel sheet(s) within " + secondsString + "s...", App.NORMAL_MESSAGE);
                break;
            case 1:
                owner.setStatus(
                        "Sucessfully imported Excel sheet(s) within " + secondsString + "s but there was " + translationMgr.statNumEmptyCells + " empty cell found! Watch out for the red marked cells!",
                        App.WARNING_MESSAGE);
                break;
            default:
                owner.setStatus(
                        "Sucessfully imported Excel sheet(s) within  " + secondsString + "s but there were " + translationMgr.statNumEmptyCells + " empty cells found! Watch out for the red marked cells!",
                        App.WARNING_MESSAGE);
                break;
        }
    }

    private void enableUserInput(boolean bEnable) {
        boolean bAnyFilesImported = translationMgr.getNumSelectedFiles() > 0 && tableCtrl.getRowCount() > 0;
        // prevent export if no files are in the "imported" list
        exportButton.setEnabled(bEnable && bAnyFilesImported);
        reloadButton.setEnabled(bEnable && bAnyFilesImported);
        importButton.setEnabled(bEnable);
        returnButton.setEnabled(bEnable);
        checkBoxAutoResize.setEnabled(bEnable);
        checkBoxMergeCompAndKey.setEnabled(bEnable);
        checkIncludeHiddenSheets.setEnabled(bEnable);
        checkBoxUseHyperlinkIfAvailable.setEnabled(bEnable);
        comboFolderNaming.setEnabled(bEnable);
    }

    private boolean exportData(String outputFolder, String fileName) {
        translationMgr.setFlag(TranslationMgrFlags.Export.CONCAT_COMPONENT_AND_KEY, checkBoxMergeCompAndKey.isSelected());

        translationMgr.folderNamingType = TranslationMgrFlags.FolderNaming.getValue(comboFolderNaming.getSelectedIndex());
        return translationMgr.export2Json(outputFolder, fileName);
    }


}
