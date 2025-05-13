package core;

import java.util.regex.Pattern;

public class StringHelper {
    /* 1. converts '-' to '_';
     * 2. converts 'myWord' to 'my_word' * */
    public static String convertToSnakeCase(String value) {
        StringBuilder result = new StringBuilder();
        boolean bLastIsUnderscore = false;
        for (char c : value.toCharArray()) {
            if (c == '-') {
                result.append("_");
                bLastIsUnderscore = true;
            } else {
                if (Character.isUpperCase(c)) {
                    if (!bLastIsUnderscore) {
                        result.append("_");
                    }
                    result.append(Character.toLowerCase(c));
                    bLastIsUnderscore = false;
                } else {
                    bLastIsUnderscore = c == '_';
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    /* Checks if 'str' is not null and is not blank. * */
    public static boolean isValid(String str) {
        if (str == null) return false;
        return !str.isBlank();
    }

    public static String fixString(String str) {
        StringBuilder result = new StringBuilder();

        boolean bPreviousWasSpace = false;

        str = str.trim().replaceAll("\\\\(?![nrt])", ""); // remove all escape characters except for escape \n, \r, \t
        for (char c : str.toCharArray()) {
            if (c == '\n' || c == '\r') {
                result.append(' ');
                bPreviousWasSpace = true;
            } else if (c == '"') {
                result.append('\\');
                result.append('"');
                bPreviousWasSpace = false;
            } else if (c == ' ') {
                if (!bPreviousWasSpace) {
                    result.append(' ');
                }
                bPreviousWasSpace = true;
            } else {
                result.append(c);
                bPreviousWasSpace = false;
            }
        }
        return result.toString();
    }

    // OLD METHOD, CHECK WETHER NEW GIVES THE SAME OUTPUT
//    public static String fixString(String str) {
//        String value = str.replace("\n", " ").replace("\r", " ").replace(System.getProperty("line.separator"), " ");
//        // Replace double spacebar
//        value = value.replace("\"", "\\\"");
//        value = value.replaceAll("( )+", " ");
//        return value.trim();
//    }
}
