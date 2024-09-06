/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Provides classes for testing the effectiveness of various CPU cooling strategies.
 *
 * <p>This package contains the {@link net.openhft.chronicle.core.cooler.CoolerTester} class
 * which is designed to assess the performance of different {@link net.openhft.chronicle.core.cooler.CpuCooler}
 * implementations. This is achieved by configuring and executing various tests using multiple cooling strategies,
 * and recording the time taken for each test to complete.
 *
 * <p>The {@link net.openhft.chronicle.core.cooler.CpuCooler} interface represents a CPU cooler
 * and provides a method to perform operations that cause the CPU to perform some work, effectively "disturbing" it.
 *
 * <p>The {@link net.openhft.chronicle.core.cooler.CpuCoolers} enum contains various CPU cooler implementations,
 * each employing a different strategy to disturb the CPU. The specific disturbance strategy is defined by the
 * {@code disturb()} method of each enum constant.
 *
 * @see net.openhft.chronicle.core.cooler.CoolerTester
 * @see net.openhft.chronicle.core.cooler.CpuCooler
 * @see net.openhft.chronicle.core.cooler.CpuCoolers
 */
package net.openhft.chronicle.core.cooler;
