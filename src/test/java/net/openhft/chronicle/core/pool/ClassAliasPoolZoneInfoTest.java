/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduces the {@code java.lang.ClassNotFoundException:
 * sun.util.calendar.ZoneInfo not available} failure reported by users on
 * Java 17+ when reading a persisted Chronicle-Map / Chronicle-Set that
 * embeds a TimeZone-typed field. The wire layer captures the JDK-internal
 * concrete subclass name (sun.util.calendar.ZoneInfo) as the YAML/TextWire
 * type tag; on read, ClassAliasPool refuses to resolve any name in the
 * banned sun.* / com.sun.* / com.oracle / jdk.* prefixes (see
 * {@link ClassAliasPool#banned(String)}), and the read fails.
 *
 * The fix is to register the legacy concrete name as an alias for
 * java.util.TimeZone so the resolver returns the public type before the
 * banned-prefix check runs.
 */
class ClassAliasPoolZoneInfoTest extends CoreTestCommon {

    @Test
    void zoneInfoLegacyName_resolvesToTimeZone() {
        // The exact failure point from the user's stack
        // (TextWire$TextValueIn.typePrefixOrObject -> ClassAliasPool.forName).
        assertEquals(TimeZone.class,
                CLASS_ALIASES.forName("sun.util.calendar.ZoneInfo"));
    }

    @Test
    void timeZoneCanonicalName_resolvesToTimeZone() {
        assertEquals(TimeZone.class,
                CLASS_ALIASES.forName("java.util.TimeZone"));
    }

    @Test
    void timeZoneSimpleName_resolvesToTimeZone() {
        assertEquals(TimeZone.class,
                CLASS_ALIASES.forName("TimeZone"));
    }

    @Test
    void runtimeZoneInfoClassName_isStillBannedWithoutTheAlias() {
        // Belt-and-braces: ClassAliasPool's banned() rule rejects every
        // sun.* class even when the JDK can load it. This test verifies
        // the ban is still active for unrelated sun.* names; only TimeZone
        // gets a free pass via the registered alias.
        assertThrows(ClassNotFoundRuntimeException.class,
                () -> CLASS_ALIASES.forName("sun.util.calendar.CalendarSystem"));
    }
}
