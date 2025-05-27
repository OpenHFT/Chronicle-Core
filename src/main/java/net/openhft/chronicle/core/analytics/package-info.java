/**
 * Provides functionality for reporting analytics to an upstream receiver, primarily Google Analytics.
 *
 * <p>The {@link net.openhft.chronicle.core.analytics.AnalyticsFacade} is an interface which is designed
 * to provide a means for libraries to report analytics to an upstream receiver. It provides a best-effort
 * mechanism for propagating events to Google Analytics. This interface has methods for sending events
 * with or without additional parameters and also for enabling or disabling analytics reporting.
 *
 * <p>Furthermore, it has an internal builder that can be used to build an instance of AnalyticsFacade with
 * custom configurations like frequency limits, custom loggers for error and debug messages, custom URL for
 * Google Analytics, and more.
 *
 * <p>This package is part of the Chronicle Core library which provides utilities and low-level support
 * for higher-level components.
 *
 * @see net.openhft.chronicle.core.analytics.AnalyticsFacade
 *
 * <p>Unless otherwise stated, parameters and return values in this package are
 * non-null by default. Use {@link org.jetbrains.annotations.Nullable} to
 * indicate when {@code null} is permitted.
 */
package net.openhft.chronicle.core.analytics;
