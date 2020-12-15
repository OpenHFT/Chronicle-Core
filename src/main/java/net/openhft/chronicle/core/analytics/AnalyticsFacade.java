package net.openhft.chronicle.core.analytics;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.internal.analytics.MuteBuilder;
import net.openhft.chronicle.core.internal.analytics.ReflectionUtil;
import net.openhft.chronicle.core.internal.analytics.ReflectiveBuilder;
import net.openhft.chronicle.core.internal.analytics.StandardMaps;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;


/**
 * Provides means for libraries to report analytics to an upstream receiver.
 * <p>
 * Analytics instances only provides a best-effort to propagate
 * events to the upstream receiver.
 * <p>
 * Analytics can be turned off buy setting the system property
 * "chronicle.analytics.disable=true" prior to acquiring any Analytics
 * instances.
 */
public interface AnalyticsFacade {

    /**
     * Sends an event to Google Analytics as identified by the provided event {@code name}.
     * <p>
     * Depending on settings and other conditions, the event may or may not be
     * sent upstream. For example, some implementations may
     * send a limited number of upstream events per time unit.
     */
    default void sendEvent(@NotNull String name) {
        sendEvent(name, Collections.emptyMap());
    }

    /**
     * Sends an event to Google Analytics as identified by the provided event {@code name} including
     * the provided {@code additionalEventParameters} in the event.
     * <p>
     * Depending on settings and other conditions, the event may or may not be
     * sent upstream. For example, some implementations may
     * send a limited number of upstream events per time unit.
     */
    void sendEvent(@NotNull String name, @NotNull Map<String, String> additionalEventParameters);

    /**
     * Returns if Analytics is enabled.
     *
     * @return if Analytics is enabled
     */
    static boolean isEnabled() {
        return ReflectionUtil.analyticsPresent() && !Jvm.getBoolean("chronicle.analytics.disable");
    }

    /**
     * Creates and returns a new empty Builder that can be used to create an Analytic instance.
     * <p>
     * The builder can only create one single Analytic instance.
     * <p>
     * If {@link #isEnabled()} returns false, then a NOP builder is returned.
     *
     * @param measurementId to use for reporting
     * @param apiSecret     to use for reporting
     * @return a new empty Builder that can be used to create an Analytic instance
     */
    @NotNull
    static Builder builder(@NotNull final String measurementId, @NotNull final String apiSecret) {
        requireNonNull(measurementId);
        requireNonNull(apiSecret);
        if (isEnabled()) {
            return new ReflectiveBuilder(measurementId, apiSecret);
        } else {
            return MuteBuilder.INSTANCE;
        }
    }

    /**
     * Creates and returns a new Builder that can be used to create an Analytic instance with
     * standard Chronicle settings including pre-configuration as per
     * {@link #standardEventParameters(String)} and {@link #standardUserProperties()}.
     * <p>
     * The builder can only create one single Analytic instance.
     *
     * @param measurementId to use for reporting
     * @param apiSecret     to use for reporting
     * @param appVersion    to add to the event parameters
     * @return a new Builder that can be used to create an Analytic instance with
     * standard Chronicle settings including pre-configuration as per
     * {@link #standardEventParameters(String)} and {@link #standardUserProperties()}
     * @see #standardEventParameters(String) ()
     * @see #standardUserProperties()
     */
    @NotNull
    static Builder standardBuilder(@NotNull final String measurementId,
                                   @NotNull final String apiSecret,
                                   @NotNull final String appVersion) {
        requireNonNull(measurementId);
        requireNonNull(apiSecret);
        requireNonNull(appVersion);
        final Builder builder = builder(measurementId, apiSecret);
        standardEventParameters(appVersion).forEach(builder::putEventParameter);
        standardUserProperties().forEach(builder::putUserProperty);
        builder.withFrequencyLimit(4, 1, TimeUnit.HOURS); // Let's be gentle
        return builder;
    }

    /**
     * Creates and returns a new map of standard event parameters.
     * The map may contain properties like:
     * <ul>
     *     <li>app_version</li>
     * </ul>
     *
     * @param appVersion to include in the map
     * @return a new map of standard event parameters
     */
    @NotNull
    static Map<String, String> standardEventParameters(@NotNull final String appVersion) {
        requireNonNull(appVersion);
        return StandardMaps.standardEventParameters(appVersion);
    }

