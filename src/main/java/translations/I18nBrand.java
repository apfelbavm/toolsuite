package translations;

import java.util.ArrayList;

public class I18nDictionary {
    String brand;
    ArrayList<I18nCollection> collections = new ArrayList<I18nCollection>();

    public I18nDictionary() {

    }

    public boolean add(String locale, I18n i18n) {
        for (I18nCollection col : collections) {
            if (col.locale.equals(locale)) {
                return col.add(i18n);
            }
        }
        I18nCollection col = new I18nCollection(locale);
        collections.add(col);
        return col.add(i18n);
    }

    public void sort() {
        for (I18nCollection col : collections) {
            col.sort();
        }
    }

    public void print()
    {
        for(I18nCollection col : collections)
        {
            col.print();
        }
    }
}
