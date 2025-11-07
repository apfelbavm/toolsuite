package widgets.dialogs;

import reader.ExcelReader;
import translations.I18nCSB;
import widgets.FColor;
import widgets.UIConstants;
import widgets.table.GroupableTable;
import widgets.table.LanguageTable;
import writer.WriterConfig;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class MergeExcelDialog {
    static final Color DEFAULT_OVERRIDE_FILL_COLOR = new Color(255, 0, 255);
    static final Color DEFAULT_NEW_FILL_COLOR = new Color(0, 255, 0);
    public boolean bNewExcelSheet;
    public String sheetName;
    public WriterConfig writerConfig = new WriterConfig();

    JComponent parentComponent;
    JRadioButton newSheet = new JRadioButton("Add new sheet...");
    JRadioButton existingSheet = new JRadioButton("Merge into existing sheet...");
    JTextField newSheetNameInput = new JTextField("Tabelle 1", 10);
    JComboBox existingSheetOptions;
    JLabel errorLabel = new JLabel("This sheet already exists!");

    JButton openOverrideColorPickerButton = new JButton("Choose Color");
    JCheckBox useOverrideFillColor = new JCheckBox("Use override cell Fillcolor");

    JButton openNewColorPickerButton = new JButton("Choose Color");
    JCheckBox useNewFillColor = new JCheckBox("Use new cell Fillcolor");
    JCheckBox saveAsNewFile = new JCheckBox("Save as new file");
    ArrayList<String> sheetNames;
    JPanel panel = new JPanel();
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    JOptionPane optionPane;
    JButton OKButton;

    public MergeExcelDialog(JComponent parentComponent_) {
        parentComponent = parentComponent_;

        layout.setConstraints(panel, constraints);
        panel.setLayout(layout);
    }

    public int showDialog(File file, I18nCSB csb) {
        if (file == null) return -1;
        sheetNames = ExcelReader.getExcelSheetNames(file);

        LanguageTable languageTable = csb.createLanguageTable(null);

        GroupableTable table = new GroupableTable();
        table.updateTable(languageTable);

        writerConfig = new WriterConfig();
        writerConfig.setOverrideCellFillColor(DEFAULT_OVERRIDE_FILL_COLOR);
        writerConfig.setNewCellFillColor(DEFAULT_NEW_FILL_COLOR);

        {
            openOverrideColorPickerButton.setBackground(DEFAULT_OVERRIDE_FILL_COLOR);
            useOverrideFillColor.addActionListener(e -> {
                openOverrideColorPickerButton.setEnabled(useOverrideFillColor.isSelected());
                writerConfig.bColorizeOverridden = useOverrideFillColor.isSelected();
            });
            useOverrideFillColor.setSelected(true);
            writerConfig.bColorizeOverridden = useOverrideFillColor.isSelected();

            openNewColorPickerButton.setBackground(DEFAULT_NEW_FILL_COLOR);
            useNewFillColor.addActionListener(e -> {
                openNewColorPickerButton.setEnabled(useNewFillColor.isSelected());
                writerConfig.bColorizeNew = useNewFillColor.isSelected();
            });
            useNewFillColor.setSelected(true);
            writerConfig.bColorizeNew = useNewFillColor.isSelected();

            existingSheetOptions = new JComboBox(sheetNames.toArray());

            newSheet.addActionListener(e -> {
                existingSheet.setSelected(false);
                newSheet.setSelected(true);
                existingSheetOptions.setEnabled(false);
                newSheetNameInput.setEnabled(true);
                errorLabel.setVisible(true);
            });

            existingSheet.addActionListener(e -> {
                existingSheet.setSelected(true);
                newSheet.setSelected(false);

                existingSheetOptions.setEnabled(true);
                newSheetNameInput.setEnabled(false);
                errorLabel.setVisible(false);
            });

            newSheet.setSelected(true);
            existingSheet.setSelected(false);
            bNewExcelSheet = true;

            newSheetNameInput.setEnabled(true);
            existingSheetOptions.setEnabled(false);

            saveAsNewFile.setSelected(false);
            saveAsNewFile.addActionListener(e -> {
                writerConfig.bSaveAsNewFile = saveAsNewFile.isSelected();
            });
            writerConfig.bSaveAsNewFile = saveAsNewFile.isSelected();

            openOverrideColorPickerButton.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(parentComponent, "Choose a cell override  color", DEFAULT_OVERRIDE_FILL_COLOR);
                if (newColor != null) {
                    writerConfig.setOverrideCellFillColor(newColor);
                    openOverrideColorPickerButton.setBackground(newColor);
                }
            });

            openNewColorPickerButton.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(parentComponent, "Choose a new cell color", DEFAULT_NEW_FILL_COLOR);
                if (newColor != null) {
                    writerConfig.setNewCellFillColor(newColor);
                    openNewColorPickerButton.setBackground(newColor);
                }
            });

            newSheetNameInput.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    validate();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    validate();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    validate();
                }
            });

            addToGrid(newSheet, 0, 0);
            addToGrid(newSheetNameInput, 0, 1);
            addToGrid(errorLabel, 1, 0, 1, 2, false);

            addToGrid(existingSheet, 2, 0);
            addToGrid(existingSheetOptions, 2, 1);

            addToGrid(useOverrideFillColor, 3, 0);
            addToGrid(openOverrideColorPickerButton, 3, 1);

            addToGrid(useNewFillColor, 4, 0);
            addToGrid(openNewColorPickerButton, 4, 1);

            addToGrid(saveAsNewFile, 5, 0);

            addToGrid(table, 6, 0, 1, 2, true);

            existingSheet.setEnabled(false);
        }

        Dimension defaultDimension = (Dimension) UIManager.get("OptionPane.minimumSize");
        UIManager.put("OptionPane.minimumSize", new Dimension(800, 600));

        OKButton = new JButton("OK");
        OKButton.addActionListener(e -> {
            optionPane.setValue(0);
        });
        JButton CancelButton = new JButton("Cancel");
        CancelButton.addActionListener(e -> {
            optionPane.setValue(1);
        });

        JButton[] arr = new JButton[]{OKButton, CancelButton};
        optionPane = new JOptionPane(panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null, arr, OKButton);

        validate();

        JDialog dialog = optionPane.createDialog(parentComponent, "Merge into Excel file...");
        dialog.setVisible(true);
        Object result = optionPane.getValue();
        int selection = -1;
        if (result instanceof Integer) {
            selection = (Integer) result;
        } else {
            System.out.println("OH NOOO");
        }
