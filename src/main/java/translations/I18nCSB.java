package translations;

import widgets.table.LanguageIdentifier;
import widgets.table.LanguageTable;

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

    private ArrayList<I18nRowMap> getRowMap(String specificBrand) {
        if (bNeedsRegenerateRowMap || specificBrand != null) {
            bNeedsRegenerateRowMap = false;
            regenerateRowMap(specificBrand);
        }
        return rowMap;
    }

    public void print() {
        for (I18nBrand brand : brands) {
            brand.print();
        }
    }

    private void regenerateRowMap(String specificBrand) {
        rowMap = new ArrayList<I18nRowMap>();
        for (I18nBrand brand : brands) {
            if (specificBrand != null && !specificBrand.equals(brand.name)) continue;
            for (I18nLanguage lang : brand.languages) {
                for (I18n i18n : lang.translations) {
                    if (i18n.isJSON()) {
                        for (I18nData child : i18n.json) {
                            maybeAdd(i18n.component, i18n.data.key, child.key);
                        }
                    } else {
                        maybeAdd(i18n.component, i18n.data.key, null);
                    }
                }
            }
        }
        SortManager.quickSort(rowMap);
    }

    private void maybeAdd(String component, String key, String childKey) {
        boolean bFound = false;

        System.out.println("maybe add: " + component + " " + key + " " + childKey);

        for (I18nRowMap row : rowMap) {
            if (row.eq(component, key, childKey)) {
                bFound = true;
                break;
            }
        }
        if (!bFound) {
            rowMap.add(new I18nRowMap(component, key, childKey));
        }
    }

    private int countTotalLanguages(String specificBrand) {
        int count = 0;
        for (I18nBrand brand : brands) {
            if (specificBrand != null && !specificBrand.equals(brand.name)) continue;
            count += brand.languages.size();
        }
        return count;
    }

    public String[][] createTable(String specificBrand) {
        ArrayList<I18nRowMap> map = getRowMap(specificBrand);

        int numLangs = countTotalLanguages(specificBrand);

        String[][] data = new String[map.size()][numLangs + 2];

        //int statNumEmptyCells = 0;
        for (int c = 0; c < numLangs; ++c) {
            I18nLanguage lang = getLanguageBySortedIndex(c, specificBrand);
            int r = 0;
            for (I18nRowMap row : map) {
                String value = lang.getRowDisplayValue(row);
                data[r][0] = row.component;
                data[r][1] = row.getBeautifulKey();
//                if (value == null || value.isBlank() || value.isEmpty()) ; //++statNumEmptyCells
                data[r][c + 2] = value;
                ++r;
            }
        }
        return data;
    }

    public I18nLanguage getLanguageBySortedIndex(int index, String specificBrand) {
        int i = 0;
        for (I18nBrand brand : brands) {
            if (specificBrand != null && !specificBrand.equals(brand.name)) continue;
            int langIndex = index - i;
            if (brand.languages.size() > langIndex) {
                return brand.languages.get(langIndex);
            }
            i += brand.languages.size();
        }
        return null;
    }

    public LanguageIdentifier[] getHeader(String specificBrand) {
        int numLangs = countTotalLanguages(specificBrand);
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

    public void removeAllDuplicates(I18nCSB other) {
        for (I18nBrand otherBrand : other.brands) {
            int lastIndex = brands.size() - 1;
            for (int brandIdx = lastIndex; brandIdx >= 0; --brandIdx) {
                brands.get(brandIdx).removeAllDuplicates(otherBrand);
                if (brands.get(brandIdx).isEmpty()) {
                    brands.remove(brandIdx);
                }
                break;
            }
        }
    }

    public LanguageTable createLanguageTable() {
        String[][] data = createTable(null);
        LanguageIdentifier[] header = getHeader(null);

        LanguageTable languageTable = new LanguageTable(header, data);

        return languageTable;
    }
}
