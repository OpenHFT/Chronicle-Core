/**
 * Checked variants of selected {@code java.lang} types.
 *
 * <p>This package redefines a small subset of core language classes,
 * notably exception types, so that they extend {@link Exception}
 * rather than {@link RuntimeException}. It is used in specialised
 * build profiles that enforce checked error handling.
 *
 * <p>These classes are only intended for use inside the Chronicle
 * checked exceptions module. They must not be shipped as general
 * replacements for the standard Java runtime library.
 */
package java.lang;
