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
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ChronicleFeature} indicates a compile-time switch recognised by
 * Chronicle libraries. It is aimed at library maintainers configuring optional
 * behaviour.
 * The {@code int} value represents a feature id or bit mask defined by
 * Chronicle components.
 *
 * <p><b>Retention and effect:</b> Retained in the class file and discarded by
 * the Java runtime. It has no runtime effect unless Chronicle tooling
 * recognises the value.</p>
 *
 * <pre>
 * {@code @ChronicleFeature(1)}
 * public interface CustomFeature { }
 * </pre>
 *
 * <pre>
 * if (ChronicleFeatures.FEATURE_X.isPresent()) {
 *     // feature specific logic
 * }
 * </pre>
 *
 * <p>This annotation is not inherited and must be repeated on subclasses if
 * required.</p>
 *
 * @see TargetMajorVersion
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ChronicleFeature {
    int value();
}
