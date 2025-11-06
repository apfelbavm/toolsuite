package writer;


import translations.I18nCSB;

public class FileWriter {
    public boolean export(FileWriterOptions options, WriterConfig config, I18nCSB csb, String outputFolder, String fileName) {
        switch (options) {
            case JSON: {
                break;
            }
            case NEW_EXCEL: {
                ExcelWriter writer = new ExcelWriter();
                return writer.writeNewExcelFiles(config, csb, outputFolder, fileName, false);
            }
            case FILL_EXCEL: {
                break;
            }
        }
        return false;
    }
}
