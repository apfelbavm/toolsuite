package translations;

public class I18nRowMap implements Comparable<I18nRowMap> {
    String component;
    String key;

    public I18nRowMap(String component_, String key_)
    {
        component = component_;
        key = key_;
    }
    @Override
    public int compareTo(I18nRowMap other) {
        int prec = component.compareTo(other.component);
        if (prec != 0) {
            return prec;
        }
        return key.compareTo(other.key);
    }
}