//        int selection = optionPane.showOptionDialog(parentComponent, panel, "Merge into Excel file...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, arr, OKButton);
        System.out.println("test");
        UIManager.put("OptionPane.minimumSize", defaultDimension);

        bNewExcelSheet = !existingSheet.isSelected();
        if (bNewExcelSheet) {
            sheetName = newSheetNameInput.getText();
        } else {
            sheetName = (String) existingSheetOptions.getSelectedItem();
        }
        return selection;
    }

    private static final Insets WEST_INSETS = new Insets(5, 0, 5, 5);
    private static final Insets EAST_INSETS = new Insets(5, 5, 5, 0);

    void addToGrid(JComponent comp, int row, int col) {
        constraints.gridx = col;
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = col == 0 ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        constraints.insets = (col == 0) ? WEST_INSETS : EAST_INSETS;
        panel.add(comp, constraints);
    }

    void addToGrid(JComponent comp, int row, int col, int rowSpan, int colSpan, boolean bFill) {
        constraints.gridx = col;
        constraints.gridy = row;
        constraints.gridwidth = colSpan;
        constraints.gridheight = rowSpan;
        constraints.weightx = 1.0;
        constraints.weighty = bFill ? 100.0f : 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = col == 0 ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        constraints.insets = (col == 0) ? WEST_INSETS : EAST_INSETS;
        panel.add(comp, constraints);
    }

    void validate() {
        boolean bInvalidName = sheetNames.contains(newSheetNameInput.getText());
        int alpha = bInvalidName ? 255 : 0;
        errorLabel.setForeground(new FColor(UIConstants.BitterSweet, alpha));
        OKButton.setEnabled(!bInvalidName);
    }
}
