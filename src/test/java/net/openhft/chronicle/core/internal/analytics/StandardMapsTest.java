/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class StandardMapsTest extends CoreTestCommon {

    @Test
    public void standardEventParameters() {
        assertEquals(Collections.singletonMap("app_version", "1.0.0"), StandardMaps.standardEventParameters("1.0.0"), "standardEventParameters: L19");
    }

    @Test
    public void standardAdditionalEventParametersThreadsStackTrace() {
        final Map<String, String> actual = StandardMaps.standardAdditionalEventParameters();
        assertFalse(actual.containsValue("java.lang"), "standardAdditionalEventParametersThreadsStackTrace: L25");
        assertFalse(actual.containsValue("org.junit"), "standardAdditionalEventParametersThreadsStackTrace: L26");
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

        assertEquals(expected, actual, "standardAdditionalEventParameters: L56");

    }

    @Test
    public void standardUserProperties() {
        assertFalse(StandardMaps.standardUserProperties().values().stream().anyMatch(Objects::isNull), "standardUserProperties: L62");
    }

    @Test
    public void packageNameUpToMaxLevel3Empty() {
        assertEquals("", StandardMaps.packageNameUpToMaxLevel3(""), "packageNameUpToMaxLevel3Empty: L67");
    }

    @Test
    public void packageNameUpToMaxLevel3L0() {
        assertEquals("foo", StandardMaps.packageNameUpToMaxLevel3("foo"), "packageNameUpToMaxLevel3L0: L72");
    }

    @Test
    public void packageNameUpToMaxLevel3L1() {
        assertEquals("a", StandardMaps.packageNameUpToMaxLevel3("a.foo"), "packageNameUpToMaxLevel3L1: L77");
    }

    @Test
    public void packageNameUpToMaxLevel3L2() {
        assertEquals("a.b", StandardMaps.packageNameUpToMaxLevel3("a.b.foo"), "packageNameUpToMaxLevel3L2: L82");
    }

    @Test
    public void packageNameUpToMaxLevel3L3() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.foo"), "packageNameUpToMaxLevel3L3: L87");
    }

    @Test
    public void packageNameUpToMaxLevel3L4() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.d.foo"), "packageNameUpToMaxLevel3L4: L92");
    }

    @Test
    public void packageNameUpToMaxLevelThisClass() {
        assertEquals("net.openhft.chronicle", StandardMaps.packageNameUpToMaxLevel3(StandardMapsTest.class.getName()), "packageNameUpToMaxLevelThisClass: L97");
    }

    @Test
    public void distinctUpToMaxLevel3() {
        final Set<String> distinctKeys = new HashSet<>();
        final List<String> list = Stream.of("a.b.c.d", "a.b.c.d.e", "x", "y", "z")
                .filter(pn -> StandardMaps.distinctUpToMaxLevel3(pn, distinctKeys))
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("a.b.c.d", "x", "y", "z"), list, "distinctUpToMaxLevel3: L107");
    }

    @Test
    public void standardEventParametersIncludesAppVersion() {
        Map<String, String> eventParameters = StandardMaps.standardEventParameters("9.9.9");
        assertEquals("9.9.9", eventParameters.get("app_version"), "standardEventParametersIncludesAppVersion: L113");
    }

    @Test
    public void additionalEventParametersHonoursWhitelistAndMaxThreeEntries() {
        StackTraceElement[] elements = {
                new StackTraceElement("run.chronicle.demo.alpha.Component", "m", "Component.java", 1),
                new StackTraceElement("run.chronicle.demo.beta.Helper", "m", "Helper.java", 1),
                new StackTraceElement("com.example.service.Worker", "m", "Worker.java", 1),
                new StackTraceElement("software.chronicle.enterprise.Service", "m", "Service.java", 1)
        };

        Map<String, String> additional = StandardMaps.standardAdditionalEventParameters(elements);

        assertTrue(additional.size() <= 3, "Expected at most three entries");
        assertTrue(additional.values().stream().anyMatch(v -> v.contains("run.chronicle.demo")), "additionalEventParametersHonoursWhitelistAndMaxThreeEntries: L128");
        assertFalse(additional.values().stream().anyMatch(v -> v.startsWith("software.chronicle")), "Enterprise packages should be filtered");
    }

    @Test
    public void packageNameUpToMaxLevel3CollapsesDeepPackages() {
        String collapsed = StandardMaps.packageNameUpToMaxLevel3("com.example.deep.pkg.name.Component");
        assertEquals("com.example.deep", collapsed, "packageNameUpToMaxLevel3CollapsesDeepPackages: L135");

        assertEquals("net.openhft", StandardMaps.packageNameUpToMaxLevel3("net.openhft.Class"), "packageNameUpToMaxLevel3CollapsesDeepPackages: L137");
        assertEquals("Class", StandardMaps.packageNameUpToMaxLevel3("Class"), "packageNameUpToMaxLevel3CollapsesDeepPackages: L138");
    }

    @Test
    public void standardUserPropertiesExposeRuntime() {
        Map<String, String> userProps = StandardMaps.standardUserProperties();
        assertTrue(userProps.containsKey("java_runtime_name"), "standardUserPropertiesExposeRuntime: L144");
        assertNotNull(userProps.get("java_runtime_name"), "standardUserPropertiesExposeRuntime: L145");
        assertFalse(userProps.values().stream().anyMatch(Objects::isNull), "standardUserPropertiesExposeRuntime: L146");
    }
}
