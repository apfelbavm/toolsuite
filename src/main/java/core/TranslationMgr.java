package core;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import reader.FileReader;
import structs.LanguageIdentifier;
import structs.LanguageTable;
import translations.I18nCSB;
import writer.JsonCreator;

public class TranslationMgr {
    private LanguageTable languageTable;
    // Grid size in which this tool searches for all necessary data to extract the
    // rest of a single sheet.
    // These are variables for stat tracking.
    public int statNumEmptyCells = 0;
    private long statCalculationTime = 0;
    public File[] files;
    private int importFlags = 0;
    private int exportFlags = 0;
    public TranslationMgrFlags.FolderNaming folderNamingType;

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

    public LanguageTable importFiles() {
        FileReader reader = new FileReader();
        I18nCSB csb = reader.read(files);

        long statSort = System.currentTimeMillis();
        csb.sort();
        csb.print();

        String[][] data = csb.createTable();
        LanguageIdentifier[] header = csb.getHeader();

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