    /**
     * Creates and returns a new map of standard user properties.
     * <p>
     * The map may contain properties like:
     * <ul>
     *     <li>java_runtime_name</li>
     *     <li>java_runtime_version</li>
     *     <li>os_name</li>
     *     <li>os_arch</li>
     *     <li>os_version</li>
     *     <li>timezone_default</li>
     *     <li>available_processors</li>
     *     <li>max_memory_gib</li>
     *     <li>java_major_version</li>
     *     <li>max_direct_memory_gib</li>
     * </ul>
     *
     * @return a new map of standard user properties
     */
    @NotNull
    static Map<String, String> standardUserProperties() {
        return StandardMaps.standardUserProperties();
    }

    /**
     * Creates and returns a new map of standard user properties.
     * <p>
     * The map may contain package names.
     */
    static Map<String, String> standardAdditionalProperties() {
        return StandardMaps.standardAdditionalEventParameters();
    }

    interface Builder {

        /**
         * Associates the provided {@code value} with the provided {@code key} in this builder's user properties.
         * If the builder previously contained an association for the key, the old value is replaced
         * by the provided value.
         * <p>
         * The key will be used as a Google Analytics "user property" key with the
         * associated value.
         * </p>
         *
         * @param key   to associate
         * @param value to associate with the key
         * @return this builder
         */
        @NotNull
        Builder putUserProperty(@NotNull String key, @NotNull String value);

        /**
         * Associates the provided {@code value} with the provided {@code key} in this builder's event parameters.
         * If the builder previously contained an association for the key, the old value is replaced
         * by the provided value.
         * <p>
         * The key will be used as a Google Analytics "event parameter" key with the
         * associated value.
         * </p>
         *
         * @param key   to associate
         * @param value to associate with the key
         * @return this builder
         */
        @NotNull
        Builder putEventParameter(@NotNull String key, @NotNull String value);

        /**
         * Limits the frequency by which events can be sent upstream to Google Analytics.
         * <p>
         * Events that are posted within the provided time limit, counted from the time the last
         * message was successfully attempted upstream, are silently dropped.
         * <p>
         * Thus, the highest rate of messages sent upstream can be calculated using the
         * following function {@code
         * <p>
         * 1.0d/timeUnit.toSeconds(duration)
         * <p>
         * }
         * which yields messages per second.
         *
         * @param messages max number of messages to send per duration
         * @param duration minimum duration between upstream messages.
         * @param timeUnit for the provided duration.
         * @return this builder
         */
        @NotNull
        Builder withFrequencyLimit(int messages, long duration, @NotNull TimeUnit timeUnit);

        /**
         * Specifies a custom logger that will receive error messages.
         * For example, failed HTTP communications.
         * <p>
         * The default logger is {@link System#err System.err::println}, i.e. messages will be
         * output to the error console.
         * <p>
         * The actual messages sent to the error logger are unspecified
         * and shall not be acted on by logic.
         *
         * @param errorLogger to use for error messages.
         * @return this builder
         */
        @NotNull
        Builder withErrorLogger(@NotNull Consumer<String> errorLogger);

        /**
         * Specifies a custom logger that will receive debug messages.
         * For example, HTTP messages sent back and forth.
         * <p>
         * The default logger is {@code s -> {}}, i.e. messages will be
         * discarded.
         * <p>
         * The actual messages sent to the debug logger are unspecified
         * and shall not be acted on by logic.
         *
         * @param debugLogger to use for debug messages.
         * @return this builder
         */
        @NotNull
        Builder withDebugLogger(@NotNull Consumer<String> debugLogger);

        /**
         * Specifies a custom file name to use when storing a persistent client id
         * used to identify returning users.
         * <p>
         * By default, a file named "chronicle.analytics.client.id" located
         * in the user's home directory will be used.
         *
         * @param clientIdFileName used to store the persistent client id.
         * @return this builder
         */
        @NotNull
        Builder withClientIdFileName(@NotNull String clientIdFileName);

        /**
         * Specifies a custom URL to use when connecting to Google Analytics.
         * <p>
         * By default, the URL "https://www.google-analytics.com/mp/collect"
         * will be used.
         *
         * @param url used for remote connection
         * @return this builder
         */
        @NotNull
        Builder withUrl(@NotNull String url);

        /**
         * Specifies that reporting shall be made even though
         * JUnit test classes are available to the classloader.
         * <p>
         * By default, no reporting will be done if either of the classes
         * `org.junit.jupiter.api.Test` or `org.junit.Test` are available
         * to the classloader.
         *
         * @return this builder
         */
        @NotNull
        Builder withReportDespiteJUnit();

        /**
         * Creates and returns a new Analytics instance for this Builder.
         *
         * @return a new Analytics instance for this Builder
         */
        @NotNull
        AnalyticsFacade build();
    }

}
