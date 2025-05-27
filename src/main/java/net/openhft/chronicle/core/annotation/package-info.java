/**
 * Chronicle Core provides annotations describing constraints and design intent.
 * They help library maintainers and tooling reason about code usage.
 *
 * <ul>
 * <li>{@link Negative} &ndash; value &lt; 0</li>
 * <li>{@link NonPositive} &ndash; value &lt;= 0</li>
 * <li>{@link NonNegative} &ndash; value &gt;= 0</li>
 * <li>{@link Positive} &ndash; value &gt; 0</li>
 * <li>{@link Range} &ndash; numeric range</li>
 * <li>{@link ForceInline} &ndash; request JVM inlining</li>
 * <li>{@link HotMethod} &ndash; performance sensitive method</li>
 * <li>{@link DontChain} &ndash; skip interface chaining</li>
 * <li>{@link UsedViaReflection} &ndash; accessed by reflection</li>
 * <li>{@link PackageLocal} &ndash; intentionally package private</li>
 * <li>{@link RequiredForClient} &ndash; class name appears on wire</li>
 * <li>{@link SingleThreaded} &ndash; not thread safe</li>
 * <li>{@link ScopeConfined} &ndash; object scrubbed after use</li>
 * <li>{@link ChronicleFeature} &ndash; compile-time feature toggle</li>
 * <li>{@link TargetMajorVersion} &ndash; required Java version</li>
 * <li>{@link Java9} &ndash; method used on Java&nbsp;9+</li>
 * </ul>
 *
 * <p>Annotations with {@code RetentionPolicy.CLASS} are not visible via
 * standard reflection. Tools may read them from the class file.</p>
 *
 * <p>Design goal: make hidden performance and safety constraints
 * self-documenting without polluting the public API surface.</p>
 *
 * @author Chronicle Software
 * @since 2.23
 */
package net.openhft.chronicle.core.annotation;
