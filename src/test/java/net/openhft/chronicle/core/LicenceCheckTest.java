/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core;

import junit.framework.TestCase;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.After;
import org.junit.Test;

import javax.naming.TimeLimitExceededException;
import java.util.Map;

import static net.openhft.chronicle.core.LicenceCheck.CHRONICLE_LICENSE;
import static org.junit.Assert.*;

public class LicenceCheckTest {

    @After
    public void tearDown() {
        System.getProperties().remove(CHRONICLE_LICENSE);
        Jvm.resetExceptionHandlers();
    }

    @Test
    public void checkEval() {
        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        // Evaluation license
        LicenceCheck.check("test", LicenceCheck.class);
        assertEquals("{ExceptionKey{level=WARN, clazz=interface net.openhft.chronicle.core.LicenceCheck, message='Evaluation version expires in 92 days', throwable=}=1}", map.toString());
    }

    @Test
    public void checkEvalExpired() {
        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        // Evaluation license
        try {
            LicenceCheck.check("test", TestCase.class);
            fail();
        } catch (AssertionError e) {
            assertEquals(TimeLimitExceededException.class, e.getCause().getClass());
        }
        assertTrue(map.toString().contains("Evaluation version expires in -"));
    }

    @Test
    public void checkLicense() {
        System.setProperty(CHRONICLE_LICENSE, "product=test.,owner=Test Unit,expires=9999-01-01,code=123456789");

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        // licensed
        LicenceCheck.check("test", null);
        System.out.println(map);
        assertTrue(map.toString().contains("License for Test Unit expires in 29"));
    }

    @Test
    public void checkLicenseExpired() {
        System.setProperty(CHRONICLE_LICENSE, "product=test.,owner=Test Unit,expires=2019-01-01,code=123456789");

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        // licensed
        try {
            LicenceCheck.check("test", null);
            fail();
        } catch (AssertionError e) {
            assertEquals(TimeLimitExceededException.class, e.getCause().getClass());
        }
        assertTrue(map.toString().contains("License for Test Unit expires in -"));
    }
}