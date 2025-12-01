# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Chronicle-Core is a foundational low-level Java library providing OS and JVM abstraction layers, off-heap memory access, deterministic resource management, and diagnostics. It serves as Layer 0 (Foundation) in the Chronicle ecosystem, supporting higher-level modules like Chronicle-Bytes, Chronicle-Queue, Chronicle-Wire, and Chronicle-Threads.

**Key Design Principles:**
- **Allocation-free hot paths**: Performance-critical code avoids object allocation after warm-up
- **Deterministic resource management**: Resources are explicitly released without waiting for GC
- **Thread-safety by design**: Single-threaded components use explicit guards to detect misuse
- **Graceful degradation**: Optional native features (JNA, POSIX) fall back to safe alternatives

## Build Commands

**Prerequisites:**
- Java 1.8 (required - later versions may cause compiler warnings)
- Maven 3.x

```bash
# Standard build with tests
mvn clean verify

# Quick build without tests
mvn clean install -DskipTests

# Run specific test class
mvn test -Dtest=JvmTest

# Run single test method
mvn test -Dtest=JvmTest#testGetProcessId

# Build with assertions enabled
mvn clean install -Passertions

# Run benchmarks (if applicable)
mvn clean test -Prun-benchmarks
```

## Architecture Overview

Chronicle-Core is organized into capability-focused packages:

**Core Capabilities (`net.openhft.chronicle.core`):**
- `OS` - Operating system utilities (process ID, hostname, memory mapping, page size)
- `Jvm` - JVM utilities (debug detection, system properties, reflection helpers, exception rethrowing)
- `Memory` / `UnsafeMemory` - Direct memory access with atomic operations

**Resource Management (`net.openhft.chronicle.core.io`):**
- `AbstractCloseable` - Base class for closeable resources with single-use lifecycle
- `ReferenceCounted` - Interface for reference-counted resources
- `AbstractCloseableReferenceCounted` - Combines closeable and reference counting
- `BackgroundResourceReleaser` - Defers heavy deallocation off the critical path
- `SingleThreadedChecked` - Thread-safety enforcement for single-threaded components

**Threading (`net.openhft.chronicle.core.threads`):**
- Thread affinity integration (via optional `affinity` dependency)
- Pauser integration for low-latency waiting strategies
- `CleaningThreadLocal` for deterministic cleanup

**Diagnostics (`net.openhft.chronicle.core.onoes`):**
- `ExceptionHandler` - Custom exception handling strategies
- `Slf4jExceptionHandler` - Bridge to SLF4J logging
- `RecordingExceptionHandler` - Captures exceptions for testing

**Utilities (`net.openhft.chronicle.core.util`):**
- `ClassLocal` - Per-class caching without static fields
- `StringInterner` / `EnumInterner` - Efficient string/enum pooling
- `Histogram` - High-performance wide-range histogram
- `Maths` - Type-safe casting and rounding functions

**Time (`net.openhft.chronicle.core.time`):**
- `TimeProvider` - Abstraction for deterministic time in tests
- `SystemTimeProvider` - Production time provider

## Common Development Patterns

### Resource Management

Always extend `AbstractCloseable` or `AbstractCloseableReferenceCounted` for resources that need deterministic cleanup:

```java
public class MyResource extends AbstractCloseable {
    @Override
    protected void performClose() {
        // Release native resources, unmap memory, etc.
    }
}
```

When using reference counting, ensure proper reserve/release pairing:

```java
ReferenceCounted resource = ...
ReferenceOwner owner = ReferenceOwner.temporary("description");
try {
    resource.reserve(owner);
    // Use resource
} finally {
    resource.release(owner);
}
```

### Thread Safety

Single-threaded components should implement `SingleThreadedChecked` (usually via `AbstractCloseable`). To hand off to another thread:

```java
// Initialize in main thread
MyCloseable resource = new MyCloseable();
resource.someMethod(); // Sets used-by thread

// Hand off to worker thread
resource.singleThreadedCheckReset();
workerThread.submit(() -> resource.someMethod());
```

### System Properties

Chronicle-Core automatically loads `system.properties` from the working directory at startup. Use `Jvm.getProperty()` methods to ensure proper initialization order:

```java
// Correct - guarantees Jvm is initialized
int value = Jvm.getInteger("my.property", defaultValue);

// Incorrect - may read before system.properties is loaded
int value = Integer.getInteger("my.property", defaultValue);
```

