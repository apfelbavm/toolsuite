package core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import structs.LanguageIdentifier;
import structs.LanguageTable;

public class JsonCreator
{
	private TranslationMgrFlags.FolderNaming folderNamingType;

	public boolean export2Json(LanguageTable languageTable, String outputFolder, String fileName, boolean bMergeComponentAndKey, boolean bSkipEmptyCells,
		TranslationMgrFlags.FolderNaming inFolderNamingType)
	{
		folderNamingType = inFolderNamingType;
		// we have only one line off error message, thus we just have to return wether
		// there was an error, the error is already printed and shouldnt be overriden by
		// the success message if succeeding exports were successfull.
		for (LanguageIdentifier identifier : languageTable.getIdentifiers())
		{
			if (bMergeComponentAndKey)
			{
				if (!exportSimple(languageTable, outputFolder, fileName, identifier, bSkipEmptyCells)) return false;
			}

			else
			{
				if (!exportAdvanced(languageTable, outputFolder, fileName, identifier, bSkipEmptyCells)) return false;
			}
		}
		return true;
	}

	private boolean exportAdvanced(LanguageTable languageTable, String outputFolder, String fileName, LanguageIdentifier identifier, boolean skipEmptyCells)
	{
		String[][] data = languageTable.getJTableData();
		int langIndex = languageTable.getIdentifierIndex(identifier);
		if (langIndex == -1) return false;
		// skip component and key columns;
		langIndex += 2;
		try
		{
			String pathToCreate = createOutputFolder(outputFolder, identifier);
			if (pathToCreate == null)
			{
				return false;
			}
			// We need this filewriter to allow Umlauts
			Writer writer = new OutputStreamWriter(new FileOutputStream(pathToCreate + fileName + ".json"), StandardCharsets.UTF_8);
			String lastComponent = "";
			boolean isFirstComp = true;
			writer.write("{\n");
			for (String[] row : data)
			{
				String value = row[langIndex];
				if (skipEmptyCells && (value.isBlank() || value.isEmpty())) continue;
				boolean isFirstKeyValue = false;
				if (row[0] != lastComponent)
				{
					// New component
					if (isFirstComp)
					{
						isFirstKeyValue = true;
						isFirstComp = false;
					}
					else
					{
						writer.write("\n    },\n");
					}
					writer.write("    \"" + row[0] + "\": {\n        \"" + row[1] + "\": " + "\"" + value + "\"");
					lastComponent = row[0];
				}
				else
				{
					if (!isFirstKeyValue)
					{
						writer.write(",\n");
					}

					writer.write("        \"" + row[1] + "\": " + "\"" + value + "\"");
				}
			}
			writer.write("\n    }\n}");
			writer.close();
		}
		catch (Exception e)
		{
			App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private boolean exportSimple(LanguageTable languageTable, String outputFolder, String fileName, LanguageIdentifier identifier, boolean skipEmptyCells)
	{
		String[][] data = languageTable.getJTableData();
		int langIndex = languageTable.getIdentifierIndex(identifier);
		if (langIndex == -1) return false;
		// skip component and key columns;
		langIndex += 2;
		try
		{
			String pathToCreate = createOutputFolder(outputFolder, identifier);
			if (pathToCreate == null)
			{
				return false;
			}
			// We need this filewriter to allow Umlauts
			Writer writer = new OutputStreamWriter(new FileOutputStream(pathToCreate + fileName + ".json"), StandardCharsets.UTF_8);
			writer.write("{");
			boolean isFirstItem = false;
			for (String[] row : data)
			{
				String value = row[langIndex];
				if (value.isBlank() || value.isEmpty()) continue;
				if (!isFirstItem)
				{
					writer.write("\n    \"" + row[0] + "_" + row[1] + "\": " + "\"" + value + "\"");
					isFirstItem = true;
				}
				else
				{
					writer.write(",\n    \"" + row[0] + "_" + row[1] + "\": " + "\"" + value + "\"");
				}
			}
			writer.write("\n}");
			writer.close();
		}
		catch (Exception e)
		{
			App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private String createOutputFolder(String outputFolder, LanguageIdentifier identifier)
	{
		String fileSep = System.getProperty("file.separator");
		String path = outputFolder + fileSep;
		switch (folderNamingType)
		{
		case BRAND_AND_LOCALE_AS_SUBFOLDER:
			path += identifier.brand + fileSep + identifier.locale + fileSep;
			break;
		case BRAND_LOCALE:
			path += identifier.brand + "_" + identifier.locale + fileSep;
			break;
		case LOCALE_BRAND:
			path += identifier.locale + "_" + identifier.brand + fileSep;
			break;
		}
		if (!createFolder(path)) return null;

		return path;
	}

	private boolean createFolder(String path)
	{
		Path pathObj = Paths.get(path);
		return createFolder(pathObj);
	}

	private boolean createFolder(Path path)
	{
		if (Files.exists(path)) return true;
		try
		{
			// Create directory doesnt with with sub directories when the parent older is
			// not created yet
			Files.createDirectories(path);
			return true;
		}
		catch (IOException e)
		{
			App.get().setStatus(e.getLocalizedMessage(), App.ERROR_MESSAGE);
			return false;
		}
	}
}
