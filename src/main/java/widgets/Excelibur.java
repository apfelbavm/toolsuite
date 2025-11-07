package widgets;

import java.awt.*;
import java.io.File;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import core.*;
import reader.OnBrandMissing;
import reader.OnLocaleMissing;
import reader.ReaderConfig;
import reader.SupportedFileType;
import widgets.dialogs.ExportChoice;
import widgets.dialogs.ExportExcelDialog;
import widgets.dialogs.ExportOptionsDialog;
import widgets.dialogs.MergeExcelDialog;
import widgets.tab_control.Tab;
import widgets.table.LanguageTable;
import widgets.table.GroupableTable;
import writer.ExcelWriter;
import writer.FileWriter;
import writer.FileWriterOptions;
import writer.WriterConfig;

public class Excelibur extends JPanel implements OnLocaleMissing, OnBrandMissing {

    public Tab tab = null;
    private final TranslationMgr translationMgr = new TranslationMgr();
    private final TranslationMgr translationMgrDifference = new TranslationMgr();
    @Serial
    private static final long serialVersionUID = 1L;
    private final SaveManager saveManager = SaveManager.get();
    App owner;
    FileList<String> fileList = new FileList<String>();

    JButton importButton, exportButton, reloadButton;

    JSplitPane horSplit = new JSplitPane();
    JCheckBox checkBoxMergeCompAndKey = new JCheckBox("Concat. 'Component' and 'Key'");
    JCheckBox checkBoxUseHyperlinkIfAvailable = new JCheckBox("Get hyperlink instead of cell text");
    JCheckBox checkIncludeHiddenSheets = new JCheckBox("Include hidden sheets");
    JCheckBox checkDoNotExportEmptyCells = new JCheckBox("Skip empty values");
    GroupableTable table = new GroupableTable();
    ShortcutManager shortcuts = new ShortcutManager();
    JComboBox<String> comboFolderNaming;

    public Excelibur(App owner) {

        this.owner = owner;
        shortcuts.init(this);
        owner.setStatus("Welcome to Excelibur..", App.NORMAL_MESSAGE);
        setLayout(new BorderLayout());

        // TABLE
        DefaultTableModel model = new DefaultTableModel(new String[]{"Component", "Key", "Locale"}, 0);
        //table = new JTable(model);
        //table.setFillsViewportHeight(true);

        JPanel infoPanel = new JPanel();
        GridLayout grid = new GridLayout(11, 1, 8, 0);
        infoPanel.setLayout(grid);
        CompoundBorder b = new CompoundBorder(infoPanel.getBorder(), new EmptyBorder(4, 4, 4, 4));
        infoPanel.setBorder(b);

        table.updateTable(null);


        checkBoxMergeCompAndKey.setSelected(false);
        checkBoxMergeCompAndKey.setToolTipText("Concatenates component and key. That means 'dialog' and 'heading' become 'dialog_heading'.\nThis eventually reduces the json tree depth by 1");
        checkBoxUseHyperlinkIfAvailable.setSelected(false);
        checkBoxUseHyperlinkIfAvailable.setToolTipText("Replaces cell content with hyperlink if any");
        checkIncludeHiddenSheets.setSelected(false);
        checkIncludeHiddenSheets.setToolTipText("Consider hidden and very hidden sheets in excel during import. Usually this can be toggled off.");
        checkDoNotExportEmptyCells.setSelected(true);
        checkDoNotExportEmptyCells.setToolTipText("Don't export key value pairs with empty values. This is for each language individually");

        createOutputFolderComboBox();

        infoPanel.add(new JLabel("Import settings:"));
        infoPanel.add(checkBoxUseHyperlinkIfAvailable);
        infoPanel.add(checkIncludeHiddenSheets);
        infoPanel.add(new JLabel(""));
        infoPanel.add(new JLabel("Export settings:"));
        infoPanel.add(checkBoxMergeCompAndKey);
        infoPanel.add(checkDoNotExportEmptyCells);
        JLabel outputFolderRuleLabel = new JLabel("Output folder (struct):");
        outputFolderRuleLabel.setForeground(UIConstants.Gray);
        infoPanel.add(outputFolderRuleLabel);
        infoPanel.add(comboFolderNaming);
        infoPanel.add(new JLabel());

        JLabel infoImportedFiles = new JLabel("Imported");
        b = new CompoundBorder(infoImportedFiles.getBorder(), new EmptyBorder(4, 4, 4, 4));
        infoImportedFiles.setBorder(b);

        reloadButton = App.createButtonWithIcon("icon_refresh.png", UIConstants.DodgerBlue);
        reloadButton.setToolTipText("Reimport the selected files.");
        reloadButton.addActionListener(e -> updateTableView());

        JPanel importedFilesPanel = new JPanel(new BorderLayout());
        importedFilesPanel.add(infoImportedFiles, BorderLayout.WEST);
        importedFilesPanel.add(reloadButton, BorderLayout.EAST);

        JPanel filePane = new JPanel();
        filePane.setLayout(new BorderLayout());
        filePane.add(importedFilesPanel, BorderLayout.NORTH);
        filePane.add(new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        JSplitPane leftSplitPane = new JSplitPane();
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setEnabled(false);
        leftSplitPane.setDividerSize(0);

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
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
        horSplit.setRightComponent(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        importButton = App.createButtonWithTextAndIcon("Import files...", "icon_import.png");
        importButton.setToolTipText("Import files and folders via a selection dialog. On import the current selection of files is cleared.");
        importButton.addActionListener(e -> startImport());
        exportButton = App.createButtonWithTextAndIcon("Export", "icon_export.png");
        exportButton.setToolTipText("Bulk export every language to a .json. The locale is appended to the filename so 'translation' changes to 'translation_de_DE' etc.");
        exportButton.addActionListener(e -> openExportDialog());

        filePane.add(importButton, BorderLayout.SOUTH);
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
        String[] arr1 = TranslationMgr.ISO_CODES.toArray(new String[0]);
        String[] arr2 = TranslationMgr.SUCCESSFACTOR_CODES.toArray(new String[0]);

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
        String[] list = new String[TranslationMgrFlags.FolderNaming.values().length];
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
                case LOCALE_AND_BRAND_AS_SUBFOLDER:
                    list[i] = "Locale / brand";
                    break;
            }
            ++i;
        }
        comboFolderNaming = new JComboBox<String>(list);
        comboFolderNaming.setSelectedIndex(saveManager.userSettings.exportFolderNaming.ordinal());
        comboFolderNaming.addActionListener(e -> {
            saveManager.userSettings.exportFolderNaming = TranslationMgrFlags.FolderNaming.getValue(comboFolderNaming.getSelectedIndex());
        });
    }

