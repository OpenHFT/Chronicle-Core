package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.pool.StringBuilderPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static net.openhft.chronicle.core.io.Closeable.*;

public class Wget {

    private static StringBuilderPool stringBuilderPool = new StringBuilderPool();

    private Wget() {
    }

    /**
     * performs and http get
     *
     * @param url the url of the http get
     * @return the result, as a string
     * @throws IOException if the connection could not be established
     */
    public static CharSequence url(String url) throws IOException {
        final StringBuilder sb = stringBuilderPool.acquireStringBuilder();
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
