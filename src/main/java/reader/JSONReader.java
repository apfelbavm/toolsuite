package reader;

import core.TranslationMgr;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import translations.I18n;
import translations.I18nCSB;
import translations.I18nLanguage;

public class JSONReader {

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

    public I18nCSB read(ArrayList<File> files) {
        I18nCSB csb = new I18nCSB();
        for (File file : files) {
            I18nLanguage language = read("", file);
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
                csb.add("", language);
            }
        }
        return csb;
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

    private I18nLanguage read(String locale, File file) {

        I18nLanguage language = new I18nLanguage("", locale);
        try {
            InputStream fis = new FileInputStream(file);
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject parent = new JSONObject(tokener);

            for (Object componentName : parent.names()) {
                String componentNameString = componentName.toString();
                JSONObject component = (JSONObject) parent.get(componentNameString);

                if (componentNameString.equals(I18nLanguage.META_STRING)) {
                    for (Object keyName : component.names()) {
                        String keyNameString = keyName.toString();
                        switch (keyNameString) {
                            case I18nLanguage.META_LOCALE_STRING: {
                                language.addMetaLocale(component.get(keyNameString).toString());
                                break;
                            }
                            case I18nLanguage.META_BRAND_STRING: {
                                language.addMetaBrand(component.get(keyNameString).toString());
                                break;
                            }
                        }
                    }
                } else {
                    for (Object keyName : component.names()) {
                        I18n i18n = new I18n("", "", componentName.toString(), keyName.toString(), component.get(keyName.toString()).toString());
                        boolean bAdded = language.add(i18n, false);
                    }
                }
            }
        } catch (Exception e) {
            System.out.print(e);
        }
        return language;
    }
}
