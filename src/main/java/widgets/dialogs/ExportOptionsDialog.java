package widgets.dialogs;

import core.App;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ExportOptionsDialog {
    JOptionPane optionPane = new JOptionPane();

    public ExportChoice showDialog(Component parentComponent) {
        JButton JsonButton = App.createButtonWithTextAndIcon("Export to Json", "icon_json.png");
        JButton NewExcelButton = App.createButtonWithTextAndIcon("Export to new Excel File", "icon_excel.png");
        JButton MergeExcelButton = App.createButtonWithTextAndIcon("Merge into Excel File", "icon_excel_merge.png");

        JsonButton.setPreferredSize(new Dimension(250, 50));
        NewExcelButton.setPreferredSize(new Dimension(250, 50));
        MergeExcelButton.setPreferredSize(new Dimension(250, 50));

        JsonButton.addActionListener(e -> {
            optionPane.setValue(ExportChoice.EXPORT_JSON.ordinal());
        });

        NewExcelButton.addActionListener(e -> {
            optionPane.setValue(ExportChoice.EXPORT_EXCEL.ordinal());
        });

        MergeExcelButton.addActionListener(e -> {
            optionPane.setValue(ExportChoice.MERGE_EXCEL.ordinal());
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 12, 4));
        buttonPanel.add(JsonButton);
        buttonPanel.add(NewExcelButton);
        buttonPanel.add(MergeExcelButton);

        buttonPanel.setBorder(new EmptyBorder(8, 92, 8, 92));

        optionPane.setOptionType(JOptionPane.DEFAULT_OPTION);
        optionPane.setMessage(buttonPanel);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            optionPane.setValue(ExportChoice.ABORT.ordinal());
        });
        optionPane.setOptions(new JButton[]{cancelButton});

        JDialog dialog = optionPane.createDialog(parentComponent, "Export options");
        dialog.setVisible(true);
        Object result = optionPane.getValue();

        int selection = -1;
        if (result instanceof Integer) {
            selection = (int) result;
        }

        if (selection == -1) {
            return ExportChoice.ABORT;
        }
        return ExportChoice.values()[selection];

    }
}
