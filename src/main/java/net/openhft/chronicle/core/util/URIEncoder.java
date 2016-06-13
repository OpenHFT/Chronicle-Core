package net.openhft.chronicle.core.util;

/**
 * simple uri encoder, made from the spec at http://www.ietf.org/rfc/rfc2396.txt
 * Feel free to copy this. I'm not responsible for this code in any way, ever.
 * Thanks to Marco and Thomas
 *
 * @author Daniel Murphy
 */
public class URIEncoder {
    private static final String mark = "-_.!~*'()\"";
    private static final char[] hex = "0123456789ABCDEF".toCharArray();

    public static String encodeURI(String argString) {
        StringBuilder uri = new StringBuilder();

        for (char c : argString.toCharArray()) {
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z') || mark.indexOf(c) != -1) {
                uri.append(c);
            } else {
                appendEscaped(uri, c);
            }
        }
        return uri.toString();
    }

    private static void appendEscaped(StringBuilder uri, char c) {
        if (c <= 0xFF) {
            uri.append("%");
            uri.append(hex[(c >> 4) & 0xF]);
            uri.append(hex[c & 0xF]);
            return;
        }
        // unicode
        uri.append('\\');
        uri.append('u');
        uri.append(hex[(c >> 12) & 0xF]);
        uri.append(hex[(c >> 8) & 0xF]);
        uri.append(hex[(c >> 4) & 0xF]);
        uri.append(hex[c & 0xF]);
    }
}