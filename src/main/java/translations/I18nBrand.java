package translations;

import java.util.ArrayList;

public class I18nBrand implements Comparable<I18nBrand> {
    public String name;
    public ArrayList<I18nLanguage> languages = new ArrayList<I18nLanguage>();

    public I18nBrand(String brand) {
        this.name = brand;
    }

    public boolean add(String locale, I18n i18n) {
        for (I18nLanguage col : languages) {
            if (col.locale.equals(locale)) {
                return col.add(i18n, false);
            }
        }
        I18nLanguage col = new I18nLanguage(name, locale);
        languages.add(col);
        return col.add(i18n, false);
    }

    public boolean append(ArrayList<I18nLanguage> newLanguages) {
        boolean bSuccess = false;
        for (I18nLanguage lang : newLanguages) {
            bSuccess |= append(lang);
        }
        return bSuccess;
    }

    public boolean append(I18nLanguage newLanguage) {
        for (I18nLanguage lang : languages) {
            if (lang.locale.equals(newLanguage.locale)) {
                return lang.append(newLanguage);
            }
        }
        languages.add(newLanguage);
        return true;
    }

    public void sort() {
        SortManager.quickSort(languages);
        for (I18nLanguage lang : languages) {
            lang.sort();
        }
    }

    public void print() {
        System.out.println("______________________________brand " + name + "______________________________");
        for (I18nLanguage lang : languages) {
            lang.print();
        }
    }

    @Override
    public int compareTo(I18nBrand other) {
        return name.compareTo(other.name);
    }
}
