package core;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import structs.Language;
import structs.LanguageIdentifier;
import structs.LanguageTable;
import structs.LanguageRowMap;

public class TranslationMgr {
    private LanguageTable languageTable;
    // Grid size in which this tool searches for all necessary data to extract the
    // rest of a single sheet.
    private static final int MAX_SEARCH_COLUMN = 60;
    private static final int MAX_SEARCH_ROW = 20;
    // These are variables for stat tracking.
    public int statNumEmptyCells = 0;
    private long statCalculationTime = 0;
    public File[] files;
    public boolean appendBrandToLocale = false;
    private int importFlags = 0;
    private int exportFlags = 0;
    public TranslationMgrFlags.FolderNaming folderNamingType;

    private boolean getFlag(TranslationMgrFlags.Import flag) {
        return ((importFlags >> flag.ordinal()) & 1) == 1;
    }

    private boolean getFlag(TranslationMgrFlags.Export flag) {
        return ((exportFlags >> flag.ordinal()) & 1) == 1;
    }

    public void setFlag(TranslationMgrFlags.Import flag, boolean bEnable) {
        if (bEnable) {
            importFlags |= 1 << flag.ordinal();
        } else {
            importFlags &= ~(1 << flag.ordinal());
        }
    }

    public void setFlag(TranslationMgrFlags.Export flag, boolean bEnable) {
        if (bEnable) {
            exportFlags |= 1 << flag.ordinal();
        } else {
            exportFlags &= ~(1 << flag.ordinal());
        }
    }

