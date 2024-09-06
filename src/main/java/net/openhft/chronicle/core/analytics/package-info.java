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
 */
package net.openhft.chronicle.core.analytics;
