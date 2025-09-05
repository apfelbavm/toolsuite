package translations;

import core.StringHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class I18n implements Comparable<I18n> {

    public I18nData data;
    ArrayList<I18nData> json;
    //    public String workbook;
//    public String sheet;
    public String component;
    //    public String key;
    //    public String value;
//    public JSONObject json;

    //    public I18nCompareResult compareResult;

    public boolean isJSON() {
        return json != null && !json.isEmpty();
    }

    private I18n() {
    } // made private so you have to use the other constructor

    public I18n(String workbook_, String sheet_, String component_, String key_, String value_) {
//        workbook = workbook_;
        component = component_;
//        compareResult = I18nCompareResult.New;
        data = new I18nData();
        int splitIndex = key_.indexOf(".");

        if (splitIndex > 0) {
            String[] split = key_.split("\\.");
            if (split.length == 1 || split[1] == null || split[1].isBlank() || split[1].isEmpty()) {
                data.workbook = workbook_;
                data.sheet = sheet_;
                data.key = key_;
                data.value = value_;
                data.compareResult = I18nCompareResult.New;

            } else {
                json = new ArrayList<I18nData>();
                data.key = split[0];
                I18nData newData = new I18nData();
                newData.workbook = workbook_;
                newData.sheet = sheet_;
                newData.key = split[1];
                newData.value = value_;
                newData.compareResult = I18nCompareResult.New;

                json.add(newData);
            }
        } else {
            data.workbook = workbook_;
            data.sheet = sheet_;
            data.key = key_;
            data.value = value_;
            data.compareResult = I18nCompareResult.New;
        }
    }

    public void as(I18n other) {
        data.workbook = other.data.workbook;
        data.sheet = other.data.sheet;
        component = other.component;
        data.key = other.data.key;
        data.value = other.data.value;
        json = other.json;
    }

    boolean has(String key_) {
        if (isJSON()) {

            for (I18nData innerData : json) {
                if (innerData.key.equals(key_)) {
                    return true;
                }
            }
            return false;
        }
        return data.key.equals(key_);
    }

    I18nData find(String key_) {
        if (isJSON()) {
            for (I18nData innerData : json) {
                if (innerData.key.equals(key_)) {
                    return innerData;
                }
            }
            return null;
        }
        return data;
    }

    void remove(String key_) {
        if (isJSON()) {
            int i = 0;
            for (I18nData innerData : json) {
                if (innerData.key.equals(key_)) {
                    json.remove(i);
                    return;
                }
                ++i;
            }
        }
    }

    public I18nResult addOrOverride(I18n other, boolean bOverride) {
        if (component.equals(other.component) && data.key.equals(other.data.key)) {
            if (isJSON() && other.isJSON()) {
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
                        json.add(otherData);
                        if (result != I18nResult.Overridden) {
                            result = I18nResult.Added;
                        }
                    }
                }
                return result;
            } else if (bOverride) {
                if (!StringHelper.isValid(other.data.value)) return I18nResult.AlreadyExists;
                if (StringHelper.isValid(data.value)) return I18nResult.AlreadyExists;
                data.value = other.data.value;
                data.compareResult = I18nCompareResult.Override;
                return I18nResult.Overridden;
            } else {
                return I18nResult.AlreadyExists;
            }
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
        if (isJSON()) {
            for (I18nData innerData : json) {
                if (StringHelper.isValid(innerData.value))
                    return true; // this is fucked up but we check wether just one value is actually valid in json array.
            }
            return false;
        }
        return StringHelper.isValid(data.value);
    }

    public void print() {
        if (isJSON()) {
            System.out.println("workbook: " + data.workbook + ", sheet: " + data.sheet + ", component: " + component);
            for (I18nData innerData : json) {
                System.out.println(" -> key: " + data.key + "." + innerData.key + ", value:" + innerData.value);
            }
        } else {
            System.out.println("workbook: " + data.workbook + ", sheet: " + data.sheet + ", component: " + component + ", key: " + data.key + ", value: " + data.value);
        }
    }

    @Override
    public int compareTo(I18n other) {
        int prec = component.compareTo(other.component);
        if (prec != 0) {
            return prec;
        }
        return data.key.compareTo(other.data.key);
    }

    public void removeJsonDuplicates(I18n other) {
        for (I18nData otherData : other.json) {
            I18nData innerData = find(otherData.key);
            if (innerData != null) {
                if (innerData.value.equals(otherData.value)) {
                    remove(otherData.key);
                } else {
                    data.compareResult = I18nCompareResult.Override;
                }
            }
        }
    }
}
