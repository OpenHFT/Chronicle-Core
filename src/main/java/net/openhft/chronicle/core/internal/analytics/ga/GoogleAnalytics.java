package net.openhft.chronicle.core.internal.analytics.ga;


import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.analytics.Analytics;
import net.openhft.chronicle.core.internal.analytics.http.HttpUtil;
import org.jetbrains.annotations.NotNull;

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

public final class GoogleAnalytics implements Analytics {

    private final String COOKIE_FILE_NAME = "software.chronicle.client.id";
    private static final String ENDPOINT_URL = "https://www.google-analytics.com/mp/collect";
    private static final String API_SECRET = "IeQiD0YvRCaKRZ52vUV79A";
    private static final String START_EVENT_NAME = "start";

    private final String libraryName;
    private final String version;
    private final String clientId;
    private final String measurementId;

    public GoogleAnalytics(@NotNull final String libraryName, @NotNull final String version) {
        this.libraryName = libraryName;
        this.version = version;
        this.clientId = acquireClientId();
        this.measurementId = measurementId(libraryName);
    }

    @Override
    public void onStart() {
        httpSend(START_EVENT_NAME);
    }

    @Override
    public void onFeature(@NotNull final String id) {
        httpSend(id);
    }

    private void httpSend(@NotNull String eventName) {
        final String url = ENDPOINT_URL + "&measurement_id=" + urlEncode(measurementId) + "?api_secret=" + urlEncode(API_SECRET);
        final String json = jsonFor(eventName, userProperties());
        HttpUtil.send(url, json);
    }

    private String jsonFor(@NotNull final String eventName, @NotNull final Map<String, Object> additionalProperties) {
        return Stream.of(
                "{",
                jsonElement(" ", "clientId", clientId) + ",",
                jsonElement(" ", "nonPersonalizedAds", true) + ",",
                " " + asElement("events") + ": [{",
                jsonElement("  ", "name", eventName) + ",",
                "  " + asElement("params") + ": {",
                jsonElement("   ", "version", version),
                "  }",
                " }],",
                " " + asElement("userProperties") + ": {",
                additionalProperties.entrySet().stream().map(this::userProperty).collect(Collectors.joining(String.format(",%n"))),
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
                entry("available.processors", Runtime.getRuntime().availableProcessors()),
                entry("max.memory", Runtime.getRuntime().maxMemory())
        )
                .filter(e -> e.getValue() != null)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    private static Map.Entry<String, String> entryFor(final String systemProperty) {
        return new AbstractMap.SimpleImmutableEntry<>(systemProperty, System.getProperty(systemProperty));
    }

    private static Map.Entry<String, Object> entry(final String key, final Object value) {
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

    private String measurementId(final String libraryName) {
        switch (libraryName) {
            case "map":
                return "G-E0S89S7N6Z";
            case "core":
                return "G-E0S89S7N6Z";
        }
        // Unknown library
        return "G-E0S89S7N6Z";
    }

    private String nl() {
        return String.format("%n");
    }


}