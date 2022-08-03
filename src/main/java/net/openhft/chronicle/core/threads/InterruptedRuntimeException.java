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

package net.openhft.chronicle.core.threads;

/**
 * An unchecked alternative to {@link InterruptedException}.
 * <p>
 * This should generally not be used, prefer {@link InterruptedException} wherever possible, but
 * there are some scenarios where we need to throw an unchecked exception after being interrupted.
 * <p>
 * If {@link InterruptedException} was caught, remember to set {@link Thread#interrupt()} prior to throwing these.
 */
public class InterruptedRuntimeException extends IllegalStateException {

    public InterruptedRuntimeException() {
    }

    public InterruptedRuntimeException(String s) {
        super(s);
    }

    public InterruptedRuntimeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InterruptedRuntimeException(Throwable throwable) {
        super(throwable);
    }
}
