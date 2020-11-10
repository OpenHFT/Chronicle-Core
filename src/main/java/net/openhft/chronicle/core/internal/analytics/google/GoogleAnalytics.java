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
                jsonElement("   ", "app_version", version) + (eventParameters.isEmpty() ? "" : ","),
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
                return create("G-Q699PK0278", "YDkgw-3DR6yMIcGsCGxrBg");
            case "decentred":
                return create("G-MHNQF60609", "lwahZLwuR62lmlhWQO_XYA");
            case "websocket":
                return create("G-664QLSXFND", "CI402HSGRN-ou20YMSTV9Q");
            // Enterprise
            case "wire-enterprise":
                return create("G-FLEY5Z3PTV", "w4HxPDbzR2We_8XoF79BRA");
            case "network-enterprise":
                return create("G-TVNVZ37HC0", "ymz2OTZgQtSUAHEV_fxVjw");
            case "services":
                return create("G-7QVV85K3RG", "u-ED1zpYRPqq4WwuuD25RQ");
            case "services-demo":
                return create("G-4H93Z95F9S", "VmiM3ElBRbaJXl6fskAOqw");
            case "fix":
                return create("G-V9N5NL07WJ", "iCo8d19UQG26zZKHDi5M5A");
            case "fix-demo":
                return create("G-1QJ7EZKQEZ", "HxiuHF85QKyZAJ9dWEHJZQ");
            case "queue-enterprise":
                return create("G-BP94BVC2ZS", "iPRHY5a2TbeBP8GuWaK0rA");
            case "queue-enterprise-demo":
                return create("G-XXR6JRVS5V", "Fz9YiKtmQ1ukSunPIwxfQw");
            case "queue-zero":
                return create("G-H50N0NLQ7C", "2NgRCqTtQx-aR6gwZVpshg");
            case "map-enterprise":
                return create("G-TKNRN0Y813", "SKYi5vinTDGgLuiMKU2UXA");
            case "ring":
                return create("G-C1WV1K8P3C", "MqQm60ViRkO385xUs1zQ9Q");
            case "datagrid":
                return create("G-H0VN4G0ZYL","-hftABXXRB-Z4hL8KrMOPw");
            case "datagrid-demo":
                return create("G-N8NLPDX36X", "BdyxSG6iT6Cc8nWz5YxzrA");
            case "mdd":
                return create("G-FR9BRC8NSW", "FC8RHQpLROOGCCRFiXiKjg");
            case "mdd-demo":
                return create("G-RPPKNQD1PR", "4vpBLpK4QuOd6_3lDfL8sw");
            case "efx":
                return create("G-PK6BJZNRD9", "c6ujn9xNQQqMfu07p8zPhw");
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