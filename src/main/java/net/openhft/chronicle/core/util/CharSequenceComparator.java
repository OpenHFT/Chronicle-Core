/*
 * Copyright 2016 higherfrequencytrading.com
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
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.ForceInline;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public enum CharSequenceComparator implements Comparator<CharSequence> {
    INSTANCE;

    @Override
    @ForceInline
    public int compare(@NotNull CharSequence o1, @NotNull CharSequence o2) {
        final int o1Length = o1.length();
        final int o2Length = o2.length();
        final int len = Math.min(o1Length, o2Length);
        for (int i = 0; i < len; i++) {
            final int cmp = Character.compare(o1.charAt(i), o2.charAt(i));
            if (cmp != 0)
                return cmp;
        }
        return Integer.compare(o1Length, o2Length);
    }
}