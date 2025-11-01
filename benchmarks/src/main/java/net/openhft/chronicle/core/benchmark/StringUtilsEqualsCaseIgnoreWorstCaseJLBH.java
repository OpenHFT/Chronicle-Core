/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.benchmark;

import static net.openhft.chronicle.core.benchmark.StringUtilsEqualsCaseIgnoreBaseJLBH.generate;

public class StringUtilsEqualsCaseIgnoreWorstCaseJLBH {
    public static void main(String[] args) {
        // Test two strings of entirely different case for their duration
        StringUtilsEqualsCaseIgnoreBaseJLBH.run(
                StringUtilsEqualsCaseIgnoreWorstCaseJLBH.class,
                () -> generate(() -> 'A', 100),
                () -> generate(() -> 'a', 100)
        );
    }
}
