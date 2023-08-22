/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
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

import net.openhft.chronicle.core.pool.StringBuilderPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static net.openhft.chronicle.core.io.Closeable.closeQuietly;

/**
 * Utility class for performing an HTTP GET request.
 */
public final class Wget {

    private static final StringBuilderPool STRING_BUILDER_POOL = new StringBuilderPool();

    private Wget() {
    }

    /**
     * Performs an HTTP GET request to the specified URL.
     *
     * @param url the URL of the HTTP GET request
     * @return the result of the request as a {@link CharSequence}
     * @throws IOException If an error occurs while establishing the connection
     */
    public static CharSequence url(String url) throws IOException {

        final StringBuilder sb = STRING_BUILDER_POOL.acquireStringBuilder();

        InputStream is = null;
        try {
            is = new URL(url).openStream();
            String s;

            try (BufferedReader d = new BufferedReader(new InputStreamReader(is))) {
                while ((s = d.readLine()) != null) {
                    sb.append(s);
                }
            }
            return sb;
        } finally {
            closeQuietly(is);
        }
    }

}
