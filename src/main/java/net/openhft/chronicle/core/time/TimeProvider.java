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

package net.openhft.chronicle.core.time;

import java.util.concurrent.TimeUnit;

import static net.openhft.chronicle.core.time.SystemTimeProvider.TIME_PROVIDER;

/*
 * Created by Peter Lawrey on 10/03/16.
 */
@FunctionalInterface
public interface TimeProvider {

    static TimeProvider get() {
        return TIME_PROVIDER.get();
    }

    long currentTimeMillis();

    default long currentTimeMicros() {
        return currentTimeMillis() * 1000;
    }

    default long currentTimeNanos() {
        return currentTimeMillis() * 1000000;
    }

    default long currentTime(TimeUnit timeUnit) {
        return timeUnit == TimeUnit.MILLISECONDS
                ? currentTimeMillis()
                : timeUnit.convert(currentTimeNanos(), TimeUnit.NANOSECONDS);
    }
}
