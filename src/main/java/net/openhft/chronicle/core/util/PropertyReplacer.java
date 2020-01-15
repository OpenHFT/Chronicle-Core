package net.openhft.chronicle.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PropertyReplacer {
    ;

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");

    public static String replaceTokensWithProperties(String expression) {

        StringBuilder result = new StringBuilder(expression.length());
        int i = 0;
        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
        while (matcher.find()) {
            // Strip leading "${" and trailing "}" off.
            result.append(expression.substring(i, matcher.start()));
            String property = matcher.group();
            property = property.substring(2, property.length() - 1);

            //look up property and replace
            String p = System.getProperty(property);
            result.append((p != null) ? p : matcher.group());

            i = matcher.end();
        }
        result.append(expression.substring(i));
        return result.toString();
    }

}
