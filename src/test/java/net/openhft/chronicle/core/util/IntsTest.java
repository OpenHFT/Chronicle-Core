package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IntsTest {

    @Test
    public void require1Arg() {
        test(1, 0, v -> Ints.require(Ints.positive(), v));
        test(1, -1, v -> Ints.require(Ints.positive(), v));
    }

    @Test
    public void require2Arg() {
        test(1, 0, v -> Ints.require(Ints.equalTo(), v, 1));
    }

    @Test
    public void require3Ard() {
        test(0, 16, v -> Ints.require(Ints.between(), v, 0, 16));
        test(15, -1, v -> Ints.require(Ints.between(), v, 0, 16));
    }

    @Test
    public void shortAligned() {
        test(0, 1, v -> Ints.require(Ints.shortAligned(), v));
        test(2, 3, v -> Ints.require(Ints.shortAligned(), v));
        test(4, 5, v -> Ints.require(Ints.shortAligned(), v));
        test(6, 7, v -> Ints.require(Ints.shortAligned(), v));
        test(8, 9, v -> Ints.require(Ints.shortAligned(), v));
    }
  @Test
    public void intAligned() {
        test(0, 1, v -> Ints.require(Ints.intAligned(), v));
        test(0, 2, v -> Ints.require(Ints.intAligned(), v));
        test(0, 3, v -> Ints.require(Ints.intAligned(), v));
        test(4, 5, v -> Ints.require(Ints.intAligned(), v));
        test(4, 6, v -> Ints.require(Ints.intAligned(), v));
    }
  @Test
    public void longAligned() {
        test(0, 1, v -> Ints.require(Ints.longAligned(), v));
        test(0, 2, v -> Ints.require(Ints.longAligned(), v));
        test(0, 3, v -> Ints.require(Ints.longAligned(), v));
        test(0, 4, v -> Ints.require(Ints.longAligned(), v));
        test(0, 5, v -> Ints.require(Ints.longAligned(), v));
        test(0, 6, v -> Ints.require(Ints.longAligned(), v));
        test(0, 7, v -> Ints.require(Ints.longAligned(), v));
        test(8, 9, v -> Ints.require(Ints.longAligned(), v));
    }

    private void test(final int happy,
                      final int sad,
                      @NotNull final IntUnaryOperator mapper) {
        try {
            final long result = mapper.applyAsInt(happy);
            assertEquals(happy, result);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
        try {
            final long result2 = mapper.applyAsInt(sad);
            fail(result2 + " is not valid!");
        } catch (IllegalArgumentException ignored) {
            // Happy path
        }
    }

    interface IntUnaryOperator {
        long applyAsInt(int happy) throws IllegalArgumentException;
    }

}