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
package net.openhft.chronicle.core;

import junit.framework.TestCase;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.After;
import org.junit.Test;

import javax.naming.TimeLimitExceededException;
import java.util.Map;

import static net.openhft.chronicle.core.LicenceCheck.CHRONICLE_LICENSE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LicenceCheckTest extends CoreTestCommon {

    @After
    public void tearDown() {
        System.getProperties().remove(CHRONICLE_LICENSE);
        Jvm.resetExceptionHandlers();
    }

    @Test(expected = TimeLimitExceededException.class)
    public void checkExpiredExpiryFile() {
        LicenceCheck.check("test", LicenceCheck.class);
        fail("should have got an AssertionError");
    }

    @Test
    public void checkUnexpiredExpiryFileWithNewline() {
        LicenceCheck.check("test2", LicenceCheck.class);
    }

    @Test(expected = TimeLimitExceededException.class)
    public void checkEvalExpired() {
        LicenceCheck.check("test", TestCase.class);
    }

    @Test
    public void checkLicense() {
        System.setProperty(CHRONICLE_LICENSE, "product=test.,owner=Test Unit,expires=9999-01-01,code=123456789");

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        // licensed
        LicenceCheck.check("test", null);
        assertTrue(map.toString().contains("license for Test Unit expires in about 7"));
    }

    @Test(expected = TimeLimitExceededException.class)
    public void checkLicenseExpired() {
        System.setProperty(CHRONICLE_LICENSE, "product=test.,owner=Test Unit,expires=2019-01-01,code=123456789");
        LicenceCheck.check("test", null);
    }
}
