/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

package net.openhft.chronicle.core.threads;

public class InvalidEventHandlerException extends Exception {

    private static final InvalidEventHandlerException STATIC = new ReusableInvalidEventHandlerException();

    public InvalidEventHandlerException(String message) {
        super(message);
    }

    public InvalidEventHandlerException() {
    }

    public InvalidEventHandlerException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns a reusable, pre-created, InvalidEventHandlerException that is
     * unmodifiable and contains no stack trace.
     * <p>
     * This is useful for situations where throwing an InvalidEventHandlerException
     * is used strictly for flow-control (e.g. removing an EventHandler from
     * an EventLoop when the former has completed with no errors).
     *
     * @return a reusable, pre-created, InvalidEventHandlerException that is
     *         unmodifiable and contains no stack trace
     */
    public static InvalidEventHandlerException reusable() {
        return STATIC;
    }

    private static final class ReusableInvalidEventHandlerException extends InvalidEventHandlerException {

        public ReusableInvalidEventHandlerException() {
            super("Reusable InvalidEventHandlerException with no stack trace.");
            setStackTrace(new StackTraceElement[0]);
        }

        @Override
        public synchronized Throwable initCause(Throwable cause) {
            return this;
        }

        @Override
        public void setStackTrace(StackTraceElement[] stackTrace) {
            if (stackTrace.length == 0)
                super.setStackTrace(stackTrace);
        }
    }

}