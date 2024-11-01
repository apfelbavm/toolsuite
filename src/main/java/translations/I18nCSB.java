package translations;

import java.util.ArrayList;

public class i18CSB {
    ArrayList<I18nBrand> brands = new ArrayList<I18nBrand>();

    public boolean add(String brand, String locale, I18n i18n) {
        for (I18nBrand i18nBrand : brands) {
            if (i18nBrand.brand.equals(brand)) {
                return i18nBrand.add(locale, i18n);
            }
        }
        I18nBrand i18nBrand = new I18nBrand(brand);
        brands.add(i18nBrand);
        return i18nBrand.add(locale, i18n);
    }
}