    public int getNumSelectedFiles() {
        if (files == null) return 0;
        return files.length;
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

    private String emptyCellValue = "";

    private String getCellValue(Row row, int col) {
        if (row == null) return null;
        Cell cell = row.getCell(col, MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell == null) return null;

        if (getFlag(TranslationMgrFlags.Import.USE_HYPERLINK_IF_AVAILABLE)) {
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
                return emptyCellValue;
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

    static Pattern p = Pattern.compile("");

    private String fixString(String str) {
        String value = str.replace("\n", " ").replace("\r", " ").replace(System.getProperty("line.separator"), " ");
        // Replace double spacebar
        value = value.replace("\"", "\\\"");
        value = value.replaceAll("( )+", " ");
        return value.trim();
    }

    public String getFileName(String path) {
        int dot = path.lastIndexOf(".");
        int slash = path.lastIndexOf("\\");
        if (slash == -1) slash = path.lastIndexOf("/");
        if (dot > slash) return path.substring(slash > 0 ? slash + 1 : 0, dot);
        return path.substring(slash > 0 ? slash + 1 : 0);
    }

    public boolean export2Json(String outputFolder, String fileName) {
        JsonCreator json = new JsonCreator();
        boolean bMergeComponentAndKey = getFlag(TranslationMgrFlags.Export.CONCAT_COMPONENT_AND_KEY);
        boolean bSkipEmptyCells = getFlag(TranslationMgrFlags.Export.DONT_EXPORT_EMPTY_VALUES);
        return json.export2Json(languageTable, outputFolder, fileName, bMergeComponentAndKey, bSkipEmptyCells, folderNamingType);
    }

    public static final HashSet<String> ISO_CODES = new HashSet<String>(Arrays.asList(new String[]{"af_za", "am_et", "ar_ae", "ar_bh", "ar_dz", "ar_eg", "ar_iq", "ar_jo", "ar_kw", "ar_lb", "ar_ly", "ar_ma",
            "arn_cl", "ar_om", "ar_qa", "ar_sa", "ar_sd", "ar_sy", "ar_tn", "ar_ye", "as_in", "az_az", "az_cyrl_az", "az_latn_az", "ba_ru", "be_by", "bg_bg", "bn_bd", "bn_in", "bo_cn", "br_fr",
            "bs_cyrl_ba", "bs_latn_ba", "ca_es", "co_fr", "cs_cz", "cy_gb", "da_dk", "de_at", "de_ch", "de_de", "de_li", "de_lu", "dsb_de", "dv_mv", "el_cy", "el_gr", "en_029", "en_au", "en_bz", "en_ca",
            "en_cb", "en_gb", "en_es", "en_ie", "en_in", "en_it", "en_jm", "en_mt", "en_my", "en_nz", "en_ph", "en_pt", "en_tr", "en_sg", "en_tt", "en_us", "en_za", "en_zw", "es_ar", "es_bo", "es_cl",
            "es_co", "es_cr", "es_do", "es_ec", "es_es", "es_gt", "es_hn", "es_mx", "es_ni", "es_pa", "es_pe", "es_pr", "es_py", "es_sv", "es_us", "es_uy", "es_ve", "et_ee", "eu_es", "fa_ir", "fi_fi",
            "fil_ph", "fo_fo", "fr_be", "fr_ca", "fr_ch", "fr_fr", "fr_lu", "fr_mc", "fy_nl", "ga_ie", "gd_gb", "gd_ie", "gl_es", "gsw_fr", "gu_in", "ha_latn_ng", "he_il", "hi_in", "hr_ba", "hr_hr",
            "hsb_de", "hu_hu", "hy_am", "id_id", "ig_ng", "ii_cn", "in_id", "is_is", "it_ch", "it_it", "iu_cans_ca", "iu_latn_ca", "iw_il", "ja_jp", "ka_ge", "kk_kz", "kl_gl", "km_kh", "kn_in", "kok_in",
            "ko_kr", "ky_kg", "lb_lu", "lo_la", "lt_lt", "lv_lv", "mi_nz", "mk_mk", "ml_in", "mn_mn", "mn_mong_cn", "moh_ca", "mr_in", "ms_bn", "ms_my", "mt_mt", "nb_no", "ne_np", "nl_be", "nl_nl",
            "nn_no", "no_no", "nso_za", "oc_fr", "or_in", "pa_in", "pl_pl", "prs_af", "ps_af", "pt_br", "pt_pt", "qut_gt", "quz_bo", "quz_ec", "quz_pe", "rm_ch", "ro_mo", "ro_ro", "ru_mo", "ru_ru",
            "rw_rw", "sah_ru", "sa_in", "se_fi", "se_no", "se_se", "si_lk", "sk_sk", "sl_si", "sma_no", "sma_se", "smj_no", "smj_se", "smn_fi", "sms_fi", "sq_al", "sr_ba", "sr_cs", "sr_cyrl_ba",
            "sr_cyrl_cs", "sr_cyrl_me", "sr_cyrl_rs", "sr_latn_ba", "sr_latn_cs", "sr_latn_me", "sr_latn_rs", "sr_me", "sr_rs", "sr_sp", "sv_fi", "sv_se", "sw_ke", "syr_sy", "ta_in", "te_in",
            "tg_cyrl_tj", "th_th", "tk_tm", "tlh_qs", "tn_za", "tr_tr", "tt_ru", "tzm_latn_dz", "ug_cn", "uk_ua", "ur_pk", "uz_cyrl_uz", "uz_latn_uz", "uz_uz", "vi_vn", "wo_sn", "xh_za", "yo_ng", "zh_cn",
            "zh_hk", "zh_mo", "zh_sg", "zh_tw", "zu_za"}));

    public static final HashSet<String> SUCCESSFACTOR_CODES = new HashSet<String>(Arrays.asList(new String[]{"bs_id", "bs_BS", "cnr_ME"}));

    private boolean isLocale(String value) {
        return ISO_CODES.contains(value.toLowerCase()) || SUCCESSFACTOR_CODES.contains(value.toLowerCase());
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

    private Language extractLanguage(Sheet sheet, LanguageIdentifier languageIdentifier, int startRow, int componentCol, int keyCol, int valueCol) {
        if (sheet == null) return null;

        Language language = new Language(languageIdentifier);

        for (int r = startRow; r <= sheet.getLastRowNum(); ++r)// 0 based
        {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            String component = getCellValue(row, componentCol);
            if (component == null || component.isBlank() || component.isEmpty()) continue;

            String key = getCellValue(row, keyCol);
            if (key == null || key.isBlank() || key.isEmpty()) continue;
            String value = getCellValue(row, valueCol);

            language.addUnique(component, key, value);
        }
        return language;
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

    private ArrayList<Language> extractSheet(Sheet sheet) {
        if (sheet == null) return null;
        ArrayList<Pair<LanguageIdentifier, Integer>> locales = findLocales(sheet);
        int componentCol = findColumnWithString(sheet, "component");
        int keyCol = findColumnWithString(sheet, "key");
        if (componentCol == -1 || keyCol == -1) return null;

        ArrayList<Language> languages = new ArrayList<Language>(32);

        for (Pair<LanguageIdentifier, Integer> pair : locales) {
            int valueCol = pair.getValue();
            int firstRow = findFirstValueRow(sheet, componentCol, keyCol, valueCol);
            if (firstRow < 0) continue; // If no value was found we must escape this sheet.

            Language lang = extractLanguage(sheet, pair.getKey(), firstRow, componentCol, keyCol, valueCol);
            languages.add(lang);
        }
        return languages;
    }

    private boolean containsLanguage(ArrayList<Language> languages, Language otherLanguage) {
        if (languages == null) return false;
        for (Language language : languages) {
            if (language.isSameLanguageIdentifier(otherLanguage)) return true;
        }
        return false;
    }

    private void appendLanguage(ArrayList<Language> languages, Language other) {
        if (languages == null) return;
        for (Language language : languages) {
            if (language.isSameLanguageIdentifier(other)) {
                language.appendTable(other);
            }
        }
    }

    public LanguageTable importExcelFiles() {
        if (files == null) return null;
        statNumEmptyCells = 0;
        HashSet<Sheet> sheets = new HashSet<Sheet>();

        long statRead = System.currentTimeMillis();

        Thread[] threads = new Thread[files.length];
        int w = 0;
        for (int v = 0; v < files.length; ++v) {
            final int v2 = v;
            threads[w] = new Thread(() -> {
                getSheetsFromExcel(sheets, files[v2]);
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

        ArrayList<Language> sumLanguages = new ArrayList<Language>(32);
        boolean bIncludeHiddenSheets = getFlag(TranslationMgrFlags.Import.INCLUDE_HIDDEN_SHEETS);
        for (Sheet sheet : sheets) {
            int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);

            boolean bIsHidden = sheet.getWorkbook().isSheetHidden(sheetIndex);
            boolean bIsVeryHidden = sheet.getWorkbook().isSheetVeryHidden(sheetIndex);

            if (bIsHidden || bIsVeryHidden) {
                if (!bIncludeHiddenSheets) continue;
            }

            ArrayList<Language> languages = extractSheet(sheet);
            if (languages == null) continue;
            for (Language language : languages) {
                if (containsLanguage(sumLanguages, language)) {
                    appendLanguage(sumLanguages, language);
                } else {
                    sumLanguages.add(language);
                }
            }
        }

        statExtract = System.currentTimeMillis() - statExtract;
        System.out.println("Extracting excel files took:" + statExtract + "ms");
        System.out.println("Extracting excel files took:" + statExtract + "ms");

        if (sumLanguages.size() == 0) return null;

        long statSort = System.currentTimeMillis();

        LanguageRowMap rowMap = new LanguageRowMap(sumLanguages);
        int numLangs = sumLanguages.size();
        String[][] data = new String[rowMap.rowMap.length][numLangs + 2];
        for (int i = 0; i < data.length; ++i) {
            data[i][0] = rowMap.rowMap[i][0];
            data[i][1] = rowMap.rowMap[i][1];
        }

        LanguageIdentifier[] header = new LanguageIdentifier[sumLanguages.size()];
        for (int i = 0; i < sumLanguages.size(); ++i) {
            Language lang = sumLanguages.get(i);
            header[i] = lang.identifier;
        }

        LanguageIdentifier.sortHeader(header, sumLanguages);

        for (int c = 0; c < numLangs; ++c) {
            Language lang = sumLanguages.get(c);
            int r = 0;
            for (String[] row : rowMap.rowMap) {
                String value = lang.findValue(row[0], row[1]);
                if (value == null || value.isBlank() || value.isEmpty()) ++statNumEmptyCells;
                data[r][c + 2] = value;
                ++r;
            }
        }
        languageTable = new LanguageTable(header, data);

        statSort = System.currentTimeMillis() - statSort;
        System.out.println("Sorting data took:" + statSort + "ms");

        return languageTable;
    }

    public void startTimeTrace() {
        statCalculationTime = System.nanoTime();
    }

    public void stopTimeTrace() {
        statCalculationTime = System.nanoTime() - statCalculationTime;
    }

    public double getCalculationTime() {
        return statCalculationTime / 1000.0 / 1000000.0;
    }
}
