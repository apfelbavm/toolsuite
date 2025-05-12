package translations;

public class I18nRowMap implements Comparable<I18nRowMap> {
    String component;
    String key;
    String childKey;

    public I18nRowMap(String component_, String key_, String childKey_) {
        component = component_;
        key = key_;
        childKey = childKey_;
    }

    public String getBeautifulKey() {
        if (childKey == null) {
            return key;
        }
        return key + "." + childKey;
    }

    public boolean eq(String component_, String key_, String childKey_) {
        return component.equals(component_) && key.equals(key_) && ((childKey == null && childKey_ == null) || (childKey != null && childKey.equals(childKey_)));
    }

    @Override
    public int compareTo(I18nRowMap other) {
        int prec = component.compareTo(other.component);
        if (prec != 0) {
            return prec;
        }
        prec = key.compareTo(other.key);
        if (prec != 0) {
            return prec;
        }
        if (childKey == null || other.childKey == null) {
            return prec;
        }

        return childKey.compareTo(other.childKey);
    }
}
