/*
 * Copyright 2016-2020 chronicle.software
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

package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an interface should be excluded from being considered by {@code MethodReader} and {@code MethodWriter}
 * when they are scanning and chaining interfaces.
 *
 * <p>This annotation is particularly useful in scenarios where you want certain interfaces to be ignored
 * during the generation or processing of method readers and writers within the Chronicle software stack.
 *
 * <p>For example, applying this annotation prevents the interface from being included in automatic interface chaining logic.
 *
 * <p>Retention policy is set to {@link RetentionPolicy#RUNTIME}, meaning the annotation is available at runtime
 * and can be processed by frameworks or tools during runtime.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * @DontChain
 * public interface ExcludedInterface {
 *     // Methods that should not be chained by MethodReader/Writer
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DontChain {
}
