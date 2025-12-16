/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.naming.TimeLimitExceededException;
import java.util.Map;

import static net.openhft.chronicle.core.LicenceCheck.CHRONICLE_LICENSE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LicenceCheckTest extends CoreTestCommon {

    @AfterEach
    public void tearDown() {
        System.getProperties().remove(CHRONICLE_LICENSE);
        Jvm.resetExceptionHandlers();
    }

    @Test
    public void checkExpiredExpiryFile() {
        assertThrows(TimeLimitExceededException.class,
                () -> LicenceCheck.check("test", LicenceCheck.class),
                "checkExpiredExpiryFile");
    }

    @Test
    public void checkUnexpiredExpiryFileWithNewline() {
        assertDoesNotThrow(() -> LicenceCheck.check("test2", LicenceCheck.class),
                "checkUnexpiredExpiryFileWithNewline");
    }

    @Test
    public void checkEvalExpired() {
        assertThrows(TimeLimitExceededException.class,
                () -> LicenceCheck.check("test", LicenceCheckTest.class),
                "checkEvalExpired");
    }

    @Test
    public void checkLicense() {
        System.setProperty(CHRONICLE_LICENSE, "product=test.,owner=Test Unit,expires=9999-01-01,code=123456789");

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        // licensed
        LicenceCheck.check("test", null);
        assertTrue(map.toString().contains("license for Test Unit expires in about 7"),
                "expected warning about licence expiry in map: " + map);
    }

    @Test
    public void checkLicenseExpired() {
        System.setProperty(CHRONICLE_LICENSE, "product=test.,owner=Test Unit,expires=2019-01-01,code=123456789");
        assertThrows(TimeLimitExceededException.class,
                () -> LicenceCheck.check("test", null),
                "checkLicenseExpired");
    }
}
