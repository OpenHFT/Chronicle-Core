package net.openhft.chronicle.core.internal.analytics;

import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class StandardMapsTest {

    @Test
    public void standardEventParameters() {
        assertEquals(Collections.singletonMap("app_version", "1.0.0"), StandardMaps.standardEventParameters("1.0.0"));
    }

    @Test
    public void standardAdditionalEventParametersThreadsStackTrace() {
        final Map<String, String> actual = StandardMaps.standardAdditionalEventParameters();
        assertFalse(actual.containsValue("java.lang"));
        assertFalse(actual.containsValue("org.junit"));
    }

    @Test
    public void standardAdditionalEventParameters() {

        final StackTraceElement[] stackTrace = Stream.of(
                "a.Foo",
                java.lang.Thread.class.getName(),
                "software.chronicle.enterprise.E",
                "b.Foo",
                java.lang.reflect.Method.class.getName(),
                "w.x.y.z.Foo",
                "jdk.internal.reflect",
                "java.util.concurrent",
                "org.apache.maven",
                java.util.List.class.getName()
        )
                .map(s -> new StackTraceElement(s, "m", "m.java", 1))
                .toArray(StackTraceElement[]::new);


        final Map<String, String> expected = new LinkedHashMap<>();
        expected.put("package_name_0", "a");
        expected.put("package_name_1", "b");
        expected.put("package_name_2", "w.x.y");

        final Map<String, String> actual = StandardMaps.standardAdditionalEventParameters(stackTrace);

        assertEquals(expected, actual);

    }

    @Test
    public void standardUserProperties() {
        assertFalse(StandardMaps.standardUserProperties().values().stream().anyMatch(Objects::isNull));
    }

    @Test
    public void packageNameUpToMaxLevel3Empty() {
        assertEquals("", StandardMaps.packageNameUpToMaxLevel3(""));
    }

    @Test
    public void packageNameUpToMaxLevel3JustClassName() {
        assertEquals("", StandardMaps.packageNameUpToMaxLevel3("Foo"));
    }

    @Test
    public void packageNameUpToMaxLevel3L1() {
        assertEquals("a", StandardMaps.packageNameUpToMaxLevel3("a.Foo"));
    }

    @Test
    public void packageNameUpToMaxLevel3L2() {
        assertEquals("a.b", StandardMaps.packageNameUpToMaxLevel3("a.b.Foo"));
    }

    @Test
    public void packageNameUpToMaxLevel3L3() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.Foo"));
    }

    @Test
    public void packageNameUpToMaxLevel3L4() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.d.Foo"));
    }

    @Test
    public void packageNameUpToMaxLevelThisClass() {
        assertEquals("net.openhft.chronicle", StandardMaps.packageNameUpToMaxLevel3(StandardMapsTest.class.getName()));
    }
}