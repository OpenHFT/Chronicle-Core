package net.openhft.chronicle.core.internal.analytics.http;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public enum HttpUtil {;

    public static void send(@NotNull final String urlString, @NotNull final String json) {
        final Thread thread = new Thread(new Sender(urlString, json), "http-client-" + urlString);
        thread.setDaemon(true);
        thread.start();
    }

    private static final class Sender implements Runnable {

        private final String urlString;
        private final String json;

        public Sender(@NotNull final String urlString, @NotNull final String json) {
            this.urlString = urlString;
            this.json = json;
        }

        @Override
        public void run() {
            try {
                final URL url = new URL(urlString);
                final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    final byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    final StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }

            } catch (IOException ignore) {
                // Silently ignore
            }
        }
    }

}