package writer;

import reader.ReaderConfig;
import translations.I18nCSB;
import java.io.File;

public class FileWriter {
    public boolean export(FileWriterOptions options, WriterConfig config, I18nCSB csb, String outputFolder, String fileName) {

        switch (options) {
            case JSON: {
                I18nCSB csbCopy = new I18nCSB();
                csbCopy.as(csb);
                csbCopy.fillInEmpties(false);
                JsonWriter json = new JsonWriter();
                return json.export2Json(csbCopy, outputFolder, fileName, config.CONCAT_COMPONENT_AND_KEY, config.DONT_EXPORT_EMPTY_VALUES, config.folderNamingType);
            }
            case NEW_EXCEL: {
                ExcelWriter writer = new ExcelWriter();
                return writer.writeNewExcelFiles(config, csb, outputFolder, fileName, false);
            }
            case FILL_EXCEL: {
                System.out.println("Ooopsie");
                break;
            }
        }
        return false;
    }

    public boolean mergeExcelFile(ReaderConfig readerConfig, WriterConfig config, I18nCSB csb, File file, String sheetName) {
        ExcelWriter writer = new ExcelWriter();
        return writer.updateExcelSheet(readerConfig, config, csb, file, sheetName);
    }
}
