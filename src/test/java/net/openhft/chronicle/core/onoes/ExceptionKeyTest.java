/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionKeyTest extends CoreTestCommon {

    @Test
    @DisplayName("Equals and hash code exception key")
    void testEqualsAndHashCode() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        ExceptionKey ek1b = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        assertEquals(ek1, ek1b, "ExceptionKey with identical parameters should be equal");
        assertEquals(ek1.hashCode(), ek1b.hashCode(), "ExceptionKey with identical parameters should have same hashCode");
        assertEquals("ExceptionKey{level=PERF, clazz=class net.openhft.chronicle.core.onoes.ExceptionKeyTest, message='one', throwable=}", ek1.toString(),
                "ExceptionKey toString should include level, class, message and throwable for PERF key");
        ExceptionKey ek2 = new ExceptionKey(LogLevel.WARN, getClass(), "two", null);
        assertEquals("ExceptionKey{level=WARN, clazz=class net.openhft.chronicle.core.onoes.ExceptionKeyTest, message='two', throwable=}", ek2.toString(),
                "ExceptionKey toString should include level, class, message and throwable for WARN key");
        assertNotEquals(ek1, ek2, "ExceptionKey values with different levels or messages should not be equal");
        assertNotEquals(ek1.hashCode(), ek2.hashCode(), "ExceptionKey values with different fields should not share a hashCode");
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("message() returns throwable.toString() when message is null fallback")
    void messageReturnsThrowableWhenNull() {
        Exception ex = new RuntimeException("test error");
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), null, ex);
        assertEquals(ex.toString(), ek.message(),
                "message() should return throwable.toString() when message is null");
    }

    @Test
    @DisplayName("message() returns throwable.toString() when message is empty fallback")
    void messageReturnsThrowableWhenEmpty() {
        Exception ex = new RuntimeException("test error");
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "", ex);
        assertEquals(ex.toString(), ek.message(),
                "message() should return throwable.toString() when message is empty");
    }

    @Test
    @DisplayName("ExceptionKey message() returns custom text when caller supplies message value")
    void messageReturnsMessageWhenPresent() {
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "custom message", null);
        assertEquals("custom message", ek.message(),
                "message() should return the message when it is present");
    }

    @Test
    @DisplayName("hashCode handles null message input safely")
    void hashCodeNullMessage() {
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), null, null);
        int hash = ek.hashCode();
        // Should not throw and should produce consistent result
        assertEquals(hash, ek.hashCode(), "hashCode should be consistent");
    }

    @Test
    @DisplayName("hashCode handles null throwable input safely")
    void hashCodeNullThrowable() {
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        int hash = ek.hashCode();
        assertEquals(hash, ek.hashCode(), "hashCode should be consistent with null throwable");
    }

    @Test
    @DisplayName("hashCode includes throwable hash when present")
    void hashCodeWithThrowable() {
        Exception ex = new RuntimeException("err");
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", ex);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        assertNotEquals(ek1.hashCode(), ek2.hashCode(),
                "hashCode should differ when throwable differs");
    }

    @Test
    @DisplayName("ExceptionKey equals returns true for same instance identity comparison")
    void equalsSameInstance() {
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        assertEquals(ek, ek, "same instance should be equal to itself");
    }

    @Test
    @DisplayName("ExceptionKey equals returns false for null reference input argument comparison")
    void equalsNull() {
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        assertNotEquals(ek, null, "ExceptionKey should not equal null");
    }

    @Test
    @DisplayName("ExceptionKey equals returns false for different class type comparison")
    void equalsDifferentClass() {
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        assertNotEquals(ek, "not an ExceptionKey", "ExceptionKey should not equal String");
    }

    @Test
    @DisplayName("ExceptionKey equals returns false for different log level severity")
    void equalsDifferentLevel() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.WARN, getClass(), "msg", null);
        assertNotEquals(ek1, ek2, "ExceptionKey with different level should not be equal");
    }

    @Test
    @DisplayName("ExceptionKey equals returns false for different clazz type comparison")
    void equalsDifferentClazz() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, String.class, "msg", null);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.ERROR, Integer.class, "msg", null);
        assertNotEquals(ek1, ek2, "ExceptionKey with different clazz should not be equal");
    }

    @Test
    @DisplayName("ExceptionKey equals returns false for different message text content")
    void equalsDifferentMessage() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg1", null);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg2", null);
        assertNotEquals(ek1, ek2, "ExceptionKey with different message should not be equal");
    }

    @Test
    @DisplayName("equals handles null message comparison safely")
    void equalsNullMessageComparison() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, getClass(), null, null);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        assertNotEquals(ek1, ek2, "ExceptionKey with null vs non-null message should not be equal");
    }

    @Test
    @DisplayName("equals returns true when both message values are null inputs")
    void equalsBothMessagesNull() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, getClass(), null, null);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.ERROR, getClass(), null, null);
        assertEquals(ek1, ek2, "ExceptionKey with both null messages should be equal");
    }

    @Test
    @DisplayName("ExceptionKey equals returns false for different throwable cause reference")
    void equalsDifferentThrowable() {
        Exception ex1 = new RuntimeException("err1");
        Exception ex2 = new RuntimeException("err2");
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", ex1);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", ex2);
        assertNotEquals(ek1, ek2, "ExceptionKey with different throwable should not be equal");
    }

    @Test
    @DisplayName("equals handles null throwable comparison safely")
    void equalsNullThrowableComparison() {
        Exception ex = new RuntimeException("err");
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", ex);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        assertNotEquals(ek1, ek2, "ExceptionKey with null vs non-null throwable should not be equal");
    }

    @Test
    @DisplayName("toString includes stack trace when throwable present")
    void toStringWithThrowable() {
        Exception ex = new RuntimeException("test error");
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", ex);
        String str = ek.toString();
        assertTrue(str.contains("RuntimeException"),
                "toString output '" + str + "' should contain 'RuntimeException'");
        assertTrue(str.contains("test error"),
                "toString output '" + str + "' should contain 'test error'");
        assertTrue(str.contains("ExceptionKeyTest"),
                "toString output '" + str + "' should contain stack trace class 'ExceptionKeyTest'");
    }

    @Test
    @DisplayName("toString handles null throwable value safely")
    void toStringNullThrowable() {
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "msg", null);
        String str = ek.toString();
        assertTrue(str.contains("level=ERROR"),
                "toString output '" + str + "' should contain 'level=ERROR'");
        assertTrue(str.contains("message='msg'"),
                "toString output '" + str + "' should contain \"message='msg'\"");
        assertTrue(str.contains("throwable="),
                "toString output '" + str + "' should contain 'throwable='");
    }

    @Test
    @DisplayName("accessor methods return correct field values")
    void accessorMethods() {
        Exception ex = new RuntimeException("err");
        ExceptionKey ek = new ExceptionKey(LogLevel.WARN, String.class, "test", ex);

        assertEquals(LogLevel.WARN, ek.level(), "level() should return WARN");
        assertEquals(String.class, ek.clazz(), "clazz() should return String.class");
        assertEquals("test", ek.message(), "message() should return test string value 'test'");
        assertSame(ex, ek.throwable(), "throwable() should return same exception");
    }
}
