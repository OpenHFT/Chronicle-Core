/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.test;

import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecordingLogger implements InvocationHandler {
    private final String name;
    private final Logger logger;
    private final List<Call> calls = new ArrayList<>();
    private String throwingMethod;
    private RuntimeException thrown;

    public RecordingLogger(String name) {
        this.name = name;
        logger = (Logger) Proxy.newProxyInstance(
                Logger.class.getClassLoader(),
                new Class<?>[]{Logger.class},
                this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        switch (methodName) {
            case "getName":
                return name;
            case "toString":
                return "RecordingLogger[" + name + ']';
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return System.identityHashCode(proxy);
            default:
                calls.add(new Call(methodName, args == null ? new Object[0] : args.clone()));
                if (methodName.equals(throwingMethod))
                    throw thrown;
                return defaultValue(method.getReturnType());
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == Void.TYPE)
            return null;
        if (returnType == Boolean.TYPE)
            return true;
        if (returnType == Byte.TYPE)
            return (byte) 0;
        if (returnType == Short.TYPE)
            return (short) 0;
        if (returnType == Integer.TYPE)
            return 0;
        if (returnType == Long.TYPE)
            return 0L;
        if (returnType == Float.TYPE)
            return 0.0f;
        if (returnType == Double.TYPE)
            return 0.0d;
        if (returnType == Character.TYPE)
            return (char) 0;
        return null;
    }

    public Logger logger() {
        return logger;
    }

    public void throwFrom(String methodName, RuntimeException thrown) {
        this.throwingMethod = methodName;
        this.thrown = thrown;
    }

    public int callCount(String methodName) {
        int count = 0;
        for (Call call : calls)
            if (call.methodName().equals(methodName))
                count++;
        return count;
    }

    public boolean hasCalls() {
        return !calls.isEmpty();
    }

    public List<Call> calls() {
        return Collections.unmodifiableList(calls);
    }

    public static final class Call {
        private final String methodName;
        private final Object[] args;

        private Call(String methodName, Object[] args) {
            this.methodName = methodName;
            this.args = args;
        }

        public String methodName() {
            return methodName;
        }

        public Object[] args() {
            return args.clone();
        }
    }
}
