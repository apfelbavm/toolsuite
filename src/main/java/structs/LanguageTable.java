package structs;

public class LanguageTable
{
	private String[] locales = null;
	private String[][] data = null;

	public LanguageTable(String[] locales, String[][] data)
	{
		this.locales = locales;
		this.data = data;
	}

	public String[] getLocales()
	{
		return locales;
	}

	public int getLocaleIndex(String locale)
	{
		for (int i = 0; i < locales.length; ++i)
		{
			if (locales[i].equals(locale)) return i;
		}
		return -1;
	}

	public String[] getJTableHeader()
	{
		String[] arr = new String[locales.length + 2];
		arr[0] = "Component";
		arr[1] = "Key";
		for (int i = 0; i < locales.length; ++i)
		{
			arr[i + 2] = locales[i];
		}
		return arr;
	}

	public String[][] getJTableData()
	{
		return data;
	}
}
