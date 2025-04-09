package translations;

import java.util.ArrayList;

public class I18nLanguage implements Comparable<I18nLanguage> {
    public String locale;
    public ArrayList<I18n> translations = new ArrayList<I18n>();

    public static final String META_STRING = "_meta";
    public static final String META_LOCALE_STRING = "locale";
    public static final String META_BRAND_STRING = "brand";

    public I18nLanguage(String brand_, String locale_) {
        locale = locale_;
        addMetaLocale(locale);
        if (brand_ != null && !brand_.isEmpty() && !brand_.isBlank()) {
            addMetaBrand(brand_);
        }
    }

    public boolean add(I18n i18n, boolean bOverride) {
        for (I18n translation : translations) {
            I18nResult Result = translation.addOrOverride(i18n, bOverride);
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
        return locale != null && !locale.isBlank() && !locale.isEmpty();
    }

    public String getBrand() {
        for (I18n i18n : translations) {
            if (i18n.component.equals(META_STRING)) {
                if (i18n.data.key.equals(META_BRAND_STRING)) {
                    return i18n.data.value;
                }
            }
        }
        return null;
    }

    public void sort() {
        SortManager.quickSort(translations);
    }

    public void print() {
        System.out.println("_______________locale " + locale + "_______________");
        for (I18n i18n : translations) {
            i18n.print();
        }
    }

    @Override
    public int compareTo(I18nLanguage other) {
        return locale.compareTo(other.locale);
    }


    public String getRowDisplayValue(String component, String key) {
        for (I18n i18n : translations) {
            if (i18n.component.equals(component) && i18n.data.key.equals(key)) {
                return i18n.getRowDisplayValue();
            }
        }
        return null;
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
            locale = localeName;
            add(metaLocale, true);
        }
    }

    public void removeAllDuplicates(I18nLanguage other) {
        int outerLastIndex = other.translations.size() - 1;
        for (int outerIdx = outerLastIndex; outerIdx >= 0; --outerIdx) {
            I18n outer = other.translations.get(outerIdx);

            int innerLastIndex = translations.size() - 1;
            for (int innerIdx = innerLastIndex; innerIdx >= 0; --innerIdx) {
                I18n inner = translations.get(innerIdx);

                if (!inner.component.equals(outer.component)) continue;
                if (!inner.data.key.equals(outer.data.key)) continue;

                if (inner.bIsJSON != outer.bIsJSON) {
                    System.err.println("MISMATCH JSON TO JSON");
                    // HANDLE UNEQUALITY

                } else {
                    if (inner.bIsJSON) {
                        inner.removeJsonDuplicates(outer);
                        if (!inner.isValid()) {
                            translations.remove(innerIdx);
                        }

                    } else {
                        if (inner.data.value == null && !inner.data.value.isBlank() || !inner.data.value.isEmpty() || inner.data.value.equals(outer.data.value)) {
                            translations.remove(innerIdx);
                        } else {
                            inner.data.compareResult = I18nCompareResult.Override;
                        }
                    }
                }
                break;
            }
        }
    }

    public boolean isEmpty() {
        return translations.isEmpty();
    }
}
