package translations;

import widgets.table.LanguageIdentifier;

import java.util.*;

public class I18nCSB {
    public ArrayList<I18nBrand> brands = new ArrayList<I18nBrand>();
    private ArrayList<I18nRowMap> rowMap;
    private boolean bNeedsRegenerateRowMap = false;

    public boolean add(String brand, String locale, I18n i18n) {
        for (I18nBrand i18nBrand : brands) {
            if (i18nBrand.name.equals(brand)) {
                boolean bSuccess = i18nBrand.add(locale, i18n);
                if (bSuccess) {
                    bNeedsRegenerateRowMap = true;
                }
                return bSuccess;
            }
        }
        I18nBrand i18nBrand = new I18nBrand(brand);
        brands.add(i18nBrand);
        bNeedsRegenerateRowMap = true;
        return i18nBrand.add(locale, i18n);
    }

    public boolean add(String brand, I18nLanguage language) {
        for (I18nBrand i18nBrand : brands) {
            if (i18nBrand.name.equals(brand)) {
                boolean bSuccess = i18nBrand.append(language);
                if (bSuccess) {
                    bNeedsRegenerateRowMap = true;
                }
                return bSuccess;
            }
        }
        I18nBrand i18nBrand = new I18nBrand(brand);
        brands.add(i18nBrand);
        bNeedsRegenerateRowMap = true;
        return i18nBrand.append(language);
    }

    public boolean add(I18nBrand newBrand) {
        for (I18nBrand brand : brands) {
            if (brand.name.equals(newBrand.name)) {
                boolean bSuccess = brand.append(newBrand.languages);
                if (bSuccess) {
                    bNeedsRegenerateRowMap = true;
                }
                return bSuccess;
            }
        }
        bNeedsRegenerateRowMap = true;
        return brands.add(newBrand);
    }

    public void sort() {
        SortManager.quickSort(brands);
        for (I18nBrand brand : brands) {
            brand.sort();
        }
    }

    private ArrayList<I18nRowMap> getRowMap() {
        if (bNeedsRegenerateRowMap) {
            bNeedsRegenerateRowMap = false;
            regenerateRowMap();
        }
        return rowMap;
    }

    public void print() {
        for (I18nBrand brand : brands) {
            brand.print();
        }
    }

    private void regenerateRowMap() {
        rowMap = new ArrayList<I18nRowMap>();
        for (I18nBrand brand : brands) {
            for (I18nLanguage lang : brand.languages) {
                for (I18n i18n : lang.translations) {
                    boolean bFound = false;
                    for (I18nRowMap row : rowMap) {
                        if (row.component.equals(i18n.component) && row.key.equals(i18n.key)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        rowMap.add(new I18nRowMap(i18n.component, i18n.key));
                    }
                }
            }
        }
        SortManager.quickSort(rowMap);
    }

    private int countTotalLanguages() {
        int count = 0;
        for (I18nBrand brand : brands) {
            count += brand.languages.size();
        }
        return count;
    }

    public String[][] createTable() {
        ArrayList<I18nRowMap> map = getRowMap();

        int numLangs = countTotalLanguages();

        String[][] data = new String[map.size()][numLangs + 2];

        int statNumEmptyCells = 0;
        for (int c = 0; c < numLangs; ++c) {
            I18nLanguage lang = getLanguageBySortedIndex(c);
            int r = 0;
            for (I18nRowMap row : map) {
                String value = lang.getRow(row.component, row.key);
                data[r][0] = row.component;
                data[r][1] = row.key;
                if (value == null || value.isBlank() || value.isEmpty()) ++statNumEmptyCells;
                data[r][c + 2] = value;
                ++r;
            }
        }
        return data;
    }

    public I18nLanguage getLanguageBySortedIndex(int index) {
        int i = 0;
        for (I18nBrand brand : brands) {
            int langIndex = index - i;
            if (brand.languages.size() > langIndex) {
                return brand.languages.get(langIndex);
            }
            i += brand.languages.size();
        }
        return null;
    }

    public LanguageIdentifier[] getHeader() {
        int numLangs = countTotalLanguages();
        LanguageIdentifier[] header = new LanguageIdentifier[numLangs];
        int i = 0;
        for (I18nBrand brand : brands) {
            for (I18nLanguage lang : brand.languages) {
                header[i] = new LanguageIdentifier(brand.name, lang.locale);
                ++i;
            }
        }
        return header;
    }

    public void merge(I18nCSB other) {
        for (I18nBrand brand : other.brands) {
            add(brand);
        }
    }
}
