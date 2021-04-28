package net.openhft.chronicle.core.util;

public final class AssertUtil {

    /**
     * Setting {@code SKIP_ASSERTIONS} to {@code true} will disable
     * assertions in this package and will almost certainly
     * remove the assertion byte code from the target jar(s).
     * <p>
     * Here is an original class:
     * <p>
     * <blockquote><pre>
     * import static net.openhft.chronicle.core.util.AssertUtil.SKIP_ASSERTIONS;
     * import static net.openhft.chronicle.core.util.Ints.*;
     *
     * public final class AssertTest {
     *
     *     public static void testWithAssertDirectly(int x) {
     *         assert SKIP_ASSERTIONS || x >= 0 ;
     *     }
     *
     *     public static void testWithAssertDirectlyWithText(int x) {
     *         assert SKIP_ASSERTIONS || assertIfEnabled(nonNegative(), x);
     *     }
     *
     *     public static void testWithAssertMethod(int x) {
     *         assertIfEnabled(nonNegative(), x);
     *     }
     *
     * }
     * </pre></blockquote>
     * <p>
     * It has the following byte code with {@code SKIP_ASSERTIONS = false}:
     * <p>
     * <blockquote><pre>
     * % javap -c  target/test-classes/net/openhft/chronicle/core/util/AssertTest.class
     * Compiled from "AssertTest.java"
     * public final class net.openhft.chronicle.core.util.AssertTest {
     *   static final boolean $assertionsDisabled;
     *
     *   public net.openhft.chronicle.core.util.AssertTest();
     *     Code:
     *        0: aload_0
     *        1: invokespecial #2                  // Method java/lang/Object."<init>":()V
     *        4: return
     *
     *   public static void testWithAssertDirectly(int);
     *     Code:
     *        0: getstatic     #3                  // Field $assertionsDisabled:Z
     *        3: ifne          18
     *        6: iload_0
     *        7: ifge          18
     *       10: new           #4                  // class java/lang/AssertionError
     *       13: dup
     *       14: invokespecial #5                  // Method java/lang/AssertionError."<init>":()V
     *       17: athrow
     *       18: return
     *
     *   public static void testWithAssertDirectlyWithText(int);
     *     Code:
     *        0: getstatic     #3                  // Field $assertionsDisabled:Z
     *        3: ifne          24
     *        6: invokestatic  #6                  // Method net/openhft/chronicle/core/util/Ints.nonNegative:()Ljava/util/function/IntPredicate;
     *        9: iload_0
     *       10: invokestatic  #7                  // Method net/openhft/chronicle/core/util/Ints.assertIfEnabled:(Ljava/util/function/IntPredicate;I)Z
     *       13: ifne          24
     *       16: new           #4                  // class java/lang/AssertionError
     *       19: dup
     *       20: invokespecial #5                  // Method java/lang/AssertionError."<init>":()V
     *       23: athrow
     *       24: return
     *
     *   public static void testWithAssertMethod(int);
     *     Code:
     *        0: invokestatic  #6                  // Method net/openhft/chronicle/core/util/Ints.nonNegative:()Ljava/util/function/IntPredicate;
     *        3: iload_0
     *        4: invokestatic  #7                  // Method net/openhft/chronicle/core/util/Ints.assertIfEnabled:(Ljava/util/function/IntPredicate;I)Z
     *        7: pop
     *        8: return
     *
     *   static {};
     *     Code:
     *        0: ldc           #8                  // class net/openhft/chronicle/core/util/AssertTest
     *        2: invokevirtual #9                  // Method java/lang/Class.desiredAssertionStatus:()Z
     *        5: ifne          12
     *        8: iconst_1
     *        9: goto          13
     *       12: iconst_0
     *       13: putstatic     #3                  // Field $assertionsDisabled:Z
     *       16: return
     * }
     * </pre></blockquote>
     * <p>
     * But it has the following byte code with {@code SKIP_ASSERTIONS = true}:
     * <p>
     * <blockquote><pre>
     * javap -c  target/test-classes/net/openhft/chronicle/core/util/AssertTest.class
     * Compiled from "AssertTest.java"
     * public final class net.openhft.chronicle.core.util.AssertTest {
     *   public net.openhft.chronicle.core.util.AssertTest();
     *     Code:
     *        0: aload_0
     *        1: invokespecial #2                  // Method java/lang/Object."<init>":()V
     *        4: return
     *
     *   public static void testWithAssertDirectly(int);
     *     Code:
     *        0: return
     *
     *   public static void testWithAssertDirectlyWithText(int);
     *     Code:
     *        0: return
     *
     *   public static void testWithAssertMethod(int);
     *     Code:
     *        0: invokestatic  #3                  // Method net/openhft/chronicle/core/util/Ints.nonNegative:()Ljava/util/function/IntPredicate;
     *        3: iload_0
     *        4: invokestatic  #4                  // Method net/openhft/chronicle/core/util/Ints.assertIfEnabled:(Ljava/util/function/IntPredicate;I)Z
     *        7: pop
     *        8: return
     * }
     * </pre></blockquote>
     * <p>
     * As can be seen, the cost is zero when {@code SKIP_ASSERTIONS = true} in the first two cases.
     * <p>
     * Performance critical code should use one of the first two schemes devised above to assert invariants.
     * The third, more convenient form, can be used for non-performance critical code.
     */
    public static final boolean SKIP_ASSERTIONS = true;

    // Suppresses default constructor, ensuring non-instantiability.
    private AssertUtil() {
    }
}
