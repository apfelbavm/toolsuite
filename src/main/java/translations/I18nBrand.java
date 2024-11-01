package translations;

import java.util.ArrayList;

public class I18nBrand {
    String brand;
    ArrayList<I18nLanguage> languages = new ArrayList<I18nLanguage>();

    public I18nBrand(String brand) {
        this.brand = brand;
    }

    public boolean add(String locale, I18n i18n) {
        for (I18nLanguage col : languages) {
            if (col.locale.equals(locale)) {
                return col.add(i18n, false);
            }
        }
        I18nLanguage col = new I18nLanguage(locale);
        languages.add(col);
        return col.add(i18n, false);
    }

    public boolean append(I18nLanguage language) {
        for (I18nLanguage lang : languages) {
            if (lang.locale.equals(language.locale)) {
                return lang.append(language);
            }
        }
        languages.add(language);
        return true;
    }

    public void sort() {
        for (I18nLanguage col : languages) {
            col.sort();
        }
    }

    public void print() {
        for (I18nLanguage col : languages) {
            col.print();
        }
    }
}
