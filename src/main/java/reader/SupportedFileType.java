package reader;

public class SupportedFileType {
    public String description;
    public String extension;

    public static final SupportedFileType[] SUPPORTED_FILE_TYPES = new SupportedFileType[]
            {
                    new SupportedFileType("Microsoft Excel Documents (*.xlsx)", "xlsx"),
                    new SupportedFileType("JavaScript Object Notation (*.json)", "json"),
            };

    SupportedFileType(String description_, String extension_) {
        description = description_;
        extension = extension_;
    }

    public static boolean isSupported(String extension) {
        for (SupportedFileType type : SUPPORTED_FILE_TYPES) {
            if (type.extension.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public static SupportedFileType get(String extension) {
        for (SupportedFileType type : SUPPORTED_FILE_TYPES) {
            if (type.extension.equals(extension)) {
                return type;
            }
        }
        return null;
    }
}
