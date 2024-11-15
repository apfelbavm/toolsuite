package widgets.table;

import core.App;
import widgets.MainMenu;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class GroupableTableCtrl extends JPanel {
    JTable table;
    private JTextField searchInput = new JTextField();
    private JButton clearButton = new JButton("Clear");

    public GroupableTableCtrl() {
        setLayout(new BorderLayout());
        //add(jtfFilter, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Specify a word to match:"), BorderLayout.WEST);
        panel.add(searchInput, BorderLayout.CENTER);
        panel.add(clearButton, BorderLayout.EAST);
        clearButton.addActionListener(e -> clearSearch());
        add(panel, BorderLayout.NORTH);
    }

    public void createTable(LanguageTable langTable) {
        clearSearch();
        if (langTable == null) {
            App app = App.get();
            app.setStatus("No data found inside Excel file(s). Did you set it up properly? Click 'Help' to read the documentation on how to setup an Excel file correctly.", App.ERROR_MESSAGE);
            DefaultTableModel model = new DefaultTableModel(new String[]{"Component", "Key", "Locale"}, 0);
            table = new JTable(model);
            table.setFillsViewportHeight(true);
        } else {

            ArrayList<String> localeHeader = new ArrayList<String>();
            localeHeader.add("Component");
            localeHeader.add("Key");
            ArrayList<String> brandHeader = new ArrayList<String>();
            ArrayList<Integer> localeCount = new ArrayList<Integer>();
            String previousBrand = "";
            for (LanguageIdentifier identifier : langTable.getIdentifiers()) {
                if (!previousBrand.equals(identifier.brand)) {
                    brandHeader.add(identifier.brand);
                    localeCount.add(1);
                    previousBrand = identifier.brand;
                } else {
                    int i = localeCount.size() - 1;
                    int count = localeCount.get(i);
                    localeCount.set(i, ++count);
                }
                localeHeader.add(identifier.locale);
            }

            DefaultTableModel dm = new DefaultTableModel();
            dm.setDataVector(langTable.getJTableData(), localeHeader.toArray());

            table = new JTable(dm) {
                protected JTableHeader createDefaultTableHeader() {
                    return new GroupableTableHeader(columnModel);
                }
            };

            TableColumnModel cm = table.getColumnModel();
            GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
            int offset = 2;
            for (int i = 0; i < localeCount.size(); ++i) {
                ColumnGroup group = new ColumnGroup(brandHeader.get(i));
                for (int j = 0; j < localeCount.get(i); ++j) {
                    group.add(cm.getColumn(offset + j));
                }
                offset += localeCount.get(i);
                header.addColumnGroup(group);
            }

            LanguageCellRenderer cellRenderer = new LanguageCellRenderer();
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setReorderingAllowed(false);
            table.setDefaultRenderer(Object.class, cellRenderer);
            table.setFillsViewportHeight(true);
            table.setRowSelectionAllowed(true);
            table.setDropTarget(new DropTarget() {
                public synchronized void drop(DropTargetDropEvent evt) {
                    try {
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : droppedFiles) {
                            System.out.println("Dropped Files: " + file.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        System.out.println("Drag and Drop: " + e.getLocalizedMessage());
                    }
                }
            });
        }
        sortFilter();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void clearSearch()
    {
        searchInput.setText("");
    }
    public int getRowCount() {
        if (table != null) return table.getRowCount();
        return 0;
    }

    public void updateTableAutoResizing(boolean bAutoResize) {
        if (bAutoResize) {
            for (int column = 0; column < table.getColumnCount(); column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                int preferredWidth = tableColumn.getMinWidth();
                preferredWidth = 3000;
                tableColumn.setPreferredWidth(preferredWidth);
            }
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int column = 0; column < table.getColumnCount(); column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                int preferredWidth = tableColumn.getMinWidth();
                int maxWidth = Math.min(tableColumn.getMaxWidth(), 2000);

                for (int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                    Component c = table.prepareRenderer(cellRenderer, row, column);
                    int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                    preferredWidth = Math.max(preferredWidth, width);

                    // We've exceeded the maximum width, no need to check other rows

                    if (preferredWidth >= maxWidth) {
                        preferredWidth = maxWidth;
                        break;
                    }
                }
                tableColumn.setPreferredWidth(preferredWidth);
            }
        }
    }

    public void sortFilter() { // https://stackoverflow.com/questions/22066387/how-to-search-an-element-in-a-jtable-java
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(rowSorter);

        searchInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = searchInput.getText();

                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = searchInput.getText();

                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }
}
