/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

class ThrowingSupplierTest {

    @Test
    void getShouldReturnResultWhenNoException() throws Exception {
        ThrowingSupplier<String, Exception> throwingSupplier = () -> "test";
        assertEquals("test", throwingSupplier.get(), "ThrowingSupplier get should return result when no exception thrown");
    }

    @Test
    void getShouldThrowException() {
        ThrowingSupplier<String, Exception> throwingSupplier = ThrowingSupplierTest::alwaysThrows;
        Exception exception = assertThrows(Exception.class, throwingSupplier::get,
                "get should propagate supplier exception");
        String message = exception.getMessage();
        assertEquals("test supplier failure message", message, "ThrowingSupplier get should propagate the original exception message");
    }

    @Test
    void asSupplierShouldReturnResultWhenNoException() {
        ThrowingSupplier<String, Exception> throwingSupplier = () -> "test";
        Supplier<String> supplier = ThrowingSupplier.asSupplier(throwingSupplier);
        assertEquals("test", supplier.get(), "asSupplier wrapped supplier should return result when no exception thrown");
    }

    private static String alwaysThrows() throws Exception {
        throw new Exception("test supplier failure message");
    }
}
