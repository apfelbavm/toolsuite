package writer;

import core.App;
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

            LanguageTable table = csb.createLanguageTable(brand.name);

            {
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                cell.setCellValue(brand.name);
                cell.setCellStyle(redStyle);
            }

            Row metaRow = sheet.createRow(ROW_OFFSET_META);
            {
                Cell compCell = metaRow.createCell(COL_OFFSET_COMPONENT);
                compCell.setCellValue("component");
                compCell.setCellStyle(redStyle);

                Cell keyCell = metaRow.createCell(COL_OFFSET_KEY);
                keyCell.setCellValue("key");
                keyCell.setCellStyle(redStyle);
            }

            String[][] values = table.getJTableData();
            LanguageIdentifier[] locales = table.getIdentifiers();


            for (int colIdx = 0; colIdx < locales.length; ++colIdx) {
                Cell langCell = metaRow.createCell(COL_OFFSET_LANG + colIdx);
                langCell.setCellValue(locales[colIdx].locale);
                langCell.setCellStyle(redStyle);
            }

            for (int rowIdx = 0; rowIdx < values.length; ++rowIdx) {
                Row row = sheet.createRow(ROW_OFFSET_TRANSLATIONS + rowIdx);
                for (int colIdx = 0; colIdx < values[0].length; ++colIdx) {
                    if (rowIdx < 3) {
                        if (values[rowIdx][colIdx].equals(I18nLanguage.META_STRING)) break;
                    }
                    Cell cell = row.createCell(colIdx);
                    cell.setCellValue(values[rowIdx][colIdx]);

                    if (colIdx < 2) {
                        cell.setCellStyle(redStyle);
                    }
                }
            }

//            {
//                Row row = sheet.createRow(0);
//                Cell cell = row.createCell(0);
//                cell.setCellValue(brand.name);
//                cell.setCellStyle(redStyle);
//            }
//

//
//            {
//                int langIdx = 0;
//                for (I18nLanguage lang : brand.languages) {
//
//                    {
//                        Cell cell = metaRow.createCell(COL_OFFSET_LANG + langIdx);
//                        cell.setCellValue(lang.locale);
//                        cell.setCellStyle(redStyle);
//                    }
//
//                    int rowIdx = 0;
//                    for (I18n i18n : lang.translations) {
//
//                        if (i18n.component.equals(I18nLanguage.META_STRING)) continue;
//
//                        if (i18n.isJSON()) {
//                            ArrayList<I18nData> json = i18n.getJSONSorted(false);
//                            for (I18nData data : json) {
//                                Row row;
//
//                                if (langIdx == 0) {
//                                    row = sheet.createRow(ROW_OFFSET_TRANSLATIONS + rowIdx);
//                                    {
//                                        Cell cell = row.createCell(COL_OFFSET_COMPONENT);
//                                        cell.setCellValue(i18n.component);
//                                        cell.setCellStyle(redStyle);
//                                    }
//                                    {
//                                        Cell cell = row.createCell(COL_OFFSET_KEY);
//                                        cell.setCellValue(i18n.data.key + "." + data.key);
//                                        cell.setCellStyle(redStyle);
//                                    }
//                                } else {
//                                    row = sheet.getRow(ROW_OFFSET_TRANSLATIONS + rowIdx);
//                                }
//
//                                Cell cell = row.createCell(COL_OFFSET_LANG + langIdx);
//                                cell.setCellValue(data.value);
//
//                                if (config.bColorizeChangedCells) {
//                                    switch (i18n.data.compareResult) {
//                                        default:
//                                            break;
//                                        case Override: {
//                                            cell.setCellStyle(overrideStyle);
//                                            break;
//                                        }
//                                        case New: {
//                                            cell.setCellStyle(newStyle);
//                                            break;
//                                        }
//                                    }
//                                }
//                                ++rowIdx;
//                            }
//                        } else {
//                            Row row;
//
//                            if (langIdx == 0) {
//                                row = sheet.createRow(ROW_OFFSET_TRANSLATIONS + rowIdx);
//                                {
//                                    Cell cell = row.createCell(COL_OFFSET_COMPONENT);
//                                    cell.setCellValue(i18n.component);
//                                }
//                                {
//                                    Cell cell = row.createCell(COL_OFFSET_KEY);
//                                    cell.setCellValue(i18n.data.key);
//                                }
//                            } else {
//                                row = sheet.getRow(ROW_OFFSET_TRANSLATIONS + rowIdx);
//                            }
//
//                            Cell cell = row.createCell(COL_OFFSET_LANG + langIdx);
//                            cell.setCellValue(i18n.data.value);
//
//                            ++rowIdx;
//                        }
//                    }
//                    ++langIdx;
//                }
//            }
        }

        try {
            FileOutputStream out = new FileOutputStream(
                    new File(outputFolder + System.getProperty("file.separator") + fileName + ".xlsx"));
            workbook.write(out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
