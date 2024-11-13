package reader;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import translations.I18n;
import translations.I18nLanguage;

public class JSONReader {

    public I18nLanguage read(String locale, File file) {

        I18nLanguage language = new I18nLanguage(locale);
        try {
            InputStream fis = new FileInputStream(file);
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject parent = new JSONObject(tokener);

            for (Object componentName : parent.names()) {
                String componentNameString = componentName.toString();
                JSONObject component = (JSONObject) parent.get(componentNameString);

                if (componentNameString.equals(I18nLanguage.META_STRING)) {
                    for (Object keyName : component.names()) {
                        String keyNameString = keyName.toString();
                        switch (keyNameString) {
                            case I18nLanguage.META_LOCALE_STRING: {
                                language.addMetaLocale(component.get(keyNameString).toString());
                                break;
                            }
                            case I18nLanguage.META_BRAND_STRING: {
                                language.addMetaBrand(component.get(keyNameString).toString());
                                break;
                            }
                        }
                    }
                } else {
                    for (Object keyName : component.names()) {
                        I18n i18n = new I18n("", "", componentName.toString(), keyName.toString(), component.get(keyName.toString()).toString());
                        boolean bAdded = language.add(i18n, false);
                    }
                }
            }
        } catch (Exception e) {
            System.out.print(e);
        }
        return language;
    }
}
