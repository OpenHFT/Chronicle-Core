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

class StandardMapsTest extends CoreTestCommon {

    @Test
    void standardEventParameters() {
        assertEquals(Collections.singletonMap("app_version", "1.0.0"), StandardMaps.standardEventParameters("1.0.0"),
                "event map keeps app version");
    }

    @Test
    void standardAdditionalEventParametersThreadsStackTrace() {
        final Map<String, String> actual = StandardMaps.standardAdditionalEventParameters();
        assertFalse(actual.containsValue("java.lang"), "stack trace skips java lang");
        assertFalse(actual.containsValue("org.junit"), "stack trace skips junit packages");
    }

    @Test
    void standardAdditionalEventParameters() {

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

        assertEquals(expected, actual, "filtered package list matches expected");

    }

    @Test
    void standardUserProperties() {
        assertFalse(StandardMaps.standardUserProperties().values().stream().anyMatch(Objects::isNull),
                "user properties avoid nulls");
    }

    @Test
    void packageNameUpToMaxLevel3Empty() {
        assertEquals("", StandardMaps.packageNameUpToMaxLevel3(""), "empty input yields empty name");
    }

    @Test
    void packageNameUpToMaxLevel3L0() {
        assertEquals("foo", StandardMaps.packageNameUpToMaxLevel3("foo"), "single level keeps name");
    }

    @Test
    void packageNameUpToMaxLevel3L1() {
        assertEquals("a", StandardMaps.packageNameUpToMaxLevel3("a.foo"), "two level keeps prefix");
    }

    @Test
    void packageNameUpToMaxLevel3L2() {
        assertEquals("a.b", StandardMaps.packageNameUpToMaxLevel3("a.b.foo"), "three level keeps prefix");
    }

    @Test
    void packageNameUpToMaxLevel3L3() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.foo"), "four level keeps prefix");
    }

    @Test
    void packageNameUpToMaxLevel3L4() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.d.foo"), "deep name trims prefix");
    }

    @Test
    void packageNameUpToMaxLevelThisClass() {
        assertEquals("net.openhft.chronicle", StandardMaps.packageNameUpToMaxLevel3(StandardMapsTest.class.getName()),
                "own type keeps root package");
    }

    @Test
    void distinctUpToMaxLevel3() {
        final Set<String> distinctKeys = new HashSet<>();
        final List<String> list = Stream.of("a.b.c.d", "a.b.c.d.e", "x", "y", "z")
                .filter(pn -> StandardMaps.distinctUpToMaxLevel3(pn, distinctKeys))
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("a.b.c.d", "x", "y", "z"), list, "distinct list drops duplicate prefixes");
    }

    @Test
    void standardEventParametersIncludesAppVersion() {
        Map<String, String> eventParameters = StandardMaps.standardEventParameters("9.9.9");
        assertEquals("9.9.9", eventParameters.get("app_version"), "app version entry kept");
    }

    @Test
    void additionalEventParametersHonoursWhitelistAndMaxThreeEntries() {
        StackTraceElement[] elements = {
                new StackTraceElement("run.chronicle.demo.alpha.Component", "m", "Component.java", 1),
                new StackTraceElement("run.chronicle.demo.beta.Helper", "m", "Helper.java", 1),
                new StackTraceElement("com.example.service.Worker", "m", "Worker.java", 1),
                new StackTraceElement("software.chronicle.enterprise.Service", "m", "Service.java", 1)
        };

        Map<String, String> additional = StandardMaps.standardAdditionalEventParameters(elements);

        int entryCount = additional.size();
        assertTrue(entryCount <= 3, "additional entry count should be <= 3, was " + entryCount);
        assertTrue(additional.values().stream().anyMatch(v -> v.contains("run.chronicle.demo")),
                "whitelist keeps demo package");
        assertFalse(additional.values().stream().anyMatch(v -> v.startsWith("software.chronicle")),
                "Enterprise packages should be filtered");
    }

    @Test
    void packageNameUpToMaxLevel3CollapsesDeepPackages() {
        String collapsed = StandardMaps.packageNameUpToMaxLevel3("com.example.deep.pkg.name.Component");
        assertEquals("com.example.deep", collapsed, "deep package keeps three levels");

        assertEquals("net.openhft", StandardMaps.packageNameUpToMaxLevel3("net.openhft.Class"),
                "two level prefix kept");
        assertEquals("Class", StandardMaps.packageNameUpToMaxLevel3("Class"), "unqualified class name should remain unchanged");
    }

    @Test
    void standardUserPropertiesExposeRuntime() {
        Map<String, String> userProps = StandardMaps.standardUserProperties();
        assertTrue(userProps.containsKey("java_runtime_name"), "runtime properties should include java_runtime_name");
        String runtimeName = userProps.get("java_runtime_name");
        assertNotNull(runtimeName, "java_runtime_name should be set in runtime properties");
        assertFalse(userProps.values().stream().anyMatch(Objects::isNull), "runtime props avoid nulls");
    }
}
