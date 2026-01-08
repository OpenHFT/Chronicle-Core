# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Chronicle-Core is an advanced low-level library that provides powerful tools for high-performance Java applications. It focuses on deterministic resource management, direct memory access, OS-level operations, and zero-garbage collection patterns. This is a foundational library used by other Chronicle projects (Queue, Wire, Map, etc.).

**Key Characteristics:**
- Performance-critical code with zero-allocation patterns
- Deterministic resource management (no waiting for GC)
- Direct memory access via Unsafe
- Single-threaded by design with runtime thread-safety checks
- Multi-release JAR (Java 8 baseline, Java 11+ optimizations)

## Build Commands

### Standard Build and Test
```bash
mvn clean install
```

### Run Specific Test
```bash
# Run a single test class
mvn test -Dtest=ChronicleInitTest

# Run a single test method
mvn test -Dtest=ChronicleInitTest#initShouldNotThrowException
```

### Build with Assertions Enabled
```bash
mvn clean install -Passertions
```

### Run Benchmarks
```bash
mvn clean install -Prun-benchmarks
```

### Check Code Coverage
Coverage thresholds are configured in pom.xml:
- Line coverage: 77%
- Branch coverage: 67%

JaCoCo reports are generated during test phase.

## Architecture Overview

### Core Resource Management Pattern

Chronicle-Core uses deterministic resource management instead of relying on garbage collection. All resources follow one of two lifecycle patterns:

**1. Closeable Resources (AbstractCloseable)**
- Simple lifecycle: open → close
- Cannot be used after closing
- Base class for single-owner resources
- Implements thread-safety checks
- Subclasses override `performClose()`

**2. Reference Counted Resources (AbstractReferenceCounted)**
- For shared resources with multiple owners
- Uses `reserve(ReferenceOwner)` / `release(ReferenceOwner)` pattern
- Resource freed when reference count reaches zero
- Cannot reserve after closed
- Subclasses override `performRelease()`

### Key Packages and Their Purpose

- **net.openhft.chronicle.core** - Core classes (OS, Jvm, Memory, Bootstrap)
- **net.openhft.chronicle.core.io** - Resource management (AbstractCloseable, AbstractReferenceCounted, IOTools)
- **net.openhft.chronicle.core.threads** - Event loops, handlers, thread dumps
- **net.openhft.chronicle.core.util** - Utilities (ClassLocal, Histogram, Maths, CompilerUtils)
- **net.openhft.chronicle.core.pool** - Object pooling (StringInterner, EnumInterner)
- **net.openhft.chronicle.core.time** - Time providers and abstractions
- **net.openhft.chronicle.core.onoes** - Exception handling framework
- **net.openhft.chronicle.core.scoped** - Try-with-resources compatible resource borrowing
- **net.openhft.chronicle.core.values** - Mutable value types for off-heap data

### Important Base Classes

- **AbstractCloseable** (`src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java`) - Base for closeable resources
- **AbstractReferenceCounted** (`src/main/java/net/openhft/chronicle/core/io/AbstractReferenceCounted.java`) - Base for ref-counted resources
- **Memory/UnsafeMemory** (`src/main/java/net/openhft/chronicle/core/UnsafeMemory.java`) - Direct memory operations
- **OS** (`src/main/java/net/openhft/chronicle/core/OS.java`) - System calls and memory mapping
- **Jvm** (`src/main/java/net/openhft/chronicle/core/Jvm.java`) - JVM introspection and utilities

### Thread Safety Model

**Single-threaded by Default:**
- Most classes implement `SingleThreadedChecked`
- Runtime validation that objects are used by only one thread
- Can be disabled with `-Ddisable.single.threaded.check=true`
- Use `singleThreadedCheckReset()` to hand-off between threads

**Common Patterns:**
1. **Construct in one thread, use in another** - Call `singleThreadedCheckReset()` after initialization
2. **Reset in constructor** - Factory methods call `singleThreadedCheckReset()` before returning
3. **Delegate checks** - Override `singleThreadedCheckReset()` to propagate to contained resources

### Memory Management

- Direct memory allocated via `OS.memory().allocate(size)`
- Memory-mapped files via `OS.map(fileChannel, mode, offset, size)`
- Always free with `OS.memory().freeMemory(address, size)` or `OS.unmap(address, size)`
- Use `CleaningRandomAccessFile` for files with cleanup support

### Exception Handling

Uses a pluggable exception handler system (`net.openhft.chronicle.core.onoes`):
- `Jvm.error()` - Critical errors
- `Jvm.warn()` - Warnings
- `Jvm.perf()` - Performance warnings
- `Jvm.debug()` - Debug messages

