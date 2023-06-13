package structs;

import java.util.HashMap;

public class Language {
	public LanguageIdentifier identifier;
	public HashMap<String, HashMap<String, String>> tree = new HashMap<String, HashMap<String, String>>(64);

	public Language(LanguageIdentifier inIdentifier) {
		identifier = inIdentifier;
	}

	public boolean isSameLanguageIdentifier(Language otherLanguage) {
		return identifier.brand.equalsIgnoreCase(otherLanguage.identifier.brand)
				&& identifier.locale.equalsIgnoreCase(otherLanguage.identifier.locale);
	}


	public void addUnique(String component, String key, String value) {
		HashMap<String, String> keyMap = tree.get(component);
		if (keyMap != null) {
			if (keyMap.containsKey(key))
				return;
			keyMap.put(key, value);
		} else {
			keyMap = new HashMap<String, String>(128);
			keyMap.put(key, value);
			tree.put(component, keyMap);
		}
	}

	public String findValue(String component, String key) {
		HashMap<String, String> componentMap = tree.get(component);
		if (componentMap == null)
			return "";
		String value = componentMap.get(key);
		if (value == null)
			return "";
		return value;
	}

	public void appendTable(Language other) {
		for (HashMap.Entry<String, HashMap<String, String>> componentMap : other.tree.entrySet()) {
			for (HashMap.Entry<String, String> keyMap : componentMap.getValue().entrySet()) {
				addUnique(componentMap.getKey(), keyMap.getKey(), keyMap.getValue());
			}
		}
	}
}
