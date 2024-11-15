package writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import core.App;
import core.TranslationMgrFlags;
import translations.I18n;
import translations.I18nBrand;
import translations.I18nLanguage;
import translations.I18nCSB;

public class JsonWriter {
    private TranslationMgrFlags.FolderNaming folderNamingType;

    public boolean export2Json(I18nCSB csb, String outputFolder, String fileName, boolean bMergeComponentAndKey,
                               TranslationMgrFlags.FolderNaming inFolderNamingType) {
        folderNamingType = inFolderNamingType;
        // we have only one line off error message, thus we just have to return wether
        // there was an error, the error is already printed and shouldnt be overriden by
        // the success message if succeeding exports were successfull.
        for (I18nBrand brand : csb.brands) {
            for (I18nLanguage lang : brand.languages) {
                if (bMergeComponentAndKey) {
                    //if (!exportSimple(lang, brand.name, outputFolder, fileName, bSkipEmptyCells)) return false; @todo not finished copy logic from "advanced export"
                } else {
                    if (!exportAdvanced(lang, brand.name, outputFolder, fileName)) return false;
                }
            }
        }
        return true;
    }

    private boolean exportAdvanced(I18nLanguage lang, String brand, String outputFolder, String fileName) {
        try {
            String pathToCreate = createOutputFolder(outputFolder, brand, lang.locale);
            if (pathToCreate == null) {
                return false;
            }
            // We need this filewriter to allow Umlauts
            Writer writer = new OutputStreamWriter(new FileOutputStream(pathToCreate + fileName + ".json"), StandardCharsets.UTF_8);
            String lastComponent = "";
            boolean isFirstComp = true;
            writer.write("{\n");
            for (I18n i18n : lang.translations) {
                if (!i18n.isValid()) continue;
                boolean isFirstKeyValue = false;
                if (!i18n.component.equals(lastComponent)) {
                    // New component
                    if (isFirstComp) {
                        isFirstKeyValue = true;
                        isFirstComp = false;
                    } else {
                        writer.write("\n    },\n");
                    }

                    if (i18n.bIsJSON) {
                        writer.write("    \"" + i18n.component + "\": {\n");
                        writer.write("        \"" + i18n.key + "\": {\n");
                        TreeMap<String, String> tree = i18n.getJSONSorted();
                        int numJSONEntries = tree.size();
                        int i = 0;
                        for (Map.Entry<String, String> entry : tree.entrySet()) {
                            writer.write("            \"" + entry.getKey() + "\": " + "\"" + entry.getValue() + "\"");
                            if (i < numJSONEntries - 1) {
                                writer.write(",\n");
                            } else {
                                writer.write("\n");
                            }
                            ++i;
                        }
                        writer.write("        }");
                    } else {
                        writer.write("    \"" + i18n.component + "\": {\n        \"" + i18n.key + "\": " + "\"" + i18n.value + "\"");
                        //writer.write("    }");
                    }
                    lastComponent = i18n.component;
                } else {
                    if (!isFirstKeyValue) {
                        writer.write(",\n");
                    }
                    if (i18n.bIsJSON) {
                        writer.write("        \"" + i18n.key + "\": {\n");
                        TreeMap<String, String> tree = i18n.getJSONSorted();
                        int numJSONEntries = tree.size();
                        int i = 0;
                        for (Map.Entry<String, String> entry : tree.entrySet()) {
                            writer.write("            \"" + entry.getKey() + "\": " + "\"" + entry.getValue() + "\"");
                            if (i < numJSONEntries - 1) {
                                writer.write(",\n");
                            } else {
                                writer.write("\n");
                            }
                            ++i;
                        }
                        writer.write("        }");
                    } else {
                        writer.write("        \"" + i18n.key + "\": " + "\"" + i18n.value + "\"");
                    }
                }
            }
            writer.write("\n    }\n}\n");
            writer.close();
        } catch (Exception e) {
            App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean exportSimple(I18nLanguage lang, String brand, String outputFolder, String fileName, boolean skipEmptyCells) {
        try {
            String pathToCreate = createOutputFolder(outputFolder, brand, lang.locale);
            if (pathToCreate == null) {
                return false;
            }
            // We need this filewriter to allow Umlauts
            Writer writer = new OutputStreamWriter(new FileOutputStream(pathToCreate + fileName + ".json"), StandardCharsets.UTF_8);
            writer.write("{");
            boolean isFirstItem = false;
            for (I18n i18n : lang.translations) {
                if (i18n.value.isBlank() || i18n.value.isEmpty()) continue;
                if (!isFirstItem) {
                    isFirstItem = true;
                } else {
                    writer.write(",");
                }
                writer.write("\n    \"" + i18n.component + "_" + i18n.key + "\": " + "\"" + i18n.value + "\"");
            }
            writer.write("\n}\n");
            writer.close();
        } catch (Exception e) {
            App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private String createOutputFolder(String outputFolder, String brand, String locale) {
        String fileSep = System.getProperty("file.separator");
        String path = outputFolder + fileSep;
        switch (folderNamingType) {
            case BRAND_AND_LOCALE_AS_SUBFOLDER:
                path += brand + fileSep + locale + fileSep;
                break;
            case BRAND_LOCALE:
                path += brand + "_" + locale + fileSep;
                break;
            case LOCALE_BRAND:
                path += locale + "_" + brand + fileSep;
                break;
        }
        if (!createFolder(path)) return null;

        return path;
    }

    private boolean createFolder(String path) {
        Path pathObj = Paths.get(path);
        return createFolder(pathObj);
    }

    private boolean createFolder(Path path) {
        if (Files.exists(path)) return true;
        try {
            // Create directory doesnt with with sub directories when the parent older is
            // not created yet
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
            return false;
        }
    }
}
