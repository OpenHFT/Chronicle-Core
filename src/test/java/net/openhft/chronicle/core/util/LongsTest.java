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

package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LongsTest {

    @Test
    public void require1Arg() {
        test(1, 0, v -> Longs.require(Longs.positive(), v));
        test(1, -1, v -> Longs.require(Longs.positive(), v));
    }

    @Test
    public void require2Arg() {
        test(1, 0, v -> Longs.require(Longs.equalTo(), v, 1));
    }

    @Test
    public void require3Ard() {
        test(0, 16, v -> Longs.require(Longs.between(), v, 0, 16));
        test(15, -1, v -> Longs.require(Longs.between(), v, 0, 16));
    }

    @Test
    public void shortAligned() {
        test(0, 1, v -> Longs.require(Longs.shortAligned(), v));
        test(2, 3, v -> Longs.require(Longs.shortAligned(), v));
        test(4, 5, v -> Longs.require(Longs.shortAligned(), v));
        test(6, 7, v -> Longs.require(Longs.shortAligned(), v));
        test(8, 9, v -> Longs.require(Longs.shortAligned(), v));
    }
    @Test
    public void intAligned() {
        test(0, 1, v -> Longs.require(Longs.intAligned(), v));
        test(0, 2, v -> Longs.require(Longs.intAligned(), v));
        test(0, 3, v -> Longs.require(Longs.intAligned(), v));
        test(4, 5, v -> Longs.require(Longs.intAligned(), v));
        test(4, 6, v -> Longs.require(Longs.intAligned(), v));
    }
    @Test
    public void longAligned() {
        test(0, 1, v -> Longs.require(Longs.longAligned(), v));
        test(0, 2, v -> Longs.require(Longs.longAligned(), v));
        test(0, 3, v -> Longs.require(Longs.longAligned(), v));
        test(0, 4, v -> Longs.require(Longs.longAligned(), v));
        test(0, 5, v -> Longs.require(Longs.longAligned(), v));
        test(0, 6, v -> Longs.require(Longs.longAligned(), v));
        test(0, 7, v -> Longs.require(Longs.longAligned(), v));
        test(8, 9, v -> Longs.require(Longs.longAligned(), v));
    }
    
    private void test(final int happy,
                      final int sad,
                      @NotNull final LongUnaryOperator mapper) {
        try {
            final long result = mapper.applyAsLong(happy);
            assertEquals(happy, result);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
        try {
            final long result2 = mapper.applyAsLong(sad);
            fail(result2 + " is not valid!");
        } catch (IllegalArgumentException ignored) {
            // Happy path
        }
    }

    interface LongUnaryOperator {
        long applyAsLong(int happy) throws IllegalArgumentException;
    }

}