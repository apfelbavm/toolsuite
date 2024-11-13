package translations;

import org.json.JSONObject;

public class I18n implements Comparable<I18n> {
    public String workbook;
    public String sheet;
    public String component;
    public String key;
    public String value;
    public JSONObject obj;
    private boolean bIsJSON;

    private I18n() {
    } // made private so you have to use the other constructor

    public I18n(String workbook_, String sheet_, String component_, String key_, String value_) {
        sheet = sheet_;
        component = component_;

        int splitIndex = key_.indexOf(".");

        if (splitIndex > 0) {
            obj = new JSONObject();
            String[] split = key_.split("\\.");
            obj.put(split[0], split[1]);
            bIsJSON = true;
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
        obj = other.obj;
        bIsJSON = other.bIsJSON;
    }

    public I18nResult compareAndMaybeAdd(I18n other, boolean bOverride) {
        if (component.equals(other.component)) {
            if (bIsJSON && other.bIsJSON) {
                I18nResult result = I18nResult.AlreadyExists;
                for (Object entry : other.obj.names()) {
                    String entryString = entry.toString();
                    if (!obj.has(entryString)) {
                        obj.put(entryString, other.obj.get(entryString).toString());
                        if (!result.equals(I18nResult.Overridden)) {
                            result = I18nResult.Added;
                        }
                    } else if (bOverride) {
                        result = I18nResult.Overridden;
                    }
                }
            } else if (!key.equals(other.key)) {
                value = other.value;
                return I18nResult.Added;
            } else if (bOverride) {
                value = other.value;
                return I18nResult.Overridden;
            } else {
                return I18nResult.AlreadyExists;
            }
        }
        return I18nResult.NotFound;
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
