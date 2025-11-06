package translations;

import core.StringHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class I18n implements Comparable<I18n> {

    public ArrayList<I18nData> json = new ArrayList<I18nData>();
    public String component;
    public String key;
    public I18nCompareResult compareResult;


    private I18n() {
    } // made private so you have to use the other constructor

    public I18n(I18n other)
    {
        as(other);
    }

    public I18n(String component_, String key_, String value_) {
        component = component_;
        int splitIndex = key_.indexOf(".");

        I18nData newData = new I18nData();
        if (splitIndex != -1) {
            String[] split = key_.split("\\.");

            ArrayList<String> validStrings = new ArrayList<>();
            for (String str : split) {
                if (StringHelper.isValid(str)) {
                    validStrings.add(str);
                }
            }
            if (validStrings.size() == 1 || !StringHelper.isValid(validStrings.get(1))) {
                key = key_;
                newData.key = "";
                newData.value = value_;
                newData.compareResult = I18nCompareResult.New;

            } else {
                key = validStrings.get(0);
                newData.key = validStrings.get(1);
                newData.value = value_;
                newData.compareResult = I18nCompareResult.New;
            }
        } else {
            key = key_;
            newData.key = "";
            newData.value = value_;
            newData.compareResult = I18nCompareResult.New;
        }

        json.add(newData);
    }

    public boolean isJSON() {
        return !json.isEmpty() && StringHelper.isValid(json.get(0).key);
    }

    public void as(I18n other) {
        component = other.component;
        key = other.key;
        compareResult = other.compareResult;

        json.clear();
        for (I18nData data : other.json) {
            json.add(new I18nData(data));
        }
    }


    I18nData find(String key_) {
        for (I18nData innerData : json) {
            if (innerData.key.equals(key_)) {
                return innerData;
            }
        }
        return null;
    }

    void remove(String key_) {
        int i = 0;
        for (I18nData innerData : json) {
            if (innerData.key.equals(key_)) {
                json.remove(i);
                return;
            }
            ++i;
        }
    }

    void sanitize() {
        I18nData unset = null;
        for (I18nData temp : json) {
            if (temp.key == null || temp.key.isBlank()) {
                unset = temp;
                break;
            }
        }
        if (unset != null) {
            for (I18nData temp : json) {
                if (temp.key != null && !temp.key.isBlank()) {
                    if (temp.value.equals(unset.value)) {
                        json.remove(unset);
                        break;
                    }
                }
            }
        }
    }

    public I18nResult addOrOverride(I18n other, boolean bOverride) {
        if (component.equals(other.component) && key.equals(other.key)) {
            I18nResult result = I18nResult.AlreadyExists;
            for (I18nData otherData : other.json) {
                I18nData cur = find(otherData.key);

                if (cur != null) {
                    if (!StringHelper.isValid(cur.value)) {
                        cur.value = otherData.value;
                    } else if (bOverride) {
                        cur.value = otherData.value;
                    }
                    otherData.compareResult = I18nCompareResult.Override;
                    result = I18nResult.Overridden;
                } else {
                    I18nData copy = new I18nData(otherData);
                    json.add(copy);
                    if (result != I18nResult.Overridden) {
                        result = I18nResult.Added;
                    }
                }
            }
            return result;
        }
        return I18nResult.NotFound;
    }

    public ArrayList<I18nData> getJSONSorted(boolean bSkipEmpty) {
        json.sort((a, b) -> {
            return a.key.compareTo(b.key);
        });
        return json;
    }

    public boolean isValid(boolean bSkipEmptyValues) {
        if (!bSkipEmptyValues) {
            return !json.isEmpty();
        }
        for (I18nData innerData : json) {
            if (StringHelper.isValid(innerData.value))
                return true; // this is fucked up but we check wether just one value is actually valid in json array.
        }
        return false;
    }

    public void print() {
        for (I18nData innerData : json) {
            System.out.println("component: " + component + ", key: " + key + "." + innerData.key + ", value:" + innerData.value);
        }
    }

    @Override
    public int compareTo(I18n other) {
        int prec = component.compareTo(other.component);
        if (prec != 0) {
            return prec;
        }
        return key.compareTo(other.key);
    }

    public void removeJsonDuplicates(I18n other) {
        for (I18nData otherData : other.json) {
            I18nData thisData = find(otherData.key);

            if (thisData != null) {
                if (thisData.value.equals(otherData.value)) {
                    remove(otherData.key);
//                    System.out.println("remove: " + otherData.value + ", key: " + otherData.key);
                } else {
                    compareResult = I18nCompareResult.Override;
                }
            }
        }
    }
}
