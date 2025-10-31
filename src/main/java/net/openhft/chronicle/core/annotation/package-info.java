/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Chronicle Core provides annotations describing constraints and design intent.
 * They help library maintainers and tooling reason about code usage.
 *
 * <ul>
 * <li>{@link net.openhft.chronicle.core.annotation.Negative} &ndash; value &lt; 0</li>
 * <li>{@link net.openhft.chronicle.core.annotation.NonPositive} &ndash; value &lt;= 0</li>
 * <li>{@link net.openhft.chronicle.core.annotation.NonNegative} &ndash; value &gt;= 0</li>
 * <li>{@link net.openhft.chronicle.core.annotation.Positive} &ndash; value &gt; 0</li>
 * <li>{@link net.openhft.chronicle.core.annotation.Range} &ndash; numeric range</li>
 * <li>{@link net.openhft.chronicle.core.annotation.ForceInline} &ndash; request JVM inlining</li>
 * <li>{@link net.openhft.chronicle.core.annotation.HotMethod} &ndash; performance sensitive method</li>
 * <li>{@link net.openhft.chronicle.core.annotation.DontChain} &ndash; skip interface chaining</li>
 * <li>{@link net.openhft.chronicle.core.annotation.UsedViaReflection} &ndash; accessed by reflection</li>
 * <li>{@link net.openhft.chronicle.core.annotation.PackageLocal} &ndash; intentionally package private</li>
 * <li>{@link net.openhft.chronicle.core.annotation.RequiredForClient} &ndash; class name appears on wire</li>
 * <li>{@link net.openhft.chronicle.core.annotation.SingleThreaded} &ndash; not thread safe</li>
 * <li>{@link net.openhft.chronicle.core.annotation.ScopeConfined} &ndash; object scrubbed after use</li>
 * <li>{@link net.openhft.chronicle.core.annotation.ChronicleFeature} &ndash; compile-time feature toggle</li>
 * <li>{@link net.openhft.chronicle.core.annotation.TargetMajorVersion} &ndash; required Java version</li>
 * <li>{@link net.openhft.chronicle.core.annotation.Java9} &ndash; method used on Java&nbsp;9+</li>
 * </ul>
 *
 * <p>Annotations with {@code RetentionPolicy.CLASS} are not visible via
 * standard reflection. Tools may read them from the class file.</p>
 *
 * <p>Design goal: make hidden performance and safety constraints
 * self-documenting without polluting the public API surface.</p>
 */
package net.openhft.chronicle.core.annotation;
