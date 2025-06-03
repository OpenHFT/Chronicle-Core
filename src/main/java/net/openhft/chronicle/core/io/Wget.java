/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static net.openhft.chronicle.core.io.Closeable.closeQuietly;

/**
 * Minimal helper for issuing a blocking HTTP GET request. It simply wraps
 * {@link URL#openStream()} and copies the response into a {@link StringBuilder}.
 * <p>
 * No redirects or status codes are handled. It is intended for small test
 * utilities rather than production use.
 */
public final class Wget {

    private Wget() {
    }

    /**
     * Reads the content of the given URL into the provided builder.
     * The call blocks until the entire response has been read.
     *
     * @param url textual representation of the HTTP URL
     * @param sb  container for the characters returned by the server
     * @throws IOException if the connection fails or the stream cannot be read
     */
    public static void url(String url, StringBuilder sb) throws IOException {
        InputStream is = null;
        try {
            is = new URL(url).openStream();
            String s;

            try (BufferedReader d = new BufferedReader(new InputStreamReader(is))) {
                while ((s = d.readLine()) != null) {
                    sb.append(s);
                }
            }
        } finally {
            closeQuietly(is);
        }
    }
}
