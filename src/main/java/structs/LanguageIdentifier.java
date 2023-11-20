package structs;

import java.util.ArrayList;

public class LanguageIdentifier {
	public String brand;
	public String locale;

	public LanguageIdentifier(String inBrand, String inLocale) {
		brand = inBrand;
		locale = inLocale;
	}
	
	public static void sortHeader(LanguageIdentifier[] identifiers, ArrayList<Language> languages)
	{
		sort(0, identifiers.length - 1, identifiers, languages);
	}

	private static void sort(int l, int r, LanguageIdentifier[] identifiers, ArrayList<Language> languages)
	{
		if (l < r)
		{
			int q = (l + r) / 2;

			sort(l, q, identifiers, languages);
			sort(q + 1, r, identifiers, languages);
			mergeSort(l, q, r, identifiers, languages);
		}
	}

	private static void mergeSort(int l, int q, int r, LanguageIdentifier[] identifiers, ArrayList<Language> languages)
	{
		LanguageIdentifier[] identifierCopy = new LanguageIdentifier[identifiers.length];
		Language[] dataCopy = new Language[languages.size()];
		int i, j;
		for (i = l; i <= q; i++)
		{
			identifierCopy[i] = identifiers[i];
			dataCopy[i] = languages.get(i);
		}
		for (j = q + 1; j <= r; j++)
		{
			int o = r + q + 1 - j;
			identifierCopy[o] = identifiers[j];
			dataCopy[o] = languages.get(j);
		}
		i = l;
		j = r;
		for (int k = l; k <= r; k++)
		{
			LanguageIdentifier one = identifierCopy[i];
			LanguageIdentifier two = identifierCopy[j];
			boolean bPreceedingBrand = one.brand.compareTo(two.brand) < 0;
			boolean bSameBrand = one.brand.compareTo(two.brand) == 0;
			boolean bPreceedingLocale = one.locale.compareTo(two.locale) < 0;

			if (bPreceedingBrand || (bSameBrand && bPreceedingLocale))
			{
				identifiers[k] = identifierCopy[i];
				languages.set(k, dataCopy[i]);
				i++;
			}
			else
			{
				identifiers[k] = identifierCopy[j];
				languages.set(k, dataCopy[j]);
				j--;
			}
		}
	}
}
