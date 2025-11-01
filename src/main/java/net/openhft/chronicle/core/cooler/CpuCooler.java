/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cooler;

/**
 * This interface represents a CPU cooler and provides a method to perform some operation that will
 * "disturb" the CPU, i.e., cause it to perform some work.
 */
public interface CpuCooler {
    /**
     * Performs an operation that "disturbs" the CPU, causing it to do some work.
     */
    void disturb();
}
