package translations;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class I18n implements Comparable<I18n> {
    public String workbook;
    public String sheet;
    public String component;
    public String key;
    public String value;
    public JSONObject json;
    public boolean bIsJSON;

    private I18n() {
    } // made private so you have to use the other constructor

    public I18n(String workbook_, String sheet_, String component_, String key_, String value_) {
        workbook = workbook_;
        component = component_;

        int splitIndex = key_.indexOf(".");

        if (splitIndex > 0) {
            bIsJSON = true;
            json = new JSONObject();
            String[] split = key_.split("\\.");
            if (split[1] == null || split[1].isBlank() || split[1].isEmpty()) {
                bIsJSON = false;
                key = key_;
                value = value_;
            } else {
                key = split[0];
                json.put(split[1], value_);
            }
        } else {
            bIsJSON = false;
            key = key_;
            value = value_;
        }
    }

    public void as(I18n other) {
        workbook = other.workbook;
        sheet = other.sheet;
        component = other.component;
        key = other.key;
        value = other.value;
        json = other.json;
        bIsJSON = other.bIsJSON;
    }

    public I18nResult addOrOverride(I18n other, boolean bOverride) {
        if (component.equals(other.component) && key.equals(other.key)) {
            if (bIsJSON && other.bIsJSON) {
                I18nResult result = I18nResult.AlreadyExists;
                for (Object entry : other.json.names()) {
                    String entryString = entry.toString();

                    if (!json.has(entryString)) {
                        json.put(entryString, other.json.get(entryString).toString());
                        if (result != I18nResult.Overridden) {
                            result = I18nResult.Added;
                        }
                    } else if (bOverride) {
                        json.put(entryString, other.json.get(entryString).toString());
                        result = I18nResult.Overridden;
                    }
                }
                return result;
            } else if (bOverride) {
                value = other.value;
                return I18nResult.Overridden;
            } else {
                return I18nResult.AlreadyExists;
            }
        }
        return I18nResult.NotFound;
    }

    public TreeMap<String, String> getJSONSorted() {
        TreeMap<String, String> tree = new TreeMap<String, String>();
        for (Object entry : json.names()) {
            String k = entry.toString();
            String v = json.get(k).toString();
            tree.put(k, v);
        }
        return tree;
    }

    public boolean isValid() {
        return (bIsJSON || (value != null && !value.isBlank() && !value.isEmpty()));
    }

    public void print() {
        if (bIsJSON) {
            System.out.println("workbook: " + workbook + ", sheet: " + sheet + ", component: " + component);
        } else {

            System.out.println("workbook: " + workbook + ", sheet: " + sheet + ", component: " + component + ", key: " + key + ", value: " + value);
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
}
