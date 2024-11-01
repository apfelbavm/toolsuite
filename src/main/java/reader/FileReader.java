package reader;

import core.TranslationMgr;
import translations.I18nCSB;
import translations.I18nLanguage;

import java.io.File;

public class FileReader {
    private OnLocaleMissing localeListener = null;
    private OnBrandMissing brandListener = null;

    public void bindOnRequestLocale(OnLocaleMissing listener) {
        this.localeListener = listener;
    }

    public void bindOnRequestBrand(OnBrandMissing listener) {
        this.brandListener = listener;
    }

    public void removeOnRequestLocale(OnLocaleMissing listener) {
        if (this.localeListener == listener) {
            this.localeListener = null;
        }
    }

    public void removeOnRequestbrand(OnBrandMissing listener) {
        if (this.brandListener == listener) {
            this.brandListener = null;
        }
    }

    public String requestLocale(File file) {
        if (localeListener != null) {
            return localeListener.onLocaleMissing(file);
        }
        return null;
    }

    public String requestBrand(File file) {
        if (brandListener != null) {
            return brandListener.onBrandMissing(file);
        }
        return null;
    }

    public void read(File[] files) {
        I18nCSB brand = new I18nCSB();
        for (File file : files) {
            if (file == null) continue;
            String extension = getFileExtension(file);
            switch (extension) {
                case ".json":
                    System.out.println("reading json file");
                    JSONReader reader = new JSONReader();
                    I18nLanguage language = reader.read("", file);
                    if (!language.hasLocale()) {
                        String locale = determineLocale(file);
                        if (locale == null) {
                            locale = requestLocale(file);
                        }
                    }
                    String brandName = requestBrand(file);
                    if (brandName == null) {
                        brandName = "UNSET";
                    }
                    language.print();
                    if (language != null) {
                        brand.add("", language);
                    }

                    break;
                case ".xlsx":
                    break;

                default:
                    break;

            }
        }
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

    private String determineLocale(File file) {
        if (file != null) {
            String pathName = file.getAbsolutePath().toLowerCase();
            for (String code : TranslationMgr.ISO_CODES) {
                if (pathName.contains(code)) {
                    return code;
                }
            }
        }
        return null;
    }
}
