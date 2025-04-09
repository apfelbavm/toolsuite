package widgets.dialogs;

import reader.ExcelReader;
import translations.I18nCSB;
import widgets.FColor;
import widgets.UIConstants;
import widgets.table.GroupableTable;
import widgets.table.LanguageTable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class MergeExcelDialog {
    public boolean bNewExcelSheet;
    public String sheetName;

    JComponent parentComponent;
    JRadioButton newSheet = new JRadioButton("Add new sheet...");
    JRadioButton existingSheet = new JRadioButton("Merge into existing sheet...");
    JTextField newSheetNameInput = new JTextField("Tabelle 1", 10);
    JComboBox existingSheetOptions;
    JLabel errorLabel = new JLabel("This sheet already exists!");
    ArrayList<String> sheetNames;
    JPanel panel = new JPanel();
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    JCheckBox checkBoxAutoResize = new JCheckBox("Auto Resize Table");

    public MergeExcelDialog(JComponent parentComponent_) {
        parentComponent = parentComponent_;

        layout.setConstraints(panel, constraints);
        panel.setLayout(layout);
    }

    public int showDialog(File file, I18nCSB csb) {
        if (file == null) return -1;
        sheetNames = ExcelReader.getExcelSheetNames(file);

        LanguageTable languageTable = csb.createLanguageTable();

        GroupableTable table = new GroupableTable();
        table.updateTable(languageTable);
        table.updateTableAutoResizing(true);

        checkBoxAutoResize.setSelected(true);
        checkBoxAutoResize.addItemListener(e -> table.updateTableAutoResizing(checkBoxAutoResize.isSelected()));

        {
            existingSheetOptions = new JComboBox(sheetNames.toArray());

            newSheet.setSelected(true);
            existingSheet.setSelected(false);
            bNewExcelSheet = true;

            newSheetNameInput.setEnabled(true);
            existingSheetOptions.setEnabled(false);

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

            validate();

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
            addToGrid(checkBoxAutoResize, 3, 0);
            addToGrid(table, 4, 0, 1, 2, true);

        }

        Dimension defaultDimension = (Dimension) UIManager.get("OptionPane.minimumSize");
        UIManager.put("OptionPane.minimumSize", new Dimension(800, 600));
        int selection = JOptionPane.showConfirmDialog(parentComponent, panel, "Merge into Excel file...", JOptionPane.PLAIN_MESSAGE);
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
    }
}
