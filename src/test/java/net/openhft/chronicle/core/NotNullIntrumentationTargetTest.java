package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

public class NotNullIntrumentationTargetTest {

    @Test
    public void notNull() {
        test("a");
    }

    @Test(expected = NullPointerException.class)
    @Ignore("Awaiting https://github.com/osundblad/intellij-annotations-instrumenter-maven-plugin/issues/53. " +
            "This behaves as expected when run in IntelliJ as IntelliJ compiler instruments classes with null checks")
    public void Null() {
        test(null);
    }

    private static void test(@NotNull String nn) {
        // This should throw an NPE if called with a null argument
    }

}