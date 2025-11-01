/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import org.junit.Test;

import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;

public class StandardMapsTest {

    @Test
    public void standardEventParametersIncludesAppVersion() {
        Map<String, String> eventParameters = StandardMaps.standardEventParameters("9.9.9");
        assertEquals("9.9.9", eventParameters.get("app_version"));
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

        assertTrue("Expected at most three entries", additional.size() <= 3);
        assertTrue(additional.values().stream().anyMatch(v -> v.contains("run.chronicle.demo")));
        assertFalse("Enterprise packages should be filtered", additional.values().stream().anyMatch(v -> v.startsWith("software.chronicle")));
    }

    @Test
    public void packageNameUpToMaxLevel3CollapsesDeepPackages() {
        String collapsed = StandardMaps.packageNameUpToMaxLevel3("com.example.deep.pkg.name.Component");
        assertEquals("com.example.deep", collapsed);

        assertEquals("net.openhft", StandardMaps.packageNameUpToMaxLevel3("net.openhft.Class"));
        assertEquals("Class", StandardMaps.packageNameUpToMaxLevel3("Class"));
    }

    @Test
    public void standardUserPropertiesExposeRuntime() {
        Map<String, String> userProps = StandardMaps.standardUserProperties();
        assertTrue(userProps.containsKey("java_runtime_name"));
        assertNotNull(userProps.get("java_runtime_name"));
        assertFalse(userProps.values().stream().anyMatch(Objects::isNull));
    }
}
