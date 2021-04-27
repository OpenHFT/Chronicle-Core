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
     *
     * import static net.openhft.chronicle.core.util.AssertUtil.SKIP_ASSERTIONS;
     * import static net.openhft.chronicle.core.util.IntCondition.NON_NEGATIVE;
     * import static net.openhft.chronicle.core.util.Ints.*;
     *
     * public final class AssertTest {
     *
     *     public static void testWithAssertDirectly() {
     *         assert SKIP_ASSERTIONS || NON_NEGATIVE.test(3);
     *     }
     *
     *     public static void testWithAssertDirectlyWithText() {
     *         assert SKIP_ASSERTIONS || NON_NEGATIVE.test(3) : failDescription(NON_NEGATIVE, 3);
     *     }
     *
     *     public static void testWithAssertMethod() {
     *         assertIfEnabled(NON_NEGATIVE, 3);
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
     *   public static void testWithAssertDirectly();
     *     Code:
     *        0: getstatic     #3                  // Field $assertionsDisabled:Z
     *        3: ifne          24
     *        6: getstatic     #4                  // Field net/openhft/chronicle/core/util/IntCondition.NON_NEGATIVE:Lnet/openhft/chronicle/core/util/IntCondition;
     *        9: iconst_3
     *       10: invokevirtual #5                  // Method net/openhft/chronicle/core/util/IntCondition.test:(I)Z
     *       13: ifne          24
     *       16: new           #6                  // class java/lang/AssertionError
     *       19: dup
     *       20: invokespecial #7                  // Method java/lang/AssertionError."<init>":()V
     *       23: athrow
     *       24: return
     *
     *   public static void testWithAssertDirectlyWithText();
     *     Code:
     *        0: getstatic     #3                  // Field $assertionsDisabled:Z
     *        3: ifne          31
     *        6: getstatic     #4                  // Field net/openhft/chronicle/core/util/IntCondition.NON_NEGATIVE:Lnet/openhft/chronicle/core/util/IntCondition;
     *        9: iconst_3
     *       10: invokevirtual #5                  // Method net/openhft/chronicle/core/util/IntCondition.test:(I)Z
     *       13: ifne          31
     *       16: new           #6                  // class java/lang/AssertionError
     *       19: dup
     *       20: getstatic     #4                  // Field net/openhft/chronicle/core/util/IntCondition.NON_NEGATIVE:Lnet/openhft/chronicle/core/util/IntCondition;
     *       23: iconst_3
     *       24: invokestatic  #8                  // Method net/openhft/chronicle/core/util/Ints.failDescription:(Ljava/util/function/IntPredicate;I)Ljava/lang/String;
     *       27: invokespecial #9                  // Method java/lang/AssertionError."<init>":(Ljava/lang/Object;)V
     *       30: athrow
     *       31: return
     *
     *   public static void testWithAssertMethod();
     *     Code:
     *        0: getstatic     #4                  // Field net/openhft/chronicle/core/util/IntCondition.NON_NEGATIVE:Lnet/openhft/chronicle/core/util/IntCondition;
     *        3: iconst_3
     *        4: invokestatic  #10                 // Method net/openhft/chronicle/core/util/Ints.assertIfEnabled:(Ljava/util/function/IntPredicate;I)V
     *        7: return
     *
     *   static {};
     *     Code:
     *        0: ldc           #11                 // class net/openhft/chronicle/core/util/AssertTest
     *        2: invokevirtual #12                 // Method java/lang/Class.desiredAssertionStatus:()Z
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
     *   public static void testWithAssertDirectly();
     *     Code:
     *        0: return
     *
     *   public static void testWithAssertDirectlyWithText();
     *     Code:
     *        0: return
     *
     *   public static void testWithAssertMethod();
     *     Code:
     *        0: getstatic     #3                  // Field net/openhft/chronicle/core/util/IntCondition.NON_NEGATIVE:Lnet/openhft/chronicle/core/util/IntCondition;
     *        3: iconst_3
     *        4: invokestatic  #4                  // Method net/openhft/chronicle/core/util/Ints.assertIfEnabled:(Ljava/util/function/IntPredicate;I)V
     *        7: return
     * }
     * </pre></blockquote>
     * <p>
     * As can be seen, the cost is zero when SKIP_ASSERTIONS = true in the first two cases.
     * <p>
     * Performance critical code should use one of the first two schemes devised above to assert invariants.
     * The third, more convenient form, can be used for non-performance critical code.
     */
    public static final boolean SKIP_ASSERTIONS = true;

    // Suppresses default constructor, ensuring non-instantiability.
    private AssertUtil() {
    }
}
