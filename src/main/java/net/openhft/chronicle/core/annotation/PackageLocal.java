package net.openhft.chronicle.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Deliberately made package local, usually to avoid accessor methods
 */
@Documented
@Retention(SOURCE)
@Target({METHOD, FIELD, CONSTRUCTOR, TYPE})
public @interface PackageLocal {
}