    class ImportDialogConfig {
        public ImportDialogConfig() {
            bAllowJson = false;
            bAllowXLSX = false;
            bAllowMultiSelection = false;
        }

        boolean bAllowJson;
        boolean bAllowXLSX;
        boolean bAllowMultiSelection;
    }

    void startImport() {
        ImportDialogConfig cfg = new ImportDialogConfig();
        cfg.bAllowJson = true;
        cfg.bAllowXLSX = true;
        cfg.bAllowMultiSelection = true;

        File[] files = openInputDialog(cfg);
        if (files != null) {
            translationMgr.files = files;
            saveManager.userSettings.exceliburLastImportFolder = translationMgr.files[0].getParent();
            updateListView();
            updateTableView();
        }
    }


    File[] openInputDialog(ImportDialogConfig cfg) {
        owner.setStatus("Choosing files to import...", App.NORMAL_MESSAGE);

        if (saveManager.userSettings.exceliburLastImportFolder.isBlank() || saveManager.userSettings.exceliburLastImportFolder.isEmpty()) {
            String userDir = System.getProperty("user.home");
            saveManager.userSettings.exceliburLastImportFolder = userDir + "/Desktop";
        }
        // https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
        JFileChooser fileChooser = new JFileChooser(saveManager.userSettings.exceliburLastImportFolder);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);

        if (cfg.bAllowXLSX && SupportedFileType.isSupported("xlsx")) {
            SupportedFileType fileType = SupportedFileType.get("xlsx");
            FileFilter xlsxfilter = new FileNameExtensionFilter(fileType.description, fileType.extension);
            fileChooser.addChoosableFileFilter(xlsxfilter);
        }
        if (cfg.bAllowJson && SupportedFileType.isSupported("json")) {
            SupportedFileType fileType = SupportedFileType.get("json");
            FileFilter jsonfilter = new FileNameExtensionFilter(fileType.description, fileType.extension);
            fileChooser.addChoosableFileFilter(jsonfilter);
        }
        fileChooser.setPreferredSize(new Dimension(800, 600));

