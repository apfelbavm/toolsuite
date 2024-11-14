package widgets.table;

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


	public String[][] getJTableData()
	{
		return data;
	}
}
