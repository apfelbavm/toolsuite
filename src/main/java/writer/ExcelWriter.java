package writer;

import core.App;
import core.StringHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import reader.ExcelReader;
import reader.ReaderConfig;
import test.QueryResult;
import translations.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.apache.poi.ss.usermodel.Font.COLOR_RED;

public class ExcelWriter {
    public WriterConfig config;
    private static final int COL_OFFSET_COMPONENT = 0;
    private static final int COL_OFFSET_KEY = 1;
    private static final int COL_OFFSET_LANG = 2;
    private static final int ROW_OFFSET_TRANSLATIONS = 4;
    private static final int ROW_OFFSET_META = 2;
    ExcelReader reader = new ExcelReader();
    XSSFCellStyle overrideStyle;
    XSSFCellStyle newStyle;
    XSSFCellStyle redStyle;

    void initStyles(XSSFWorkbook workbook) {
        overrideStyle = workbook.createCellStyle();
        overrideStyle.setFillForegroundColor(config.getOverrideCellFillColor(workbook));
        overrideStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        newStyle = workbook.createCellStyle();
        newStyle.setFillForegroundColor(config.getNewCellFillColor(workbook));
        newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font redFont = workbook.createFont();
        redFont.setColor(COLOR_RED);
        redStyle = workbook.createCellStyle();
        redStyle.setFont(redFont);
    }

