package net.openhft.chronicle.core.internal.analytics.http;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public enum HttpUtil {
    ;

    private static final boolean OUTPUT = Jvm.getBoolean("chronicle.analytics.output");

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "analytics-http-client");
        thread.setDaemon(true);
        return thread;
    });


    public static void send(@NotNull final String urlString, @NotNull final String json) {
        EXECUTOR.execute(new Sender(urlString, json));
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
                //conn.setRequestProperty("Content-Type", "application/json; utf-8"); // For some reason, this does not work...
                conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
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
                    if (OUTPUT)
                        System.out.println(response.toString());
                }

            } catch (IOException ioe) {
                if (OUTPUT)
                    Jvm.warn().on(HttpUtil.class, ioe);
            }
        }
    }

}