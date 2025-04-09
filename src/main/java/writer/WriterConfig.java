package writer;


import org.apache.poi.ss.usermodel.Color;

public class WriterConfig {
    public boolean bColorizeChangedCells = false;
    public Color overrideColor;
    public Color newColor;
    public boolean bSplitLargeComponentsIntoSheets = false;
    public int SplitLimit = 20;
}
