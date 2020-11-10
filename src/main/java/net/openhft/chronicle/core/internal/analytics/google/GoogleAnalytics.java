package net.openhft.chronicle.core.internal.analytics.google;


import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.analytics.Analytics;
import net.openhft.chronicle.core.internal.analytics.http.HttpUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static net.openhft.chronicle.core.internal.analytics.google.GoogleAnalytics.Account.create;

public final class GoogleAnalytics implements Analytics {

    private final String COOKIE_FILE_NAME = "software.chronicle.client.id";
    private static final String ENDPOINT_URL = "https://www.google-analytics.com/mp/collect";
    private static final String START_EVENT_NAME = "started";

    private final String libraryName;
    private final String version;
    private final String clientId;
    //private final String measurementId;
    private final Account account;

    public GoogleAnalytics(@NotNull final String libraryName, @NotNull final String version) {
        this.libraryName = libraryName;
        this.version = version;
        this.clientId = acquireClientId();
        //this.measurementId = measurementId(libraryName);
        this.account = account(libraryName);
    }

    @Override
    public void onStart(@NotNull final Map<String, String> eventParameters) {
        httpSend(START_EVENT_NAME, eventParameters);
    }

    @Override
    public void onFeature(@NotNull final String id, Map<String, String> eventParameters) {
        httpSend(id, eventParameters);
    }

    private void httpSend(@NotNull String eventName, Map<String, String> eventParameters) {
        if (account != null) {
            final String url = ENDPOINT_URL + "?measurement_id=" + urlEncode(account.measurmentId()) + "&api_secret=" + urlEncode(account.apiSectret());
            final String json = jsonFor(eventName, eventParameters, userProperties());
            HttpUtil.send(url, json);
        }
    }

    private String jsonFor(@NotNull final String eventName,
                           @NotNull final Map<String, String> eventParameters,
                           @NotNull final Map<String, Object> userProperties) {
        return Stream.of(
                "{",
                jsonElement(" ", "clientId", clientId) + ",",
                jsonElement(" ", "userId", clientId) + ",",
                jsonElement(" ", "nonPersonalizedAds", true) + ",",
                " " + asElement("events") + ": [{",
                jsonElement("  ", "name", eventName) + ",",
                "  " + asElement("params") + ": {",
                jsonElement("   ", "app_version", version) + (eventParameters.isEmpty() ? "" : String.format(",%n")),
                eventParameters.entrySet().stream().map(e -> jsonElement("   ", e.getKey(), e.getValue())).collect(Collectors.joining(String.format(",%n"))),
                "  }",
                " }],",
                " " + asElement("userProperties") + ": {",
                userProperties.entrySet().stream().map(this::userProperty).collect(Collectors.joining(String.format(",%n"))),
                " }",
                "}"
        ).collect(Collectors.joining(nl()));
    }

    private String jsonElement(final String indent,
                               final String key,
                               final Object value) {
        return indent + asElement(key) + ": " + asElement(value);
    }

    private String asElement(final Object value) {
        return
                value instanceof CharSequence
                        ? "\"" + value + "\""
                        : value.toString();

    }

    private String userProperty(final Map.Entry<String, Object> userProperty) {
        return String.format("  %s: {%n %s%n  }", asElement(userProperty.getKey()), jsonElement("   ", "value", userProperty.getValue()));
    }

    private String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            Jvm.rethrow(e);
            throw new RuntimeException();
        }
    }

    private Map<String, Object> userProperties() {
        return Stream.of(
                entryFor("java.runtime.name"),
                entryFor("java.runtime.version"),
                entryFor("os.name"),
                entryFor("os.arch"),
                entryFor("os.version"),
                entry(replaceDotsWithUnderscore("timezone.default"), TimeZone.getDefault().getID()),
                entry(replaceDotsWithUnderscore("available.processors"), Integer.toString(Runtime.getRuntime().availableProcessors())), // Must be strings...
                entry(replaceDotsWithUnderscore("max.memory"), Long.toString(Runtime.getRuntime().maxMemory()))
        )
                .filter(e -> e.getValue() != null)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    private static Map.Entry<String, String> entryFor(@NotNull final String systemProperty) {
        return new AbstractMap.SimpleImmutableEntry<>(replaceDotsWithUnderscore(systemProperty), System.getProperty(systemProperty));
    }

    private static Map.Entry<String, Object> entry(@NotNull final String key, @Nullable final Object value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    // This tries to read a client id from a "cookie" file in the
    // user's home directory. If that fails, a new random clientId
    // is generated and an attempt is made to save it in said file.
    private String acquireClientId() {
        final String userHome = System.getProperty("user.home");
        try {
            final Path path = Paths.get(userHome, COOKIE_FILE_NAME);
            return Files.lines(path, StandardCharsets.UTF_8)
                    .findFirst()
                    .map(UUID::fromString)
                    .orElseThrow(NoSuchElementException::new)
                    .toString();
        } catch (Exception ignore) {
        }
        final String clientId = UUID.randomUUID().toString();
        try {
            final Path path = Paths.get(userHome, COOKIE_FILE_NAME);
            Files.write(path, clientId.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignore) {
        }
        return clientId;
    }

    private Account account(final String libraryName) {
        switch (libraryName) {
            // OSS
            case "map":
                return create("G-TDTJG5CT6G", "J8qsWGHgQP6CLs43mQ10KQ");
            case "queue":
                return create("G-RLL8BHTN1F", "QDg5-erVRauK9P1lX1yz0w");
            case "queue-demo":
                return create("G-E0S89S7N6Z", "IeQiD0YvRCaKRZ52vUV79A");
            case "decentred":
                return create("G-E0S89S7N6Z", "IeQiD0YvRCaKRZ52vUV79A");
            case "websocket":
                return create("G-E0S89S7N6Z", "IeQiD0YvRCaKRZ52vUV79A");

            // Enterprise
            case "wire-enterprise":
                return null;
            case "network-enterprise":
                return null;
            case "services":
                return null;
            case "services-demo":
                return null;
            case "fix":
                return null;
            case "fix-demo":
                return null;
            case "queue-enterprise":
                return null;
            case "queue-enterprise-demo":
                return null;
            case "queue-zero":
                return null;
            case "map-enterprise":
                return null;
            case "ring":
                return null;
            case "datagrid":
                return null;
            case "datagrid-demo":
                return null;
            case "mdd":
                return null;
            case "mdd-demo":
                return null;
            case "efx":
                return null;

        }
        // Unknown library
        return null;
    }

    private String nl() {
        return String.format("%n");
    }

    interface Account {
        String measurmentId();

        String apiSectret();

        static Account create(@NotNull final String measurementId, @NotNull final String apiSecret) {
            return new Account() {
                @Override
                public String measurmentId() {
                    return measurementId;
                }

                @Override
                public String apiSectret() {
                    return apiSecret;
                }

                @Override
                public String toString() {
                    return "(" + measurementId + ", " + apiSecret + ")";
                }
            };
        }
    }

    private static String replaceDotsWithUnderscore(@NotNull final String s) {
        return s.replace('.', '_');
    }

}