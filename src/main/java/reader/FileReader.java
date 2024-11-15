package reader;

import translations.I18nCSB;

import java.io.File;
import java.util.ArrayList;

public class FileReader {
    public JSONReader jsonReader = new JSONReader();
    public ExcelReader excelReader = new ExcelReader();

    public I18nCSB read(File[] files) {
        ArrayList<File> XLSXFiles = new ArrayList<>();
        ArrayList<File> JSONFiles = new ArrayList<>();

        for (File file : files) {
            if (file == null) continue;
            String extension = getFileExtension(file);
            switch (extension) {
                case ".json":
                    JSONFiles.add(file);
                    break;
                case ".xlsx":
                    XLSXFiles.add(file);
                    break;
                default:
                    break;

            }
        }

        I18nCSB csb = new I18nCSB();
        if (!XLSXFiles.isEmpty()) {
            csb.merge(excelReader.read(XLSXFiles));
        }
        if (!JSONFiles.isEmpty()) {
            csb.merge(jsonReader.read(JSONFiles));
        }
        return csb;
    }

    public static String getFileName(File file) {
        if (file == null) return null;

        String path = file.getName();
        int dot = path.lastIndexOf(".");
        int slash = path.lastIndexOf("\\");
        if (slash == -1) slash = path.lastIndexOf("/");
        if (dot > slash) return path.substring(slash > 0 ? slash + 1 : 0, dot);
        return path.substring(slash > 0 ? slash + 1 : 0);
    }

    public static String getFileExtension(File file) {
        if (file == null) return null;

        String name = file.getName();
        int dot = name.lastIndexOf(".");
        return name.substring(dot);

    }


}
