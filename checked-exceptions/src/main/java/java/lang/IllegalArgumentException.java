/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package java.lang;

/**
 * Checked counterpart of {@link java.lang.IllegalArgumentException} for APIs that avoid unchecked
 * exceptions in error paths.
 */
public class IllegalArgumentException extends Exception {
    public IllegalArgumentException() {
    }

    public IllegalArgumentException(String message) {
        super(message);
    }

    public IllegalArgumentException(Throwable cause) {
        super(cause);
    }
}
