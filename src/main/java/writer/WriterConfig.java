package writer;

import core.TranslationMgrFlags;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class WriterConfig {
    public boolean bColorizeOverridden = false;
    public boolean bColorizeNew = false;
    public boolean bSaveAsNewFile = false;
    public String exportAbsolutePath = null;
    private java.awt.Color overrideCellFillColor = new java.awt.Color(0, 0, 0, 0);
    private java.awt.Color newCellFillColor = new java.awt.Color(0, 0, 0, 0);
    public boolean CONCAT_COMPONENT_AND_KEY;
    public boolean DONT_EXPORT_EMPTY_VALUES;
    public TranslationMgrFlags.FolderNaming folderNamingType;

    public void setOverrideCellFillColor(java.awt.Color newColor) {
        overrideCellFillColor = newColor;
    }

    public void setNewCellFillColor(java.awt.Color newColor) {
        newCellFillColor = newColor;
    }

    public org.apache.poi.xssf.usermodel.XSSFColor getOverrideCellFillColor(XSSFWorkbook workbook) {
        IndexedColorMap colors = workbook.getStylesSource().getIndexedColors();
        return new XSSFColor(overrideCellFillColor, colors);
    }

    public org.apache.poi.xssf.usermodel.XSSFColor getNewCellFillColor(XSSFWorkbook workbook) {
        IndexedColorMap colors = workbook.getStylesSource().getIndexedColors();
        return new XSSFColor(newCellFillColor, colors);
    }
}
