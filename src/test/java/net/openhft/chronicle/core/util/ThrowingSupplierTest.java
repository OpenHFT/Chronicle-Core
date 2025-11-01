/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

class ThrowingSupplierTest {

    @Test
    void getShouldReturnResultWhenNoException() throws Exception {
        ThrowingSupplier<String, Exception> throwingSupplier = () -> "test";
        assertEquals("test", throwingSupplier.get());
    }

    @Test
    void getShouldThrowException() {
        ThrowingSupplier<String, Exception> throwingSupplier = () -> alwaysThrows();
        Exception exception = assertThrows(Exception.class, throwingSupplier::get);
        assertEquals("error", exception.getMessage());
    }

    @Test
    void asSupplierShouldReturnResultWhenNoException() {
        ThrowingSupplier<String, Exception> throwingSupplier = () -> "test";
        Supplier<String> supplier = ThrowingSupplier.asSupplier(throwingSupplier);
        assertEquals("test", supplier.get());
    }

    private static String alwaysThrows() throws Exception {
        throw new Exception("error");
    }
}
