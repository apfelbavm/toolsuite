package widgets;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class ExceliburCellRenderer implements TableCellRenderer
{

	private static final TableCellRenderer RENDERER = new DefaultTableCellRenderer();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		final Component c = RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Object obj = table.getValueAt(row, column);
		if (obj == null)
		{
			c.setBackground(isSelected ? new Color(255, 192, 192) : new Color(255, 110, 110));
		}
		else
		{
			String content = table.getValueAt(row, column).toString();
			if (content.isBlank() || content.isEmpty())
			{
				c.setBackground(isSelected ? new Color(255, 192, 192) : new Color(255, 110, 110));
			}
			else
			{
				if (column < 2)
				{
					c.setBackground(isSelected ? new Color(57, 135, 213) : new Color(255, 255, 255, 32));
					c.setForeground(isSelected ? new Color(255, 255, 255) : new Color(187, 187, 187, 128));
				}
				else
				{
					c.setBackground(isSelected ? new Color(57, 135, 213) : new Color(0, 0, 0, 0));
					c.setForeground(isSelected ? new Color(255, 255, 255) : new Color(187, 187, 187));
				}
			}
		}

		return c;
	}
}
