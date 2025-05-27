package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ChronicleFeature} indicates a compile-time switch recognised by
 * Chronicle libraries. It is aimed at library maintainers configuring optional
 * behaviour.
 *
 * <p><b>Retention and effect:</b> Retained in the class file and discarded by
 * the Java runtime. It has no runtime effect unless Chronicle tooling
 * recognises the value.</p>
 *
 * <pre>
 * {@code @ChronicleFeature(1)}
 * public interface CustomFeature { }
 * </pre>
 *
 * @see TargetMajorVersion
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ChronicleFeature {
    int value();
}
