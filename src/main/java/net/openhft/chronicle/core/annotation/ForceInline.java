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
 * {@code @ForceInline} requests that a method or constructor be inlined where
 * possible. It targets library maintainers controlling performance
 * characteristics. The HotSpot JVM ignores custom annotations unless the
 * Chronicle Warmup agent processes the class.
 *
 * <p><b>Retention and effect:</b> Retained at runtime but has no direct effect
 * unless recognised by Chronicle warm-up tooling.</p>
 *
 * <pre>
 * {@code @ForceInline}
 * static long xor(long a, long b) { return a ^ b; }
 * </pre>
 *
 * <p>Large methods or those containing loops may still not inline.</p>
 *
 * @see HotMethod
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForceInline {
}
