package translations;

public class I18nData {
    public String workbook;
    public String sheet;
    public String key;
    public String value;
    public I18nCompareResult compareResult;

    public void as(I18nData other) {
        workbook = other.workbook;
        sheet = other.sheet;
        key = other.key;
        value = other.value;
        compareResult = other.compareResult;
    }
}
