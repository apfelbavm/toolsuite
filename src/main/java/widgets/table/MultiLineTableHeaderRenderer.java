package widgets.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.table.TableCellRenderer;

public class MultiLineTableHeaderRenderer extends JTextArea implements TableCellRenderer
{
	private static final long serialVersionUID = 1L;

public MultiLineTableHeaderRenderer() {
    setEditable(false);
    setLineWrap(true);
    setOpaque(false);
    setFocusable(false);
    setWrapStyleWord(true);
    LookAndFeel.installBorder(this, "TableHeader.cellBorder");
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    int width = table.getColumnModel().getColumn(column).getWidth();
    setText((String)value);
    setSize(width, getPreferredSize().height);
    return this;
  }
}
