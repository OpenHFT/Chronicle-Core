package net.openhft.chronicle.core.internal.analytics;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
                "software.chronicle.fix.demo.connections.example1.Main", // White listed
                "software.chronicle.fix.Runner", // Black listed
                java.lang.Thread.class.getName(),
                "software.chronicle.enterprise.E",
                "b.Foo",
                java.lang.reflect.Method.class.getName(),
                "w.x.y.z.Foo",
                "jdk.internal.reflect.A",
                "java.util.concurrent.A",
                "org.apache.maven.A",
                java.util.List.class.getName()
        )
                .map(s -> new StackTraceElement(s, "m", "m.java", 1))
                .toArray(StackTraceElement[]::new);

        final Map<String, String> expected = new LinkedHashMap<>();
        expected.put("package_name_0", "a");
        expected.put("package_name_1", "software.chronicle.fix.demo.connections.example1");
        expected.put("package_name_2", "b");

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
    public void packageNameUpToMaxLevel3L0() {
        assertEquals("foo", StandardMaps.packageNameUpToMaxLevel3("foo"));
    }

    @Test
    public void packageNameUpToMaxLevel3L1() {
        assertEquals("a", StandardMaps.packageNameUpToMaxLevel3("a.foo"));
    }

    @Test
    public void packageNameUpToMaxLevel3L2() {
        assertEquals("a.b", StandardMaps.packageNameUpToMaxLevel3("a.b.foo"));
    }

    @Test
    public void packageNameUpToMaxLevel3L3() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.foo"));
    }

    @Test
    public void packageNameUpToMaxLevel3L4() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.d.foo"));
    }

    @Test
    public void packageNameUpToMaxLevelThisClass() {
        assertEquals("net.openhft.chronicle", StandardMaps.packageNameUpToMaxLevel3(StandardMapsTest.class.getName()));
    }

    @Test
    public void distinctUpToMaxLevel3() {
        final Set<String> distinctKeys = new HashSet<>();
        final List<String> list = Stream.of("a.b.c.d", "a.b.c.d.e", "x", "y", "z")
                .filter(pn -> StandardMaps.distinctUpToMaxLevel3(pn, distinctKeys))
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("a.b.c.d", "x", "y", "z"), list);
    }
}