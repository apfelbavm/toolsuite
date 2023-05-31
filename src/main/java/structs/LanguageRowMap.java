package structs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LanguageRowMap
{
	public String[][] rowMap;
	private HashMap<String, HashSet<String>> tree = new HashMap<String, HashSet<String>>(64);

	public void buildRowMap()
	{
		int size = 0;
		for (Set<String> components : tree.values())
		{
			size += components.size();
		}
		rowMap = new String[size][2];
		int i = 0;
		for (HashMap.Entry<String, HashSet<String>> components : tree.entrySet())
		{
			String component = components.getKey();
			for (String key : components.getValue())
			{
				rowMap[i][0] = component;
				rowMap[i][1] = key;
				++i;
			}
		}
		quickSort(0, rowMap.length - 1);
	}

	private void quickSort(int low, int high)
	{
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		String[] rowPivot = rowMap[low + (high - low) / 2];

		// Divide into two lists
		while (i <= j)
		{
			// If the current value from the left list is smaller than the pivot
			// element then get the next element from the left list
			while ((rowMap[i][0].compareTo(rowPivot[0]) == 0 && rowMap[i][1].compareTo(rowPivot[1]) < 0) || rowMap[i][0].compareTo(rowPivot[0]) < 0)
			{
				i++;
			}
			// If the current value from the right list is larger than the pivot
			// element then get the next element from the right list
			while ((rowMap[j][0].compareTo(rowPivot[0]) == 0 && rowMap[j][1].compareTo(rowPivot[1]) > 0) || rowMap[j][0].compareTo(rowPivot[0]) > 0)
			{
				j--;
			}

			// If we have found a value in the left list which is larger than
			// the pivot element and if we have found a value in the right list
			// which is smaller than the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j)
			{
				exchange(i, j);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j) quickSort(low, j);
		if (i < high) quickSort(i, high);
	}

	private void exchange(int a, int b)
	{
		String[] row = rowMap[a];
		rowMap[a] = rowMap[b];
		rowMap[b] = row;
	}

	public void addUnique(String component, String key)
	{
		HashSet<String> list = tree.get(component);
		if (list == null)
		{
			list = new HashSet<String>(128);
			list.add(key);
			tree.put(component, list);
		}
		else
		{
			list.add(key);
		}
	}
}
