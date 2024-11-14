package reader;

import core.App;
import core.TranslationMgr;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import widgets.table.LanguageIdentifier;
import translations.I18n;
import translations.I18nBrand;
import translations.I18nCSB;
import translations.I18nLanguage;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class ExcelReader {
    boolean bUseHyperlinkIfAvailable = false;
    boolean bIncludeHiddenSheets = false;
    public int statNumEmptyCells = 0;
    // Grid size in which this tool searches for all necessary data to extract the
    // rest of a single sheet.
    private static final int MAX_SEARCH_COLUMN = 60;
    private static final int MAX_SEARCH_ROW = 20;

    private String getCellValue(Row row, int col) {
        if (row == null) return null;
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell == null) return null;

        if (bUseHyperlinkIfAvailable) {
            Hyperlink link = cell.getHyperlink();
            if (link != null) {
                return link.getAddress();
            }
        }

        // if a cell contains a formula we first have to check to what type it evaluates
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        switch (type) {
            default:
            case BLANK:
                return "";
            case BOOLEAN:
                return fixString(String.valueOf(cell.getBooleanCellValue()));
            case STRING:
                return fixString(cell.getStringCellValue());
            case NUMERIC:
                double value = cell.getNumericCellValue();
                if (value % 1 == 0) {
                    return String.valueOf((int) value);
                } else {
                    return String.valueOf(value);
                }
        }
    }

    private String fixString(String str) {
        String value = str.replace("\n", " ").replace("\r", " ").replace(System.getProperty("line.separator"), " ");
        // Replace double spacebar
        value = value.replace("\"", "\\\"");
        value = value.replaceAll("( )+", " ");
        return value.trim();
    }

    private boolean isLocale(String value) {
        return TranslationMgr.ISO_CODES.contains(value.toLowerCase()) || TranslationMgr.SUCCESSFACTOR_CODES.contains(value.toLowerCase());
    }

    private String findBrand(Sheet sheet) {
        if (sheet == null) return "";
        Row row = sheet.getRow(0);
        if (row == null) return "";
        return getCellValue(row, 0);
    }

    /**
     * @return List of all locales found in the sheet including their columnID.
     **/
    private ArrayList<Pair<LanguageIdentifier, Integer>> findLocales(Sheet sheet) {
        if (sheet == null) return null;
        String brand = findBrand(sheet);
        if (brand == null || brand.isBlank() || brand.isEmpty()) {
            brand = "NO_BRAND";
        }
        ArrayList<Pair<LanguageIdentifier, Integer>> locales = new ArrayList<Pair<LanguageIdentifier, Integer>>(32);
        int lastRow = Math.min(MAX_SEARCH_ROW, sheet.getLastRowNum()); // 0 based
        for (int r = 0; r <= lastRow; ++r) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            int lastCol = Math.min(MAX_SEARCH_COLUMN, row.getLastCellNum());
            for (int c = 0; c < lastCol; ++c) {
                String value = getCellValue(row, c);
                if (value != null && isLocale(value)) {
                    locales.add(new Pair<LanguageIdentifier, Integer>(new LanguageIdentifier(brand, value), c));
                }
            }
        }
        return locales;
    }

    private int findFirstValueRow(Sheet sheet, int componentCol, int keyCol, int valueCol) {
        if (sheet == null) return -1;
        final String COMPONENT = "component";
        final String KEY = "key";
        boolean foundTableHeader = false;
        int lastRow = sheet.getLastRowNum(); // 0 based
        for (int r = 0; r <= lastRow; ++r) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            String component = getCellValue(row, componentCol);
            if (component == null) continue;

            String key = getCellValue(row, keyCol);
            if (key == null) continue;

            if (!foundTableHeader) {
                if (component.equalsIgnoreCase(COMPONENT) && key.equalsIgnoreCase(KEY)) {
                    foundTableHeader = true;
                }
            } else {
                if (component.isBlank() || component.isEmpty() || key.isBlank() || key.isEmpty()) continue;
                return r;
            }
        }
        return -1;
    }

    private I18nLanguage extractLanguage(Sheet sheet, LanguageIdentifier ident, int startRow, int componentCol, int keyCol, int valueCol) {
        if (sheet == null) return null;

        I18nLanguage lang = new I18nLanguage(ident.brand, ident.locale);

        for (int r = startRow; r <= sheet.getLastRowNum(); ++r)// 0 based
        {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            String component = getCellValue(row, componentCol);
            if (component == null || component.isBlank() || component.isEmpty()) continue;

            String key = getCellValue(row, keyCol);
            if (key == null || key.isBlank() || key.isEmpty()) continue;
            String value = getCellValue(row, valueCol);
            if (value != null && !value.isBlank() && !value.isEmpty()) {
                I18n i18n = new I18n("", "", component, key, value);
                lang.add(i18n, false);
            }
        }

        return lang;
    }

    private static void getSheetsFromExcel(HashSet<Sheet> sheets, File file) {
        if (file == null) return;
        FileInputStream fis;
        try {
            // don't know why but FIS proved to be 2-3 times faster than directly using file or OPC package.
            fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            int numSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numSheets; ++i) {
                sheets.add(workbook.getSheetAt(i));
            }
            workbook.close();
        } catch (Exception e) {
            App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
        }
    }

    /**
     * Searches in a clamped area of r= MAX_SEARCH_ROW to c = MAX_SEARCH_COLUMN and returns the index of the first occurence of the specified string.
     */
    private int findColumnWithString(Sheet sheet, String string) {
        if (sheet == null) return -1;
        int lastRow = Math.min(MAX_SEARCH_ROW, sheet.getLastRowNum()); // 0 based
        for (int r = 0; r <= lastRow; ++r) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = 0; c < MAX_SEARCH_COLUMN; ++c) {
                String value = getCellValue(row, c);
                if (value != null && value.equalsIgnoreCase(string)) return c;
            }
        }
        return -1;
    }

    private I18nBrand extractSheet(Sheet sheet) {
        if (sheet == null) return null;
        ArrayList<Pair<LanguageIdentifier, Integer>> locales = findLocales(sheet);
        if (locales == null || locales.isEmpty()) return null;
        int componentCol = findColumnWithString(sheet, "component");
        int keyCol = findColumnWithString(sheet, "key");
        if (componentCol == -1 || keyCol == -1) return null;

        I18nBrand brand = new I18nBrand(locales.get(0).getFirst().brand);


        for (Pair<LanguageIdentifier, Integer> pair : locales) {
            int valueCol = pair.getValue();
            int firstRow = findFirstValueRow(sheet, componentCol, keyCol, valueCol);
            if (firstRow < 0) continue; // If no value was found we must escape this sheet.

            I18nLanguage lang = extractLanguage(sheet, pair.getKey(), firstRow, componentCol, keyCol, valueCol);
            brand.append(lang);
        }
        return brand;
    }

    public I18nCSB read(ArrayList<File> files) {
        if (files == null) return null;
        statNumEmptyCells = 0;
        HashSet<Sheet> sheets = new HashSet<Sheet>();

        long statRead = System.currentTimeMillis();

        Thread[] threads = new Thread[files.size()];
        int w = 0;
        for (int v = 0; v < files.size(); ++v) {
            final int v2 = v;
            threads[w] = new Thread(() -> {
                getSheetsFromExcel(sheets, files.get(v2));
            });
            ++w;
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        statRead = System.currentTimeMillis() - statRead;
        System.out.println("Loading excel files took:" + statRead + "ms");

        long statExtract = System.currentTimeMillis();

        I18nCSB csb = new I18nCSB();
        for (Sheet sheet : sheets) {
            int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);

            boolean bIsHidden = sheet.getWorkbook().isSheetHidden(sheetIndex);
            boolean bIsVeryHidden = sheet.getWorkbook().isSheetVeryHidden(sheetIndex);

            if (bIsHidden || bIsVeryHidden) {
                if (!bIncludeHiddenSheets) continue;
            }

            I18nBrand brand = extractSheet(sheet);
            if (brand == null) continue;
            csb.add(brand);
        }

        statExtract = System.currentTimeMillis() - statExtract;
        System.out.println("Extracting excel files took:" + statExtract + "ms");
        System.out.println("Extracting excel files took:" + statExtract + "ms");

        return csb;
    }
}
