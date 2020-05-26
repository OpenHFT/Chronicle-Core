/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

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

    public static String encodeURI(@NotNull String argString) {
        @NotNull StringBuilder uri = new StringBuilder();

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

    private static void appendEscaped(@NotNull StringBuilder uri, char c) {
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