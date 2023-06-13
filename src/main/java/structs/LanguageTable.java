package structs;

public class LanguageTable
{
	private LanguageIdentifier[] identifiers = null;
	private String[][] data = null;

	public LanguageTable(LanguageIdentifier[] inIdentifiers, String[][] data)
	{
		this.identifiers = inIdentifiers;
		this.data = data;
	}

	public LanguageIdentifier[] getIdentifiers()
	{
		return identifiers;
	}

	public int getIdentifierIndex(LanguageIdentifier otherIdentifier)
	{
		for (int i = 0; i < identifiers.length; ++i)
		{
			if (identifiers[i].brand.equals(otherIdentifier.brand) && identifiers[i].locale.equals(otherIdentifier.locale)) return i;
		}
		return -1;
	}

	public String[] getJTableHeader()
	{
		String[] arr = new String[identifiers.length + 2];
		arr[0] = "Component";
		arr[1] = "Key";
		for (int i = 0; i < identifiers.length; ++i)
		{
			arr[i + 2] = identifiers[i].locale;
		}
		return arr;
	}

	public String[][] getJTableData()
	{
		return data;
	}
}
