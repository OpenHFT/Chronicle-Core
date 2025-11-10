//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package java.lang;

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
