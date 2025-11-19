# Chronicle-Core Gemini Agent Context

This document provides context for the Gemini AI agent to understand and interact with the Chronicle-Core project.

## Project Overview

Chronicle-Core is a low-level Java library designed for high-performance and low-latency applications. It provides a set of utilities for direct memory access, operating system interactions, and efficient resource management.

The key features of Chronicle-Core include:

*   **Off-Heap Memory Access:** The `net.openhft.chronicle.core.Memory` interface and its implementation `net.openhft.chronicle.core.UnsafeMemory` provide direct memory access using `sun.misc.Unsafe`. This allows for manual memory management and avoids garbage collection overhead for critical data structures.
*   **OS Abstraction:** The `net.openhft.chronicle.core.OS` class provides an abstraction layer for interacting with the operating system. This includes memory-mapped files, getting process IDs, and checking the OS type.
*   **JVM Utilities:** The `net.openhft.chronicle.core.Jvm` class provides various utilities for interacting with the JVM, such as getting system properties, checking for debug mode, and managing exception handlers.
*   **Deterministic Resource Management:** The library includes features for reference counting and explicit resource management, allowing for predictable and timely release of resources without relying on the garbage collector.

## Building and Running

The project uses Maven for its build process. The following commands are used for building and testing the project:

*   **Build the project:**
    ```bash
    mvn install
    ```
*   **Run the tests:**
    ```bash
t    mvn test
    ```

## Development Conventions

*   **Low-Level Code:** The codebase makes extensive use of `sun.misc.Unsafe` for performance-critical operations. This means that code needs to be written carefully to avoid memory leaks and other low-level issues.
*   **Exception Handling:** The project uses a custom exception handling mechanism through the `net.openhft.chronicle.core.onoes.ExceptionHandler` interface. This allows for fine-grained control over how exceptions are logged and handled.
*   **Performance-Focused:** The library is designed for performance, so code should be written with efficiency in mind. This includes avoiding unnecessary object allocations and using primitives where possible.
*   **Cross-Platform Support:** The library supports Windows, Linux, and macOS. Any new code should be written to be compatible with all three platforms.
