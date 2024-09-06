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

package net.openhft.chronicle.core.internal.util;

/**
 * Utility class for managing string constants related to range checks and comparisons.
 * <p>
 * This class contains a set of predefined string constants used for indicating the results of
 * range or comparison checks (e.g., whether a value is positive, negative, zero, or equal to a specific value).
 * These constants can be used in messages or exceptions to provide clearer feedback about the status of a check.
 * </p>
 * <p>
 * This class is not intended to be instantiated and provides only static constants.
 * </p>
 */
public final class RangeUtil {

    // Private constructor to prevent instantiation
    private RangeUtil() {}

    // Constant messages for positive, negative, and zero checks
    public static final String IS_POSITIVE = " is positive.";
    public  static final String IS_NEGATIVE = " is negative.";
    public static final String IS_ZERO = " is zero.";

    // Constant messages for equality checks
    public static final String IS_EQUAL_TO = " is equal to.";
    public static final String IS_NOT_EQUAL_TO = " is not equal to ";

    // Constant messages for range checks
    public static final String IS_NOT_POSITIVE = " is not positive.";
    public static final String IS_NOT_NEGATIVE = " is not negative.";
    public static final String IS_NOT_ZERO = " is not zero.";
    public static final String IS_NOT_IN_THE_RANGE = " is not in the range [";

}
