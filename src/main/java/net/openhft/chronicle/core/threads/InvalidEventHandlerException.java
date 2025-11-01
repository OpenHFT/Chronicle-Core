/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

/**
 * Represents an exception that is thrown when an event handler is invalid or
 * needs to be removed from an event loop.
 *
 * <p>This exception class also provides a reusable static instance which is
 * unmodifiable and does not contain any stack trace information. This reusable
 * instance can be used for flow-control purposes such as signalling that an
 * event handler should be removed from the event loop due to successful completion.
 *
 * <p>It is important to note that the reusable instance should not be used where
 * error information or stack traces are needed, as it doesn't provide these details.
 * <p>
 * Example usage:
 * <pre>
 * class SampleHandler implements EventHandler {
 *     {@code @Override}
 *     public boolean action() throws InvalidEventHandlerException {
 *         // perform work
 *         if (done)
 *             throw InvalidEventHandlerException.reusable();
 *         return true;
 *     }
 * }</pre>
 */
public class InvalidEventHandlerException extends Exception {
    private static final long serialVersionUID = 0L;

    private static final InvalidEventHandlerException STATIC = new ReusableInvalidEventHandlerException();

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidEventHandlerException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with no detail message.
     */
    public InvalidEventHandlerException() {
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause
     */
    public InvalidEventHandlerException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns a reusable, pre-created, InvalidEventHandlerException that is
     * unmodifiable and contains no stack trace.
     *
     * <p>This is useful for situations where throwing an InvalidEventHandlerException
     * is used strictly for flow-control (e.g. removing an EventHandler from
     * an EventLoop when the former has completed with no errors).
     *
     * <p>Note that this reusable instance contains no stack trace information and
     * should not be used in scenarios where error information is necessary.
     *
     * @return a singleton without stack trace - use only for flow control
     */
    public static InvalidEventHandlerException reusable() {
        return STATIC;
    }

    private static final class ReusableInvalidEventHandlerException extends InvalidEventHandlerException {
        private static final long serialVersionUID = 0L;

        public ReusableInvalidEventHandlerException() {
            super("Reusable InvalidEventHandlerException with no stack trace.");
            setStackTrace(new StackTraceElement[0]);
        }

        @Override
        public synchronized Throwable initCause(Throwable cause) {
            return this;
        }

        @Override
        public void setStackTrace(StackTraceElement[] stackTrace) {
            if (stackTrace.length == 0)
                super.setStackTrace(stackTrace);
        }
    }
}
