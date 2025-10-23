package test;

public class QueryResult {
    public String brand;
    public String locale;
    public int col;
    public int row;

    public QueryResult(String brand_, String locale_) {
        brand = brand_;
        locale = locale_;
        col = -1;
        row = -1;
    }
    public boolean isValid()
    {
        return col != -1 && row != -1;
    }
}
