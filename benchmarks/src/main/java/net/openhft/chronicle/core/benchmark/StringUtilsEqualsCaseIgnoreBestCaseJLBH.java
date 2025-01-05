package net.openhft.chronicle.core.benchmark;

import static net.openhft.chronicle.core.benchmark.StringUtilsEqualsCaseIgnoreBaseJLBH.generate;

public class StringUtilsEqualsCaseIgnoreBestCaseJLBH {
    public static void main(String[] args) {
        StringUtilsEqualsCaseIgnoreBaseJLBH.run(
                StringUtilsEqualsCaseIgnoreBestCaseJLBH.class,
                () -> generate(() -> 'a', 100),
                () -> generate(() -> 'a', 100)
        );
    }
}
