package translations;

public class I18n implements Comparable<I18n> {
    public String workbook;
    public String sheet;
    public int row;
    public int cell;
    public String component;
    public String key;
    public String value;

    public void as(I18n other) {
        workbook = other.workbook;
        sheet = other.sheet;
        row = other.row;
        cell = other.cell;
        component = other.component;
        key = other.key;
        value = other.value;
    }

    public boolean isLocationColliding(I18n other) {
        return workbook.equals(other.workbook) && sheet.equals(other.sheet) && row == other.row && cell == other.cell;
    }

    public boolean isValueColliding(I18n other) {
        return component.equals(other.component) && key.equals(other.key) && !value.equals(other.value);
    }

    public void print() {
        System.out.println("workbook: " + workbook + ", sheet: " + sheet + ", component: " + component + ", key: " + key + ", value: " + value);
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
