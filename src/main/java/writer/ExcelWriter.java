package writer;

import core.App;
import core.StringHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import translations.*;
import widgets.table.LanguageIdentifier;
import widgets.table.LanguageTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.poi.ss.usermodel.Font.COLOR_RED;

public class ExcelWriter {
    public WriterConfig config = new WriterConfig();
    private static final int COL_OFFSET_COMPONENT = 0;
    private static final int COL_OFFSET_KEY = 1;
    private static final int COL_OFFSET_LANG = 2;
    private static final int ROW_OFFSET_TRANSLATIONS = 4;
    private static final int ROW_OFFSET_META = 2;

    public boolean export(I18nCSB csb, String outputFolder, String fileName, boolean bSkipEmptyCells) {

        Workbook workbook = new XSSFWorkbook();

        Font redFont = workbook.createFont();
        redFont.setColor(COLOR_RED);
        XSSFCellStyle redStyle = (XSSFCellStyle) workbook.createCellStyle();
        redStyle.setFont(redFont);

        XSSFCellStyle overrideStyle = (XSSFCellStyle) workbook.createCellStyle();
        XSSFCellStyle newStyle = (XSSFCellStyle) workbook.createCellStyle();
        overrideStyle.setFillBackgroundColor(config.overrideColor);
        overrideStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        newStyle.setFillBackgroundColor(config.newColor);
        newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (I18nBrand brand : csb.brands) {
            Sheet sheet = workbook.createSheet(brand.name);

            int columnOfLocale = COL_OFFSET_LANG;
            boolean bBlankSheet = true;
            for (I18nLanguage lang : brand.languages) {
                if (bBlankSheet) {
                    Row row = sheet.createRow(0);
                    Cell cell = row.createCell(0);
                    cell.setCellValue(brand.name);
                    cell.setCellStyle(redStyle);
                }

                {
                    Row row;
                    if (bBlankSheet) {
                        row = sheet.createRow(ROW_OFFSET_META);
                    } else {
                        row = sheet.getRow(ROW_OFFSET_META);
                    }
                    Cell cell = row.createCell(0);
                    cell.setCellValue("component");
                    cell.setCellStyle(redStyle);

                    cell = row.createCell(1);
                    cell.setCellValue("key");
                    cell.setCellStyle(redStyle);

                    cell = row.createCell(columnOfLocale);
                    cell.setCellValue(lang.locale);
                    cell.setCellStyle(redStyle);
                }

                int rowIndex = ROW_OFFSET_TRANSLATIONS;
                {
                    for (I18n i18n : lang.translations) {
                        if (i18n.component.equals(I18nLanguage.META_STRING)) continue;

                        if (i18n.isJSON()) {
                            for (I18nData json : i18n.json) {
                                Row row;
                                if (bBlankSheet) {
                                    row = sheet.createRow(rowIndex);
                                } else {
                                    row = sheet.getRow(rowIndex);
                                }
                                if (bBlankSheet) {
                                    Cell cell = row.createCell(COL_OFFSET_COMPONENT);
                                    cell.setCellValue(i18n.component);
                                    cell.setCellStyle(redStyle);

                                    cell = row.createCell(COL_OFFSET_KEY);
                                    cell.setCellValue(i18n.key + "." + json.key);
                                    cell.setCellStyle(redStyle);
                                }
                                Cell cell = row.createCell(columnOfLocale);
                                cell.setCellValue(json.value);
                                cell.setCellStyle(redStyle);
                                ++rowIndex;
                            }
                        } else {
                            Row row;
                            if (bBlankSheet) {
                                row = sheet.createRow(rowIndex);
                            } else {
                                row = sheet.getRow(rowIndex);
                            }
                            if (bBlankSheet) {
                                Cell cell = row.createCell(COL_OFFSET_COMPONENT);
                                cell.setCellValue(i18n.component);
                                cell.setCellStyle(redStyle);

                                cell = row.createCell(COL_OFFSET_KEY);
                                cell.setCellValue(i18n.key);
                                cell.setCellStyle(redStyle);
                            }
                            Cell cell = row.createCell(columnOfLocale);
                            cell.setCellValue(i18n.json.get(0).value);
                            cell.setCellStyle(redStyle);
                            ++rowIndex;
                        }
                    }
                }
                bBlankSheet = false;
                ++columnOfLocale;
            }
        }

        try {
            String path = outputFolder + System.getProperty("file.separator") + StringHelper.getFileName(fileName) + ".xlsx";

            File file = new File(path);
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            return true;

        } catch (Exception e) {
            App app = App.get();
            app.setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
            return false;
        }
    }


    public boolean export(I18nCSB csb, File excelFile, String sheetName) {
        if (excelFile == null || !Files.isWritable(excelFile.toPath())) return false;

        FileInputStream fis;
        try {
            // don't know why but FIS proved to be 2-3 times faster than directly using file or OPC package.
            fis = new FileInputStream(excelFile);
            Workbook workbook = new XSSFWorkbook(fis);

            if (csb.brands.size() > 1) {
                for (I18nBrand brand : csb.brands) {
                    String brandSheetName = brand.name + sheetName;
                    Sheet sheet = workbook.getSheet(brandSheetName);
                    if (sheet != null) {
                        boolean bFoundFreeeSheetName = false;
                        int nameIdx = 0;
                        int limit = 100;

                        while (!bFoundFreeeSheetName && nameIdx < limit) {
                            sheet = workbook.getSheet(brandSheetName + "(" + nameIdx + ")");
                            bFoundFreeeSheetName = sheet != null;
                        }
                    }
                }
            } else {

            }
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);

            }

            workbook.close();
        } catch (Exception e) {
            App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
        }

        return true;
    }

    private String createOutputFolder(String outputFolder, String brand, String locale) {
        String fileSep = System.getProperty("file.separator");
        String path = outputFolder + fileSep;
        path += locale + "_" + brand + fileSep;
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
