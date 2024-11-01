package JSON;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import translations.I18n;
import translations.I18nBrand;

public class JSONReader {

    public void startReading(String locale, File file) {
        try {
            InputStream fis = new FileInputStream(file);
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject parent = new JSONObject(tokener);

            I18nBrand dict = new I18nBrand();
            for (Object componentName : parent.names()) {
                JSONObject component = (JSONObject) parent.get(componentName.toString());

                for (Object keyName : component.names()) {
                    I18n i18n = new I18n();
                    i18n.component = componentName.toString();
                    i18n.key = keyName.toString();
                    i18n.value = component.get(i18n.key).toString();
                    boolean bAdded = dict.add(locale, i18n);
                }
            }
            dict.sort();
            //dict.print();
        } catch (Exception e) {
            System.out.print(e);
        }
    }
}