Default handlers log to SLF4J. Can be customized via `Jvm.setExceptionHandlers()`.

### Background Resource Releasing

Controlled by system properties:
- `background.releaser=true` - Enable queuing of resources for background release
- `background.releaser.thread=true` - Start background thread for releasing

See README.adoc section "Releasing Resources" for details.

## Testing Conventions

- Tests extend `CoreTestCommon` base class
- Uses JUnit 5 (Jupiter) with some JUnit 4 compatibility
- Tests run with `forkCount=4` and `reuseForks=true` for performance
- Use `chronicle-test-framework` for utilities like `JavaProcessBuilder`
- Resource tracing is enabled by default in tests

## Important System Properties

Chronicle-Core loads `system.properties` file from current/parent directory automatically. See `docs/systemProperties.adoc` for full list.

**Key Properties:**
- `jvm.resource.tracing` - Enable resource leak detection
- `disable.single.threaded.check` - Disable thread-safety checks
- `background.releaser` / `background.releaser.thread` - Background resource cleanup
- `chronicle.init.runnable` - Pre-initialization hook class
- `chronicle.postinit.runnable` - Post-initialization hook class

## Module Structure

- **chronicle-core** - Main module
- **assertions-enabled/assertions-disabled** - Zero-cost assertions variants
- **benchmarks** - JMH performance benchmarks
- **checked-exceptions** - Temporary checked exception variants for code review

## Multi-Release JAR

The project builds a multi-release JAR:
- `src/main/java` - Java 8 baseline
- `src/main/java11` - Java 11+ specific implementations
- Output: `META-INF/versions/11/` in JAR

When adding new features, consider if Java 11+ has better APIs (e.g., VarHandle).

## Code Style and Patterns

### When Adding New Resources

1. Extend `AbstractCloseable` for simple lifecycle
2. Extend `AbstractReferenceCounted` for shared resources
3. Implement `SingleThreadedChecked` if single-threaded
4. Override `performClose()` or `performRelease()`
5. Document ownership and threading expectations

### Performance Considerations

- Avoid allocations in hot paths
- Use primitive types when possible
- Consider `@HotMethod` and `@ForceInline` annotations
- Benchmark with JMH in `benchmarks/` module
- Profile before optimizing

### Testing Resource Management

```java
// Example test pattern
@Test
public void shouldReleaseResource() {
    MyCloseable resource = new MyCloseable();
    assertFalse(resource.isClosed());

    resource.close();
    assertTrue(resource.isClosed());

    // Should be idempotent
    resource.close();
    assertTrue(resource.isClosed());
}
```

### Testing Thread Safety

```java
// Example pattern
@Test
public void shouldDetectWrongThread() {
    MyResource resource = new MyResource();
    resource.useOnCurrentThread(); // Sets used-by thread

    assertThrows(ThreadingIllegalStateException.class, () -> {
        new Thread(() -> resource.useOnCurrentThread()).start().join();
    });
}
```

## Binary Compatibility

Chronicle-Core enforces binary compatibility with previous versions:
- Plugin: `binary-compatibility-enforcer-plugin`
- Reference version: `2.27ea0`
- Required compatibility: 99.8%
- Runs during `verify` phase

Breaking changes require careful consideration and documentation.

## Debugging Tips

### Enable Resource Tracing
```bash
mvn test -Djvm.resource.tracing=true -Dtest=YourTest
```

### Enable Assertions
Build with `-Passertions` profile to use assertions-enabled module.

### Thread Dumps
Use `ThreadDump.dumpThreads()` to capture thread states for deadlock detection.

### Monitoring
Classes implementing `Monitorable` can be monitored for resource state.

## Related Documentation

- **README.adoc** - Feature overview and examples
- **docs/systemProperties.adoc** - Complete system properties reference
- **checked-exceptions/README.adoc** - Checked exception utilities
- Build logs: `build-java8.log`, `build-java21.log` in repository root

## Common Gotchas

1. **Don't forget to close resources** - Use try-with-resources or explicit close() calls
2. **Thread safety checks fire unexpectedly** - Use `singleThreadedCheckReset()` when handing off between threads
3. **Test failures with resource leaks** - Check for unclosed resources in test setup/teardown
4. **UnsupportedOperationException on some platforms** - OS/Memory operations may not work on all platforms (e.g., ARM alignment)
5. **Module boundary issues** - Remember this is an OSGi bundle with specific export packages
