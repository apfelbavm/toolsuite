package writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import core.App;
import core.StringHelper;
import core.TranslationMgrFlags;
import translations.*;

public class JsonWriter {
    private TranslationMgrFlags.FolderNaming folderNamingType;

    public boolean export2Json(I18nCSB csb, String outputFolder, String fileName, boolean bMergeComponentAndKey, boolean bSkipEmptyCells,
                               TranslationMgrFlags.FolderNaming inFolderNamingType) {
        folderNamingType = inFolderNamingType;
        // we have only one line off error message, thus we just have to return wether
        // there was an error, the error is already printed and shouldnt be overriden by
        // the success message if succeeding exports were successfull.

        for (I18nBrand brand : csb.brands) {
            for (I18nLanguage lang : brand.languages) {
                if (bMergeComponentAndKey) {
                    if (!exportSimple(lang, brand.name, outputFolder, fileName, bSkipEmptyCells)) return false;
                } else {
                    if (!exportAdvanced(lang, brand.name, outputFolder, fileName, bSkipEmptyCells)) return false;
                }
            }
        }
        return true;
    }

    private static final String INDENTATION = (" ").repeat(4);

    private boolean exportAdvanced(I18nLanguage lang, String brand, String outputFolder, String fileName, boolean bSkipEmptyCells) {
        try {
            System.out.println("JsonWriter export-> bSkipEmptyCells: " + bSkipEmptyCells);
            String pathToCreate = createOutputFolder(outputFolder, brand, lang.locale);
            if (pathToCreate == null) {
                return false;
            }
            // We need this filewriter to allow Umlauts
            Writer writer = new OutputStreamWriter(new FileOutputStream(pathToCreate + fileName + ".json"), StandardCharsets.UTF_8);
            String lastComponent = "";
            boolean bIsFirstComp = true;
            writer.write("{\n");
            for (I18n i18n : lang.translations) {
                if (bSkipEmptyCells && !i18n.isValid(bSkipEmptyCells)) continue;

                if (!i18n.component.equals(lastComponent)) {
                    // New component
                    if (bIsFirstComp) {
                        bIsFirstComp = false;
                    } else {
                        writer.write("\n" + INDENTATION + "},\n");
                    }
                    writeJsonShenanigans(writer, i18n, false, bSkipEmptyCells);
                    lastComponent = i18n.component;
                } else {
                    writer.write(",\n");
                    writeJsonShenanigans(writer, i18n, true, bSkipEmptyCells);
                }
            }
            writer.write("\n" + INDENTATION + "}\n}\n");
            writer.close();
        } catch (Exception e) {
            App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean exportSimple(I18nLanguage lang, String brand, String outputFolder, String fileName, boolean bSkipEmptyCells) {
        try {
            System.out.println("JsonWriter export-> bSkipEmptyCells: " + bSkipEmptyCells);
            String pathToCreate = createOutputFolder(outputFolder, brand, lang.locale);
            if (pathToCreate == null) {
                return false;
            }
            // We need this filewriter to allow Umlauts
            Writer writer = new OutputStreamWriter(new FileOutputStream(pathToCreate + fileName + ".json"), StandardCharsets.UTF_8);

            boolean bIsFirst = true;
            writer.write("{");
            for (I18n i18n : lang.translations) {
                for (I18nData data : i18n.json) {
                    if (bSkipEmptyCells && !StringHelper.isValid(data.value)) continue;
                    if (!bIsFirst) {
                        writer.write(",");
                    }
                    if (StringHelper.isValid(data.key)) {
                        writer.write("\n" + INDENTATION + "\"" + i18n.component + "." + i18n.key + "." + data.key + "\": \"" + data.value + "\"");
                    } else {
                        writer.write("\n" + INDENTATION + "\"" + i18n.component + "." + i18n.key + "\": \"" + data.value + "\"");
                    }
                    bIsFirst = false;
                }
            }
            writer.write("\n}\n");
            writer.close();
        } catch (Exception e) {
            App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void writeJsonShenanigans(Writer writer, I18n i18n, boolean bIsSameComponent, boolean bSkipEmptyCells) throws IOException {
        if (i18n.isJSON()) {
            ArrayList<I18nData> json = i18n.getJSONSorted(bSkipEmptyCells);
            if (!json.isEmpty()) {
                if (!bIsSameComponent) {
                    writer.write(INDENTATION + "\"" + i18n.component + "\": {\n");
                }
                writer.write(INDENTATION + INDENTATION + "\"" + i18n.key + "\": {\n");

                ArrayList<I18nData> validData = new ArrayList<I18nData>();
                for (I18nData data : json) {
                    if (bSkipEmptyCells && !StringHelper.isValid(data.value)) continue;
                    validData.add(data);
                }

                for (int i = 0; i < validData.size(); ++i) {
                    I18nData data = validData.get(i);
                    writer.write(INDENTATION + INDENTATION + INDENTATION + "\"" + data.key + "\": \"" + data.value + "\"");
                    String lineEnding = (i < validData.size() - 1) ? ",\n" : "\n";
                    writer.write(lineEnding);
                }
                writer.write(INDENTATION + INDENTATION + "}");
            }
        } else {
            if (!bIsSameComponent) {
                writer.write(INDENTATION + "\"" + i18n.component + "\": {\n");
            }
            writer.write(INDENTATION + INDENTATION + "\"" + i18n.key + "\": \"" + i18n.json.get(0).value + "\"");
        }
    }

    private String createOutputFolder(String outputFolder, String brand, String locale) {
        String fileSep = System.getProperty("file.separator");
        String path = outputFolder + fileSep;
        switch (folderNamingType) {
            case BRAND_AND_LOCALE_AS_SUBFOLDER:
                path += brand + fileSep + locale + fileSep;
                break;
            case LOCALE_AND_BRAND_AS_SUBFOLDER:
                path += locale + fileSep + brand + fileSep;
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
