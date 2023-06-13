package core;

public class TranslationMgrFlags {
	public enum Import {
		APPEND_BRAND_TO_LOCALE, USE_HYPERLINK_IF_AVAILABLE
	}

	public enum Export {
		CONCAT_COMPONENT_AND_KEY, DONT_EXPORT_EMPTY_VALUES
	}

	public enum FolderNaming {
		BRAND_LOCALE, LOCALE_BRAND, BRAND_AND_LOCALE_AS_SUBFOLDER;

		public static FolderNaming getValue(int i) {
			int index = 0;
			for (FolderNaming rule : FolderNaming.values()) {
				if (i == index)
					return rule;
				++index;
			}
			return FolderNaming.LOCALE_BRAND;
		}
	}
}
