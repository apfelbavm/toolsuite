package writer;

import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class WriterConfig {
    public boolean bColorizeChangedCells = false;
    private java.awt.Color overrideCellFillColor = new java.awt.Color(0, 0, 0, 0);
    private java.awt.Color newCellFillColor = new java.awt.Color(0, 0, 0, 0);
    public boolean bSplitLargeComponentsIntoSheets = false;
    public int SplitLimit = 20;

    public void setOverrideCellFillColor(java.awt.Color newColor) {
        overrideCellFillColor = newColor;
    }

    public void setNewCellFillColor(java.awt.Color newColor) {
        newCellFillColor = newColor;
    }

    public Color getOverrideCellFillColor(XSSFWorkbook workbook) {
        IndexedColorMap colors = workbook.getStylesSource().getIndexedColors();
        return new XSSFColor(overrideCellFillColor, colors);
    }

    public Color getNewCellFillColor(XSSFWorkbook workbook) {
        IndexedColorMap colors = workbook.getStylesSource().getIndexedColors();
        return new XSSFColor(newCellFillColor, colors);
    }
}
