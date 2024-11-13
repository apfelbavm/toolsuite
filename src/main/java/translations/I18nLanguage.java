package translations;

import java.util.ArrayList;

public class I18nLanguage implements Comparable<I18nLanguage> {
    public String locale;
    private ArrayList<I18n> translations = new ArrayList<I18n>();

    public static final String META_STRING = "_meta";
    public static final String META_LOCALE_STRING = "locale";
    public static final String META_BRAND_STRING = "brand";

    public I18nLanguage(String locale) {
        this.locale = locale;
        addMetaLocale(locale);
    }

    public boolean add(I18n i18n, boolean bOverride) {
        for (I18n translation : translations) {
            I18nResult Result = translation.compareAndMaybeAdd(i18n, bOverride);
            if (Result != I18nResult.NotFound) return true;
        }
        translations.add(i18n);
        return true;
    }

    public boolean append(I18nLanguage other) {
        if (!locale.equals(other.locale)) return false;
        for (I18n translation : other.translations) {
            add(translation, false);
        }
        return true;
    }

    public boolean hasLocale() {
        return locale != null && !locale.isBlank() && locale.isEmpty();
    }

    public void sort() {
        SortManager.quickSort(translations);
    }

    public void print() {
        for (I18n i18n : translations) {
            i18n.print();
        }
    }

    @Override
    public int compareTo(I18nLanguage other) {
        return locale.compareTo(other.locale);
    }

    public void addMetaBrand(String brandName) {
        if (brandName != null && !brandName.isBlank() && !brandName.isEmpty()) {
            I18n brand = new I18n("", "", META_STRING, META_BRAND_STRING, brandName);
            add(brand, true);
        }
    }

    public void addMetaLocale(String localeName) {
        if (localeName != null && !localeName.isBlank() && !localeName.isEmpty()) {
            I18n metaLocale = new I18n("", "", META_STRING, META_LOCALE_STRING, localeName);
            add(metaLocale, true);
        }
    }
}
