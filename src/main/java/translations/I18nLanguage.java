package translations;

import java.util.ArrayList;

public class I18nCollection implements Comparable<I18nCollection> {
    public String locale;
    private ArrayList<I18n> translations = new ArrayList<I18n>();

    public I18nCollection(String locale) {
        this.locale = locale;
    }

    public boolean add(I18n i18n) {
        for (I18n translation : translations) {
            if (translation.component.equals(i18n.component) && translation.key.equals(i18n.key)) return false;
        }
        translations.add(i18n);
        return true;
    }

    public void append(I18nCollection other) {
        if (locale != other.locale) return;
        for (I18n translation : other.translations) {
            add(translation);
        }
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
    public int compareTo(I18nCollection other) {
        return locale.compareTo(other.locale);
    }
}
