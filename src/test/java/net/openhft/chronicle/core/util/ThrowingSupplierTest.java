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
        ThrowingSupplier<String, Exception> throwingSupplier = () -> { throw new Exception("error"); };
        Exception exception = assertThrows(Exception.class, throwingSupplier::get);
        assertEquals("error", exception.getMessage());
    }

    @Test
    void asSupplierShouldReturnResultWhenNoException() {
        ThrowingSupplier<String, Exception> throwingSupplier = () -> "test";
        Supplier<String> supplier = ThrowingSupplier.asSupplier(throwingSupplier);
        assertEquals("test", supplier.get());
    }
}
