/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

class ThrowingSupplierTest {

    @Test
    @DisplayName("ThrowingSupplier get returns string success value from lambda")
    void getShouldReturnResultWhenNoException() throws Exception {
        ThrowingSupplier<String, Exception> throwingSupplier = () -> "test";
        assertEquals("test", throwingSupplier.get(), "ThrowingSupplier get should return result when no exception thrown");
    }

    @Test
    @DisplayName("Get propagates exception from throwing supplier")
    void getShouldThrowException() {
        ThrowingSupplier<String, Exception> throwingSupplier = ThrowingSupplierTest::alwaysThrows;
        Exception exception = assertThrows(Exception.class, throwingSupplier::get,
                "ThrowingSupplier get should propagate the thrown exception");
        assertEquals("forced failure for ThrowingSupplierTest", exception.getMessage(),
                "ThrowingSupplier get should propagate exception with original message");
    }

    @Test
    @DisplayName("asSupplier returns string value when lambda succeeds")
    void asSupplierShouldReturnResultWhenNoException() {
        ThrowingSupplier<String, Exception> throwingSupplier = () -> "test";
        Supplier<String> supplier = ThrowingSupplier.asSupplier(throwingSupplier);
        assertEquals("test", supplier.get(), "asSupplier wrapped supplier should return result when no exception thrown");
    }

    private static String alwaysThrows() throws Exception {
        throw new Exception("forced failure for ThrowingSupplierTest");
    }
}
