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
package net.openhft.chronicle.core.io;

/**
 * Utility methods to validate {@link Validatable} objects and control when the
 * checks occur.
 */
public class ValidatableUtil {
    static final ThreadLocal<int[]> VALIDATE_DISABLED = ThreadLocal.withInitial(() -> new int[1]);

    /**
     * Checks if validation is enabled.
     *
     * @return {@code true} if validation is enabled, {@code false} otherwise.
     */
    public static boolean validateEnabled() {
        return VALIDATE_DISABLED.get()[0] <= 0;
    }

    /**
     * Starts a nestable block where validation is disabled. Use this around
     * logging or diagnostic code that might operate on a partially initialised
     * object.
     */
    public static void startValidateDisabled() {
        VALIDATE_DISABLED.get()[0]++;
    }

    /**
     * Ends the block started with {@link #startValidateDisabled()}.
     */
    public static void endValidateDisabled() {
        int[] val = VALIDATE_DISABLED.get();
        assert val[0] > 0;
        val[0]--;
    }

    /**
     * Check an object is valid if it implements {@link Validatable}.
     * <p>
     * For logging purposes validation can be turned off e.g. for {@code toString()}
     * with the following pattern
     * <pre>
     * ValidatableUtil.startValidatableDisabled();
     * try {
     *   // some code
     * } finally {
     *    ValidatableUtil.endValidateDisabled();
     * }
     * </pre>
     *
     * @param t   object to validate
     * @param <T> type of object
     * @return the same object
     * @throws InvalidMarshallableException if validate() method fails
     */
    public static <T> T validate(T t) throws InvalidMarshallableException {
        if (t instanceof Validatable && validateEnabled())
            ((Validatable) t).validate();
        return t;
    }

    /**
     * requires that a reference not be null, or throw an InvalidMarshallableException with an appropriate message
     *
     * @param tested reference
     * @param name   of reference
     * @throws InvalidMarshallableException if tested is null
     */
    public static void requireNonNull(Object tested, String name) throws InvalidMarshallableException {
        if (tested == null)
            throw new InvalidMarshallableException(name + " must not be null");
    }

    /**
     * requires a test flag be true, otherwise throw an InvalidMarshallableException with the message provided
     *
     * @param test to check for true
     * @param msg  to use if false
     * @throws InvalidMarshallableException to throw is false
     */
    public static void requireTrue(boolean test, String msg) throws InvalidMarshallableException {
        if (!test)
            throw new InvalidMarshallableException(msg);
    }
}
