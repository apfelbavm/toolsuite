package widgets.table;

import widgets.FColor;
import widgets.UIConstants;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class LanguageCellRenderer implements TableCellRenderer
{

	private static final TableCellRenderer RENDERER = new DefaultTableCellRenderer();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		final Component c = RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Object obj = table.getValueAt(row, column);
		if (obj == null)
		{
			c.setBackground(isSelected ? UIConstants.YourPink : UIConstants.BitterSweet);
		}
		else
		{
			String content = table.getValueAt(row, column).toString();
			if (content.isBlank() || content.isEmpty())
			{
				c.setBackground(isSelected ? UIConstants.YourPink : UIConstants.BitterSweet);
			}
			else
			{
				if (column < 2)
				{
					c.setBackground(isSelected ? UIConstants.CuriousBlue : new FColor(UIConstants.White, 32));
					c.setForeground(isSelected ? UIConstants.White : new FColor(UIConstants.Silver, 128));
				}
				else
				{
					c.setBackground(isSelected ? UIConstants.CuriousBlue : UIConstants.Transparent);
					c.setForeground(isSelected ? UIConstants.White : UIConstants.Silver);
				}
			}
		}
		return c;
	}
}