### Off-Heap Memory

Use `OS.memory()` for direct memory operations:

```java
Memory memory = OS.memory();
long address = memory.allocate(1024);
try {
    memory.writeInt(address, 42);
    int value = memory.readInt(address);
    // Atomic operations available
    memory.compareAndSwapInt(address, 42, 100);
} finally {
    memory.freeMemory(address, 1024);
}
```

## Testing Guidelines

**Resource Cleanup Verification:**
All tests must verify off-heap resources are properly released using `Chronicle-Test-Framework`:

```java
@Test
public void testSomething() {
    // Test code that allocates resources
    assertReferencesReleased(); // From Chronicle-Test-Framework
}
```

**Deterministic Time:**
Use `SystemTimeProvider` or custom `TimeProvider` implementations for time-dependent tests to avoid flakiness.

**Thread Safety:**
Enable resource tracing in tests via `system.properties`:
```properties
jvm.resource.tracing=true
```

## Documentation Standards

**Location:** All documentation lives in `src/main/docs/` (AsciiDoc format)

**Key Files:**
- `architecture-overview.adoc` - High-level architecture and component interactions
- `project-requirements.adoc` - Functional and non-functional requirements (Nine-Box taxonomy)
- `decision-log.adoc` - Architecture Decision Records (ADRs)
- `security-review.adoc` - Security analysis and threat model

**Language & Format:**
- British English (e.g., "organisation", "licence")
- ISO-8859-1 character set only (no Unicode smart quotes or accented characters)
- AsciiDoc with `:source-highlighter: rouge`

**Requirement Tags:**
Preserve Nine-Box taxonomy tags in code comments and documentation:
- `CORE-FN-xxx` - Functional requirements
- `CORE-NF-P-xxx` - Performance requirements
- `CORE-NF-S-xxx` - Security requirements
- `CORE-NF-O-xxx` - Operability requirements
- `CORE-TEST-xxx` - Testing requirements
- `CORE-DOC-xxx` - Documentation requirements

## Code Style

**General:**
- 4-space indentation (no tabs)
- K&R brace style (opening brace on same line)
- Explicit types preferred over `var`
- Minimal Javadoc for obvious getters/setters

**Javadoc Focus:**
Document behavioural contracts, edge cases, thread-safety guarantees, units, and performance characteristics. Avoid restating what the method signature already conveys.

**Good Javadoc:**
```java
/**
 * Allocates off-heap memory aligned to page boundaries.
 *
 * @param size allocation size in bytes, must be positive
 * @return memory address, caller must free via freeMemory()
 * @throws IllegalArgumentException if size <= 0
 * @throws OutOfMemoryError if native allocation fails
 */
long allocate(long size);
```

**Bad Javadoc:**
```java
/**
 * Gets the value.
 * @return the value
 */
int getValue();
```

## Common Pitfalls

1. **Java Version Mismatch**: Build requires Java 1.8. Later versions may introduce warnings that break the build.

2. **Resource Leaks**: Forgetting to call `close()` or `releaseLast()` causes off-heap memory leaks. Always use try-finally or try-with-resources.

3. **Thread Safety Violations**: Single-threaded components will throw exceptions if accessed from multiple threads. Use `singleThreadedCheckReset()` when handing off ownership.

4. **System Properties Load Order**: Direct calls to `System.getProperty()` may occur before `system.properties` is loaded. Use `Jvm.getProperty()` instead.

5. **Memory Alignment**: Some platforms (ARM) require aligned memory access. Test on multiple architectures or use Chronicle's alignment-aware methods.

## Module-Specific Files

- `AGENTS.md` - Guidance for AI agents and contributors (coding standards, commit etiquette)
- `TODO.md` - Repository-specific documentation and architecture tasks
- `system.properties` - Default JVM properties loaded at startup
- `effective-core-checkstyle.xml` - Effective Checkstyle configuration (for reference)
- `effective-core-sonar.xml` - Effective SonarQube configuration (for reference)

## Related Documentation

See `AGENTS.md` for:
- Language and character-set policy (British English, ISO-8859-1)
- Javadoc guidelines
- Commit message and PR etiquette
- Nine-Box requirement taxonomy
- AsciiDoc formatting standards

See `README.adoc` for:
- Feature overview with code examples
- OS calls, JVM access methods, memory management
- Closeable resources and reference counting
- Thread safety patterns
- Object pooling and caching
