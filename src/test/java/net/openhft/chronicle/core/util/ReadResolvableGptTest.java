package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadResolvableGptTest {

    public interface CustomReadResolvable extends ReadResolvable<CustomReadResolvable>, Serializable {
        String getMessage();
    }

    public static class CustomSerializable implements Serializable {
        private static final long serialVersionUID = 1L;
        private String message;

        public CustomSerializable(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        private Object readResolve() {
            return new CustomSerializable(message + " resolved");
        }
    }

    @Test
    void testReadResolveWithReadResolvable() {
        CustomReadResolvable original = new CustomReadResolvable() {
            @NotNull
            @Override
            public CustomReadResolvable readResolve() {
                return new CustomReadResolvable() {
                    @Override
                    public @NotNull CustomReadResolvable readResolve() {
                        return this;
                    }

                    @Override
                    public String getMessage() {
                        return "resolved message";
                    }
                };
            }

            @Override
            public String getMessage() {
                return "original message";
            }
        };

        CustomReadResolvable resolved = ReadResolvable.readResolve(original);
        assertEquals("resolved message", resolved.getMessage());
    }

    @Test
    void testReadResolveWithNonSerializable() {
        String original = "original message";
        String resolved = ReadResolvable.readResolve(original);

        assertTrue(original == resolved);
    }
}
