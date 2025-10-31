/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.internal.announcer;

import net.openhft.chronicle.core.announcer.Announcer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InternalAnnouncerTest {

    @BeforeAll
    static void disableOutput() {
        // Ensure the class initialises with announcement disabled
        System.setProperty("chronicle.announcer.disable", "true");
    }

    @Test
    void announceEmptyPropertiesAnnouncesOncePerArtifact() {
        assertDoesNotThrow(() -> {
            Announcer.announce("net.openhft", "chronicle-core");
            Announcer.announce("net.openhft", "chronicle-core");
        });
    }

    @Test
    void announceWithLogoOnly() {
        Map<String, String> props = new HashMap<>();
        props.put(Announcer.LOGO, "ASCII-LOGO");
        assertDoesNotThrow(() -> {
            Announcer.announce("net.openhft", "chronicle-map", props);
            Announcer.announce("net.openhft", "chronicle-map", props);
        });
    }

    @Test
    void announceWithAdditionalProperties() {
        Map<String, String> props = new HashMap<>();
        props.put(Announcer.LOGO, "ASCII-LOGO");
        props.put("build", "test");
        assertDoesNotThrow(() -> Announcer.announce("net.openhft", "chronicle-queue", props));
    }
}

