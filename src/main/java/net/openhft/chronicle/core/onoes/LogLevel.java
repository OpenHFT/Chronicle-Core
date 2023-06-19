/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.onoes;

/**
 * LogLevel is an enumeration that defines various levels of logging within an application.
 * These levels allow for granularity and control over what types of messages should be logged.
 *
 * <ul>
 *     <li>{@link #ERROR} - Designates error events that might still allow the application to continue running.</li>
 *     <li>{@link #WARN} - Designates potentially harmful situations which should still allow the application to continue.</li>
 *     <li>{@link #PERF} - Designates performance events that could be used for performance optimization and diagnosis.</li>
 *     <li>{@link #DEBUG} - Designates fine-grained informational events that are most useful to debug an application.</li>
 * </ul>
 */
public enum LogLevel {
    ERROR,
    WARN,
    PERF,
    DEBUG
}