        // This sets the default folder view to 'details'
        Action details = fileChooser.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);

        int choice = fileChooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFiles().length > 0) {
                ArrayList<File> files = new ArrayList<>();
                for (File file : fileChooser.getSelectedFiles()) {
                    if (file.isFile()) {
                        String fileExtension = StringHelper.getFileExtension(file.getName());
                        if (SupportedFileType.isSupported(fileExtension)) {
                            files.add(file);
                        }
                    } else if (file.isDirectory()) {
                        files.addAll(getAllFiles(file));
                    }
                }
                File[] fileArray = new File[files.size()];
                return files.toArray(fileArray);
            }
        } else if (choice == JFileChooser.CANCEL_OPTION) {
            owner.setStatus("Aborted import...", App.NORMAL_MESSAGE);
        }
        return null;
    }

    ArrayList<File> getAllFiles(File directory) {
        ArrayList<File> foundFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileExtension = StringHelper.getFileExtension(file.getName());
                    if (SupportedFileType.isSupported(fileExtension)) {
                        foundFiles.add(file);
                    }
                } else if (file.isDirectory()) {
                    foundFiles.addAll(getAllFiles(file));
                }
            }
        }
        return foundFiles;
    }

    private void updateListView() {
        fileList.setVisibleRowCount(-1);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        String[] fileNames = new String[translationMgr.getNumSelectedFiles()];

        int i = 0;
        for (File file : translationMgr.files) {
            fileNames[i++] = file.getName();
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

    private ReaderConfig createReaderConfig() {
        ReaderConfig config = new ReaderConfig();
        config.bExcelUseHyperlinkIfAvailable = checkBoxUseHyperlinkIfAvailable.isSelected();
        config.bExcelIncludeHiddenSheets = checkIncludeHiddenSheets.isSelected();
        return config;
    }

    private void importData() {
        translationMgr.importFiles(this, null, createReaderConfig());
        if (translationMgr.csb != null) {
            LanguageTable languageTable = translationMgr.csb.createLanguageTable(null);
            Component comp = horSplit.getRightComponent();
            if (comp != null) horSplit.remove(comp);
            table.updateTable(languageTable);
        } else {
            table.updateTable(null);
        }

        renameTab();
        horSplit.setRightComponent(table);

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
        boolean bAnyFilesImported = translationMgr.getNumSelectedFiles() > 0 && table.getRowCount() > 0;
        // prevent export if no files are in the "imported" list
        exportButton.setEnabled(bEnable && bAnyFilesImported);
        reloadButton.setEnabled(bEnable && bAnyFilesImported);
        importButton.setEnabled(bEnable);
        table.setEnabled(bEnable);
        checkBoxMergeCompAndKey.setEnabled(bEnable);
        checkDoNotExportEmptyCells.setEnabled(bEnable);
        checkIncludeHiddenSheets.setEnabled(bEnable);
        checkBoxUseHyperlinkIfAvailable.setEnabled(bEnable);
        comboFolderNaming.setEnabled(bEnable);
    }

    private boolean exportData(String outputFolder, String fileName) {
        translationMgr.setFlag(TranslationMgrFlags.Export.CONCAT_COMPONENT_AND_KEY, checkBoxMergeCompAndKey.isSelected());
        translationMgr.setFlag(TranslationMgrFlags.Export.DONT_EXPORT_EMPTY_VALUES, checkDoNotExportEmptyCells.isSelected());
        translationMgr.folderNamingType = TranslationMgrFlags.FolderNaming.getValue(comboFolderNaming.getSelectedIndex());
        return translationMgr.export2Json(outputFolder, fileName);
    }

    void openExportDialog() {
        ExportOptionsDialog dialog = new ExportOptionsDialog();
        ExportChoice choice = dialog.showDialog(this);

//        String[] options = {"Export as Json", "Export as Excel File", "Merge into Excel File"};
////        String[] options = {"Export as Json"};
//        JOptionPane pane = new JOptionPane();
//        pane.setPreferredSize(new Dimension(800, 600));
//        int selection = pane.showOptionDialog(this, "How would you like to export the data?", "Export",
//                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case ABORT: {
                break;
            }
            case EXPORT_JSON: {
                openExportJsonDialog();
                break;
            }
            case EXPORT_EXCEL: {
                openExportNewExcelDialog();
                break;
            }
            case MERGE_EXCEL: {
                File file = openFillExistingExcelDialog();
                if (translationMgrDifference.csb.isValid()) {
                    if (file != null) {
                        openSelectExcelSheetDialog(file);
                    }
                } else {
                    int success = JOptionPane.showConfirmDialog(this, "No translation differences found", "No changes", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
                }
                enableUserInput(true);
                break;
            }
        }

    }

    File openFillExistingExcelDialog() {
        ImportDialogConfig dialogCfg = new ImportDialogConfig();
        dialogCfg.bAllowXLSX = true;
        File[] files = openInputDialog(dialogCfg);
        enableUserInput(false);
        if (files != null && files.length > 0) {

            translationMgrDifference.importFiles(this, files, createReaderConfig());
            translationMgrDifference.csb.makeDifferenceTo(translationMgr.csb);
//            System.out.println("START DIFFERENCES");
//            translationMgrDifference.csb.print();
//            System.out.println("END DIFFERENCES");
            return files[0];
        }
        return null;
    }

    void openExportNewExcelDialog() {
        owner.setStatus("Selecting output folder...", App.NORMAL_MESSAGE);

        if (saveManager.userSettings.exceliburLastExportFolder.isBlank() || saveManager.userSettings.exceliburLastExportFolder.isEmpty()) {
            String userDir = System.getProperty("user.home");
            saveManager.userSettings.exceliburLastExportFolder = userDir + "/Desktop";
        }
        ExportExcelDialog dialog = new ExportExcelDialog();
        int choice = dialog.showDialog(this, translationMgr.csb);
        enableUserInput(false);
        if (choice == JFileChooser.APPROVE_OPTION) {
            new Thread(() -> {
                String outputFolder = dialog.getSelectedFile().toString();
                saveManager.userSettings.exceliburLastExportFolder = dialog.getSelectedFile().getParent();
                int i = outputFolder.lastIndexOf(System.getProperty("file.separator"));
                String fileName = outputFolder.substring(i + 1, outputFolder.length());
                outputFolder = outputFolder.substring(0, i);
                FileWriter writer = new FileWriter();
                boolean success = writer.export(FileWriterOptions.NEW_EXCEL, translationMgr.writerConfig, translationMgr.csb, outputFolder, fileName);
                if (success) {
                    owner.setStatus("Sucessfully exported Excel", App.NORMAL_MESSAGE);
                }
                enableUserInput(true);
            }).start();
        } else {
            owner.setStatus("Aborted export...", App.NORMAL_MESSAGE);
            enableUserInput(true);
        }
    }

    void openSelectExcelSheetDialog(File file) {
        MergeExcelDialog dialog = new MergeExcelDialog(this);
        int selection = dialog.showDialog(file, translationMgrDifference.csb);
        translationMgr.writerConfig = dialog.writerConfig;

        if (selection == 0) {

            //-------start
            owner.setStatus("Selecting output folder...", App.NORMAL_MESSAGE);

            if (translationMgr.writerConfig.bSaveAsNewFile) {
                String suggestion = StringHelper.getFileName(file.getAbsolutePath(), false) + "_merged";
                String newPath = saveManager.userSettings.exceliburLastImportFolder + System.getProperty("file.separator") + suggestion;

                JFileChooser chooser = new JFileChooser(newPath);

                chooser.setSelectedFile(new File(suggestion));
                chooser.setPreferredSize(new Dimension(800, 600));
                // This sets the default folder view to 'details'
                Action details = chooser.getActionMap().get("viewTypeDetails");
                details.actionPerformed(null);
                int choice = chooser.showSaveDialog(this);
                if (choice != JFileChooser.APPROVE_OPTION) {
                    return;
                }


                translationMgr.writerConfig.exportAbsolutePath = chooser.getSelectedFile().toString();
                if (!StringHelper.isValid(translationMgr.writerConfig.exportAbsolutePath)) {
                    translationMgr.writerConfig.exportAbsolutePath = "translations";
                }
                if (!StringHelper.getFileExtension(translationMgr.writerConfig.exportAbsolutePath).equals(".xlsx")) {
                    translationMgr.writerConfig.exportAbsolutePath += ".xlsx";
                }
            } else {
                translationMgr.writerConfig.exportAbsolutePath = file.getAbsolutePath();
            }
            //-------end

            ExcelWriter writer = new ExcelWriter();
            writer.updateExcelSheet(createReaderConfig(), dialog.writerConfig, translationMgrDifference.csb, file, dialog.sheetName);
        } else if (selection == 1) {

        }
    }

    private void renameTab() {
        String tabName = "";
        if (translationMgr.csb != null) {
            switch (translationMgr.csb.brands.size()) {
                case 0: {
                    tabName = "Excelibur";
                    break;
                }
                case 1: {
                    tabName = translationMgr.csb.brands.get(0).name;
                    break;
                }
                default: {
                    int i = 0;
                    while (i < translationMgr.csb.brands.size() && tabName.length() < 20) {
                        tabName += translationMgr.csb.brands.get(i).name + ", ";
                        ++i;
                    }
                    if (i < translationMgr.csb.brands.size()) {
                        tabName += "...";
                    }
                    break;
                }
            }
        }
        tab.rename(tabName);
    }

    private void openExportJsonDialog() {
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
}
