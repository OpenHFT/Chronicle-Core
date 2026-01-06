/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.naming.TimeLimitExceededException;
import java.util.Map;

import static net.openhft.chronicle.core.LicenceCheck.CHRONICLE_LICENSE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenceCheckTest extends CoreTestCommon {

    @AfterEach
    public void tearDown() {
        System.getProperties().remove(CHRONICLE_LICENSE);
        Jvm.resetExceptionHandlers();
    }

    @Test
    @DisplayName("Expired expiry file triggers time limit exception")
    void checkExpiredExpiryFile() {
        assertThrows(TimeLimitExceededException.class,
                () -> LicenceCheck.check("test", LicenceCheck.class),
                "expired expiry file should trigger TimeLimitExceededException");
    }

    @Test
    @DisplayName("Unexpired expiry file with newline passes check")
    void checkUnexpiredExpiryFileWithNewline() {
        assertDoesNotThrow(() -> LicenceCheck.check("test2", LicenceCheck.class),
                "unexpired expiry file with newline should not throw");
    }

    @Test
    @DisplayName("Evaluation licence expiry triggers time limit exception")
    void checkEvalExpired() {
        assertThrows(TimeLimitExceededException.class,
                () -> LicenceCheck.check("test", LicenceCheckTest.class),
                "evaluation licence expiry should trigger TimeLimitExceededException");
    }

    @Test
    @DisplayName("Valid licence records expiry warning in exception map")
    void checkLicense() {
        System.setProperty(CHRONICLE_LICENSE, "product=test.,owner=Test Unit,expires=9999-01-01,code=123456789");

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        // licensed
        LicenceCheck.check("test", null);
        assertTrue(map.toString().contains("license for Test Unit expires in about 7"),
                "licence expiry warning should be recorded in map: " + map);
    }

    @Test
    @DisplayName("Expired licence triggers time limit exception")
    void checkLicenseExpired() {
        System.setProperty(CHRONICLE_LICENSE, "product=test.,owner=Test Unit,expires=2019-01-01,code=123456789");
        assertThrows(TimeLimitExceededException.class,
                () -> LicenceCheck.check("test", null),
                "expired licence should trigger TimeLimitExceededException");
    }
}
