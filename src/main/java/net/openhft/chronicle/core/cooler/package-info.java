/**
 * Provides classes for testing the effectiveness of various CPU cooling strategies.
 *
 * <p>This package contains the {@link net.openhft.chronicle.core.cooler.CoolerTester} class
 * which is designed to assess the performance of different {@link net.openhft.chronicle.core.cooler.CpuCooler}
 * implementations. This is achieved by configuring and executing various tests using multiple cooling strategies,
 * and recording the time taken for each test to complete.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.cooler.CpuCooler} interface represents a CPU cooler
 * and provides a method to perform operations that cause the CPU to perform some work, effectively "disturbing" it.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.cooler.CpuCoolers} enum contains various CPU cooler implementations,
 * each employing a different strategy to disturb the CPU. The specific disturbance strategy is defined by the
 * {@code disturb()} method of each enum constant.</p>
 *
 * @see net.openhft.chronicle.core.cooler.CoolerTester
 * @see net.openhft.chronicle.core.cooler.CpuCooler
 * @see net.openhft.chronicle.core.cooler.CpuCoolers
 */
package net.openhft.chronicle.core.cooler;
