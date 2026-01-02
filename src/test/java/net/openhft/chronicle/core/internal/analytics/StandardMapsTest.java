/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StandardMapsTest extends CoreTestCommon {

    @Test
    @DisplayName("Event parameters include app version entry")
    void standardEventParameters() {
        assertEquals(Collections.singletonMap("app_version", "1.0.0"), StandardMaps.standardEventParameters("1.0.0"), "standardEventParameters should return map with app_version key");
    }

    @Test
    @DisplayName("Additional event parameters filter runtime stack entries")
    void standardAdditionalEventParametersThreadsStackTrace() {
        final Map<String, String> actual = StandardMaps.standardAdditionalEventParameters();
        assertFalse(actual.containsValue("java.lang"), "additional event parameters should filter out java.lang packages");
        assertFalse(actual.containsValue("org.junit"), "additional event parameters should filter out org.junit packages");
    }

    @Test
    @DisplayName("Additional event parameters keep whitelisted package entries")
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

        assertEquals(expected, actual, "standardAdditionalEventParameters should filter and return expected packages");

    }

    @Test
    @DisplayName("User properties map omits null entries")
    void standardUserProperties() {
        assertFalse(StandardMaps.standardUserProperties().values().stream().anyMatch(Objects::isNull),
                "standardUserProperties map should not contain null values");
    }

    @Test
    @DisplayName("Package prefix extraction handles empty input")
    void packageNameUpToMaxLevel3Empty() {
        assertEquals("", StandardMaps.packageNameUpToMaxLevel3(""), "packageNameUpToMaxLevel3 should return empty string for empty input");
    }

    @Test
    @DisplayName("Package prefix keeps single level name")
    void packageNameUpToMaxLevel3L0() {
        assertEquals("foo", StandardMaps.packageNameUpToMaxLevel3("foo"), "packageNameUpToMaxLevel3 should return same name for single level package");
    }

    @Test
    @DisplayName("Package prefix truncates to first level segment")
    void packageNameUpToMaxLevel3L1() {
        assertEquals("a", StandardMaps.packageNameUpToMaxLevel3("a.foo"), "packageNameUpToMaxLevel3 should return first level for two level package");
    }

    @Test
    @DisplayName("Package prefix keeps two level segment")
    void packageNameUpToMaxLevel3L2() {
        assertEquals("a.b", StandardMaps.packageNameUpToMaxLevel3("a.b.foo"), "packageNameUpToMaxLevel3 should return first two levels for three level package");
    }

    @Test
    @DisplayName("Package prefix keeps three level segment")
    void packageNameUpToMaxLevel3L3() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.foo"), "packageNameUpToMaxLevel3 should return first three levels for four level package");
    }

    @Test
    @DisplayName("Package prefix truncates deep package to three levels")
    void packageNameUpToMaxLevel3L4() {
        assertEquals("a.b.c", StandardMaps.packageNameUpToMaxLevel3("a.b.c.d.foo"), "packageNameUpToMaxLevel3 should truncate to first three levels for deep package");
    }

    @Test
    @DisplayName("Package prefix uses first three class segments")
    void packageNameUpToMaxLevelThisClass() {
        assertEquals("net.openhft.chronicle", StandardMaps.packageNameUpToMaxLevel3(StandardMapsTest.class.getName()), "packageNameUpToMaxLevel3 should return first three levels for test class package");
    }

    @Test
    @DisplayName("Distinct filter removes duplicate package prefixes")
    void distinctUpToMaxLevel3() {
        final Set<String> distinctKeys = new HashSet<>();
        final List<String> list = Stream.of("a.b.c.d", "a.b.c.d.e", "x", "y", "z")
                .filter(pn -> StandardMaps.distinctUpToMaxLevel3(pn, distinctKeys))
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("a.b.c.d", "x", "y", "z"), list, "distinctUpToMaxLevel3 should filter duplicate package prefixes");
    }

    @Test
    @DisplayName("Event parameters carry supplied app version")
    void standardEventParametersIncludesAppVersion() {
        Map<String, String> eventParameters = StandardMaps.standardEventParameters("9.9.9");
        assertEquals("9.9.9", eventParameters.get("app_version"), "standardEventParameters should include specified app_version");
    }

    @Test
    @DisplayName("Additional parameters honour whitelist and limit entries")
    void additionalEventParametersHonoursWhitelistAndMaxThreeEntries() {
        StackTraceElement[] elements = {
                new StackTraceElement("run.chronicle.demo.alpha.Component", "m", "Component.java", 1),
                new StackTraceElement("run.chronicle.demo.beta.Helper", "m", "Helper.java", 1),
                new StackTraceElement("com.example.service.Worker", "m", "Worker.java", 1),
                new StackTraceElement("software.chronicle.enterprise.Service", "m", "Service.java", 1)
        };

        Map<String, String> additional = StandardMaps.standardAdditionalEventParameters(elements);

        assertTrue(additional.size() <= 3, "Expected at most three entries");
        assertTrue(additional.values().stream().anyMatch(v -> v.contains("run.chronicle.demo")), "additional event parameters should include whitelisted packages");
        assertFalse(additional.values().stream().anyMatch(v -> v.startsWith("software.chronicle")), "Enterprise packages should be filtered");
    }

    @Test
    @DisplayName("Package prefix collapses deep package names")
    void packageNameUpToMaxLevel3CollapsesDeepPackages() {
        String collapsed = StandardMaps.packageNameUpToMaxLevel3("com.example.deep.pkg.name.Component");
        assertEquals("com.example.deep", collapsed, "deep package name should collapse to first 3 levels");

        assertEquals("net.openhft", StandardMaps.packageNameUpToMaxLevel3("net.openhft.Class"), "packageNameUpToMaxLevel3 should return first two levels for three level package name");
        assertEquals("Class", StandardMaps.packageNameUpToMaxLevel3("Class"), "packageNameUpToMaxLevel3 should return class name for unqualified class");
    }

    @Test
    @DisplayName("User properties expose runtime metadata keys")
    void standardUserPropertiesExposeRuntime() {
        Map<String, String> userProps = StandardMaps.standardUserProperties();
        assertTrue(userProps.containsKey("java_runtime_name"), "standardUserProperties should contain java_runtime_name key");
        assertNotNull(userProps.get("java_runtime_name"), "java_runtime_name value should not be null");
        assertFalse(userProps.values().stream().anyMatch(Objects::isNull),
                "standardUserProperties should contain only non-null runtime values");
    }
}