    public boolean writeNewExcelFiles(WriterConfig writerConfig, I18nCSB csb, String outputFolder, String fileName, boolean bSkipEmptyCells) {

        config = writerConfig;
        XSSFWorkbook workbook = new XSSFWorkbook();
        initStyles(workbook);

        for (I18nBrand brand : csb.brands) {
            writeNewTable(workbook, brand, null);
        }

        try {
            String path = outputFolder + System.getProperty("file.separator") + StringHelper.getFileName(fileName) + ".xlsx";
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            return true;

        } catch (Exception e) {
            App app = App.get();
            app.setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateExcelSheet(ReaderConfig readerConfig, WriterConfig writerConfig, I18nCSB csb, File excelFile, String sheetName) {
        config = writerConfig;
        reader.config = readerConfig;

        if (excelFile == null || !Files.isWritable(excelFile.toPath())) return false;

        FileInputStream fis;
        try {
            // don't know why but FIS proved to be 2-3 times faster than directly using file or OPC package.
            fis = new FileInputStream(excelFile);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            initStyles(workbook);

            if (csb.brands.size() == 1) {
                updateExistingSheets(workbook, csb);
                if (csb.isValid()) {
                    csb.fillInEmpties(true);
                    csb.sanitize();
                    csb.sort();
                    writeNewTable(workbook, csb.brands.get(0), sheetName);
                }
            } else {
                updateExistingSheets(workbook, csb);
                if (csb.isValid()) {
                    csb.fillInEmpties(true);
                    csb.sanitize();
                    csb.sort();
                    for (I18nBrand brand : csb.brands) {
                        writeNewTable(workbook, brand, brand.name + "_" + sheetName);
                    }
                }
            }
            fis.close();

            FileOutputStream fos;
            if (config.bSaveAsNewFile) {
                File file = new File(config.exportAbsolutePath);
                fos = new FileOutputStream(file);
            } else {
                fos = new FileOutputStream(config.exportAbsolutePath);
            }

            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (Exception e) {
            App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
        }

        return true;
    }

    public void writeNewTable(XSSFWorkbook workbook, I18nBrand brand, String sheetName) {
        initStyles(workbook);

        if (!StringHelper.isValid(sheetName)) {
            sheetName = brand.name;
        }
        XSSFSheet sheet = workbook.createSheet(sheetName);
        if (config.bColorizeNew) {
            System.out.println("colorize new");
            sheet.setTabColor(config.getNewCellFillColor(workbook));
        } else {
            System.out.println("NOT colorize new");

        }
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
                            if (config.bColorizeNew) {
                                cell.setCellStyle(newStyle);
                            }
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
                        if (config.bColorizeNew) {
                            cell.setCellStyle(newStyle);
                        }
                        ++rowIndex;
                    }
                }
            }
            bBlankSheet = false;
            ++columnOfLocale;
        }

    }

    void updateExistingSheets(XSSFWorkbook workbook, I18nCSB csb) {
        for (int i = 0; i < workbook.getNumberOfSheets(); ++i) {
            boolean bSheetWasUpdated = false;
            XSSFSheet sheet = workbook.getSheetAt(i);
            String sheetBrand = reader.getBrand(sheet);
            for (int brandIdx = csb.brands.size() - 1; brandIdx >= 0; --brandIdx) {
                I18nBrand brand = csb.brands.get(brandIdx);

                if (brand.name.equals(sheetBrand)) {
                    int componentCol = reader.findColumnWithString(sheet, ExcelReader.COMPONENT);
                    int keyCol = reader.findColumnWithString(sheet, ExcelReader.KEY);

                    ArrayList<QueryResult> sheetLocales = reader.findLocales(sheet);
                    for (QueryResult sheetLocale : sheetLocales) {
                        for (int rowIdx = sheetLocale.row; rowIdx <= sheet.getLastRowNum(); ++rowIdx) {
                            Row row = sheet.getRow(rowIdx);
                            if (row == null) continue;
                            String component = reader.getCellValue(row, componentCol);
                            String key = reader.getCellValue(row, keyCol);
                            for (int langIdx = brand.languages.size() - 1; langIdx >= 0; --langIdx) {
                                I18nLanguage lang = brand.languages.get(langIdx);
                                if (sheetLocale.locale.equals(lang.locale)) {
                                    for (int translationIdx = lang.translations.size() - 1; translationIdx >= 0; --translationIdx) {
                                        I18n translation = lang.translations.get(translationIdx);
                                        if (!component.equals(translation.component)) continue;

                                        if (translation.isJSON()) {
                                            for (int dataIdx = translation.json.size() - 1; dataIdx >= 0; --dataIdx) {
                                                I18nData data = translation.json.get(dataIdx);
                                                String csbKey = translation.key + "." + data.key;
                                                if (!key.equals(csbKey)) continue;
                                                String sheetValue = reader.getCellValue(row, sheetLocale.col);
                                                if (!StringHelper.isValid(data.value)) continue;
                                                if (sheetValue.equals(data.value)) continue;
                                                Cell cell = row.getCell(sheetLocale.col);
                                                cell.setCellValue(data.value);
                                                if (config.bColorizeOverridden) {
                                                    cell.setCellStyle(overrideStyle);
                                                }
                                                translation.json.remove(dataIdx);
                                                bSheetWasUpdated = true;
                                                break;
                                            }
                                        } else {
                                            I18nData data = translation.json.get(0);
                                            if (!key.equals(translation.key)) continue;
                                            String sheetValue = reader.getCellValue(row, sheetLocale.col);
                                            if (!StringHelper.isValid(data.value)) continue;
                                            if (sheetValue.equals(data.value)) continue;
                                            Cell cell = row.getCell(sheetLocale.col);
                                            cell.setCellValue(data.value);
                                            if (config.bColorizeOverridden) {
                                                cell.setCellStyle(overrideStyle);
                                            }
                                            translation.json.clear();
                                            bSheetWasUpdated = true;
                                        }
                                        if (!translation.isValid(false)) {
                                            lang.translations.remove(translationIdx);
                                        }
                                    }
                                }
                                if (lang.isEmpty()) {
                                    brand.languages.remove(langIdx);
                                }
                            }
                        }
                    }
                    break;
                }
                if (brand.isEmpty()) {
                    csb.brands.remove(brandIdx);
                }
            }
            if (bSheetWasUpdated) {
                if (config.bColorizeOverridden) {
                    sheet.setTabColor(config.getOverrideCellFillColor(workbook));
                }
            }
        }
    }
}
